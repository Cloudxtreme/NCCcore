package com.NccDhcp;

import com.NccSystem.NccUtils;
import com.google.common.primitives.Ints;
import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NccDhcpServer {
    private static Logger logger = Logger.getLogger(NccDhcpServer.class);

    private static DatagramSocket dhcpSocket;

    public NccDhcpServer() {
        try {
            this.dhcpSocket = new DatagramSocket(67);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        Thread dhcpWatchThread = new Thread(new Runnable() {
            @Override
            public void run() {

                class NccDhcpTimer extends TimerTask {
                    @Override
                    public void run() {
                        NccDhcpLeases leases = new NccDhcpLeases();
                        leases.cleanupLeases();
                    }
                }

                Timer dhcpTimer = new Timer();
                dhcpTimer.schedule(new NccDhcpTimer(), 1000, 1000);
            }
        });
        dhcpWatchThread.start();

        Thread dhcpMainThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        final byte[] recv = new byte[512];

                        final DatagramPacket inPkt = new DatagramPacket(recv, recv.length);
                        dhcpSocket.receive(inPkt);

                        Thread dhcpReceiveThread = new Thread(new Runnable() {

                            @Override
                            public void run() {

                                try {
                                    NccDhcpPacket pkt = new NccDhcpPacket(recv, inPkt.getLength());

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_DISCOVER) {

                                        logger.debug("DHCPDISCOVER from " + inPkt.getAddress().toString());

                                        logger.debug("RelayAgent: " + pkt.getRelayAgent().getHostAddress() + " remoteID: " + pkt.getOpt82RemoteID() + " clientID: " + pkt.getClientID());

                                        InetAddress agentIP = pkt.getRelayAgent();
                                        String clientMAC = pkt.getClientMAC();
                                        String remoteID = pkt.getOpt82RemoteID();
                                        Long relayAgent = null;
                                        try {
                                            relayAgent = NccUtils.ip2long(agentIP.getHostAddress());
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }

                                        NccDhcpBindData bindData = new NccDhcpBinding().getBinding(remoteID, clientMAC, relayAgent);

                                        if (bindData != null) {
                                            logger.debug("User binded: " + bindData.uid);
                                        } else {
                                            logger.debug("Unbinded user");
                                            new NccDhcpBinding().setUnbinded(remoteID, clientMAC, relayAgent);
                                            return;
                                        }

                                        try {

                                            NccDhcpRelayAgentData agentData = new NccDhcpRelayAgents().getAgentByIP(NccUtils.ip2long(agentIP.getHostAddress()));
                                            if (agentData != null) {
                                                NccDhcpPoolData poolData = new NccDhcpPools().getPool(agentData.agentPool);

                                                if (poolData != null) {
                                                    logger.debug("Found pool for relay agent: " + poolData.poolName);
                                                }

                                                NccDhcpLeaseData leaseData = new NccDhcpLeases().allocateLease(poolData, clientMAC, remoteID, NccUtils.ip2long(agentIP.getHostAddress()));

                                                if (leaseData != null) {
                                                    logger.debug("Offered IP: " + NccUtils.long2ip(leaseData.leaseIP));

                                                    InetAddress ip = InetAddress.getByName(NccUtils.long2ip(leaseData.leaseIP));
                                                    InetAddress netmask = InetAddress.getByName(NccUtils.long2ip(leaseData.leaseNetmask));
                                                    InetAddress router = InetAddress.getByName(NccUtils.long2ip(leaseData.leaseRouter));
                                                    InetAddress dns = InetAddress.getByName(NccUtils.long2ip(leaseData.leaseDNS1));

                                                    byte[] dhcpReply = pkt.buildReply(NccDhcpPacket.DHCP_MSG_TYPE_OFFER, ip, netmask, router, dns, poolData.poolLeaseTime);

                                                    DatagramPacket outPkt = new DatagramPacket(dhcpReply, dhcpReply.length, inPkt.getAddress(), inPkt.getPort());
                                                    try {
                                                        dhcpSocket.send(outPkt);
                                                    } catch (IOException e) {
                                                        logger.error("Can't write to socket");
                                                    }
                                                }
                                            }

                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_REQUEST) {

                                        logger.debug("DHCPREQUEST from " + inPkt.getAddress().toString() + " " + pkt.getOpt82RemoteID());

                                        InetAddress agentIP = pkt.getRelayAgent();
                                        String clientMAC = pkt.getClientMAC();
                                        String remoteID = pkt.getOpt82RemoteID();

                                        InetAddress clientIP = pkt.getClientIPAddress();
                                        InetAddress reqIP = pkt.getAddressRequest();

                                        byte[] dhcpReply;

                                        InetAddress ip = null;
                                        InetAddress netmask = null;
                                        InetAddress router = null;
                                        InetAddress dns = null;
                                        try {
                                            ip = InetAddress.getByName("0.0.0.0");
                                            netmask = InetAddress.getByName("0.0.0.0");
                                            router = InetAddress.getByName("0.0.0.0");
                                            dns = InetAddress.getByName("0.0.0.0");
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }

                                        NccDhcpLeaseData lease = null;

                                        if (!clientIP.getHostAddress().equals("0.0.0.0")) {     // renew lease

                                            logger.debug("Lease RENEW");

                                            try {
                                                lease = new NccDhcpLeases().acceptLease(NccUtils.ip2long(clientIP.getHostAddress()), clientMAC, remoteID);
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }

                                        } else if (reqIP != null) { // accept new lease

                                            logger.debug("Lease ACCEPT");

                                            try {
                                                lease = new NccDhcpLeases().acceptLease(NccUtils.ip2long(reqIP.getHostAddress()), clientMAC, remoteID);
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        if (lease != null) {

                                            try {
                                                ip = InetAddress.getByName(NccUtils.long2ip(lease.leaseIP));
                                                netmask = InetAddress.getByName(NccUtils.long2ip(lease.leaseNetmask));
                                                router = InetAddress.getByName(NccUtils.long2ip(lease.leaseRouter));
                                                dns = InetAddress.getByName(NccUtils.long2ip(lease.leaseDNS1));
                                                logger.debug("Leased IP: " + NccUtils.long2ip(lease.leaseIP));
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }

                                            NccDhcpPoolData poolData = new NccDhcpPools().getPool(lease.leasePool);

                                            dhcpReply = pkt.buildReply(NccDhcpPacket.DHCP_MSG_TYPE_ACK, ip, netmask, router, dns, poolData.poolLeaseTime);
                                        } else {
                                            logger.error("Lease not found for " + clientMAC);
                                            dhcpReply = pkt.buildReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, ip, netmask, router, dns, 300);
                                        }

                                        DatagramPacket outPkt = new DatagramPacket(dhcpReply, dhcpReply.length, inPkt.getAddress(), inPkt.getPort());
                                        try {
                                            dhcpSocket.send(outPkt);
                                        } catch (IOException e) {
                                            logger.error("Can't write to socket");
                                        }
                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_RELEASE) {

                                        logger.debug("DHCPRELEASE from " + inPkt.getAddress().toString() + " " + pkt.getOpt82RemoteID());
                                    }
                                } catch (
                                        NccDhcpException e
                                        )

                                {
                                    e.printStackTrace();
                                }
                            }
                        });

                        dhcpReceiveThread.start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        );

        dhcpMainThread.start();
    }
}
