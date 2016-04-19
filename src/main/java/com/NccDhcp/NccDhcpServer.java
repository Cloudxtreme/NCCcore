package com.NccDhcp;

import com.NccSystem.NccUtils;
import com.google.common.net.InetAddresses;
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
                        NccDhcpBinding binding = new NccDhcpBinding();
                        leases.cleanupLeases();
                        binding.cleanupBinding();
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

                            NccDhcpBindData checkBind(String remoteID, String circuitID, String clientMAC, Long relayAgent) {
                                NccDhcpBindData bindData = new NccDhcpBinding().getBinding(remoteID, circuitID, clientMAC, relayAgent);

                                if (bindData != null) {
                                    logger.debug("User binded: uid=" + bindData.uid);
                                    return bindData;
                                } else {
                                    try {
                                        logger.debug("Unbinded user: remoteID=" + remoteID + " circuitID=" + circuitID + " clientMAC=" + clientMAC + " relayAgent=" + NccUtils.long2ip(relayAgent));
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }
                                    if (!remoteID.equals(""))
                                        new NccDhcpBinding().setUnbinded(remoteID, circuitID, clientMAC, relayAgent);
                                    return null;
                                }
                            }

                            @Override
                            public void run() {

                                try {
                                    NccDhcpPacket pkt = new NccDhcpPacket(recv, inPkt.getLength());

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_DISCOVER) {

                                        logger.debug("DHCPDISCOVER from " + inPkt.getAddress().toString());

                                        logger.debug("RelayAgent: " + pkt.getRelayAgent().getHostAddress() + " remoteID: " + pkt.getOpt82RemoteID() + " circuitID: " + pkt.getOpt82CircuitID() + " clientID: " + pkt.getClientID());

                                        InetAddress localIP = null;
                                        try {
                                            localIP = InetAddress.getByName("93.170.48.8");
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }
                                        InetAddress agentIP = pkt.getRelayAgent();
                                        String clientMAC = pkt.getClientMAC();
                                        String remoteID = pkt.getOpt82RemoteID();
                                        String circuitID = pkt.getOpt82CircuitID();
                                        Long relayAgent = null;
                                        try {
                                            relayAgent = NccUtils.ip2long(agentIP.getHostAddress());
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }

                                        if (remoteID.equals("")) {
                                            logger.debug("Empty remoteID");
                                            return;
                                        }

                                        NccDhcpBindData bindData = checkBind(remoteID, circuitID, clientMAC, relayAgent);
                                        if (bindData == null) return;

                                        try {

                                            NccDhcpRelayAgentData agentData = new NccDhcpRelayAgents().getAgentByIP(NccUtils.ip2long(agentIP.getHostAddress()));
                                            if (agentData != null) {
                                                NccDhcpPoolData poolData = new NccDhcpPools().getPool(agentData.agentPool);

                                                if (poolData != null) {
                                                    logger.debug("Found pool for relay agent: " + poolData.poolName);
                                                }

                                                NccDhcpLeaseData leaseData = new NccDhcpLeases().allocateLease(bindData.uid, poolData, clientMAC, remoteID, circuitID, NccUtils.ip2long(agentIP.getHostAddress()));

                                                if (leaseData != null) {
                                                    logger.debug("Offered IP: " + NccUtils.long2ip(leaseData.leaseIP));

                                                    InetAddress ip = InetAddress.getByName(NccUtils.long2ip(leaseData.leaseIP));
                                                    InetAddress netmask = InetAddress.getByName(NccUtils.long2ip(leaseData.leaseNetmask));
                                                    InetAddress router = InetAddress.getByName(NccUtils.long2ip(leaseData.leaseRouter));
                                                    InetAddress dns = InetAddress.getByName(NccUtils.long2ip(leaseData.leaseDNS1));

                                                    byte[] dhcpReply = pkt.buildReply(NccDhcpPacket.DHCP_MSG_TYPE_OFFER, localIP, ip, netmask, router, dns, poolData.poolLeaseTime);

                                                    logger.debug("Send DHCPOFFER to " + inPkt.getAddress().getHostAddress() + ":" + inPkt.getPort());
                                                    DatagramPacket outPkt = new DatagramPacket(dhcpReply, dhcpReply.length, inPkt.getAddress(), 67);
                                                    try {
                                                        dhcpSocket.send(outPkt);
                                                    } catch (IOException e) {
                                                        logger.error("Can't write to socket");
                                                    }
                                                }
                                            } else {
                                                logger.debug("Pool for relay agent " + NccUtils.long2ip(relayAgent) + " not found");
                                            }

                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_REQUEST) {

                                        logger.debug("DHCPREQUEST from " + inPkt.getAddress().toString());

                                        logger.debug("RelayAgent: " + pkt.getRelayAgent().getHostAddress() + " remoteID: " + pkt.getOpt82RemoteID() + " circuitID: " + pkt.getOpt82CircuitID() + " clientID: " + pkt.getClientID());

                                        InetAddress agentIP = pkt.getRelayAgent();
                                        String clientMAC = pkt.getClientMAC();
                                        String remoteID = pkt.getOpt82RemoteID();
                                        String circuitID = pkt.getOpt82CircuitID();
                                        InetAddress localIP = null;
                                        try {
                                            localIP = InetAddress.getByName("93.170.48.8");
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }
                                        Long relayAgent = null;
                                        try {
                                            relayAgent = NccUtils.ip2long(agentIP.getHostAddress());
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }

                                        if (remoteID.equals("")) {
                                            logger.debug("Empty remoteID");
                                            return;
                                        }

                                        NccDhcpBindData bindData = checkBind(remoteID, circuitID, clientMAC, relayAgent);
                                        if (bindData == null) return;

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
                                                lease = new NccDhcpLeases().acceptLease(NccUtils.ip2long(clientIP.getHostAddress()), clientMAC, remoteID, circuitID);
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }

                                        } else if (reqIP != null) { // accept new lease

                                            logger.debug("Lease ACCEPT");

                                            try {
                                                lease = new NccDhcpLeases().acceptLease(NccUtils.ip2long(reqIP.getHostAddress()), clientMAC, remoteID, circuitID);
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

                                            dhcpReply = pkt.buildReply(NccDhcpPacket.DHCP_MSG_TYPE_ACK, localIP, ip, netmask, router, dns, poolData.poolLeaseTime);
                                        } else {
                                            logger.error("Lease not found for " + clientMAC);
                                            dhcpReply = pkt.buildReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, localIP, ip, netmask, router, dns, 300);
                                        }

                                        logger.debug("Send DHCPACK/NAK to " + inPkt.getAddress().getHostAddress() + ":" + inPkt.getPort());
                                        DatagramPacket outPkt = new DatagramPacket(dhcpReply, dhcpReply.length, inPkt.getAddress(), 67);
                                        try {
                                            dhcpSocket.send(outPkt);
                                        } catch (IOException e) {
                                            logger.error("Can't write to socket");
                                        }
                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_RELEASE) {

                                        logger.debug("DHCPRELEASE from " + inPkt.getAddress().toString());
                                        logger.debug("RelayAgent: " + pkt.getRelayAgent().getHostAddress() + " remoteID: " + pkt.getOpt82RemoteID() + " circuitID: " + pkt.getOpt82CircuitID() + " clientID: " + pkt.getClientID());
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
