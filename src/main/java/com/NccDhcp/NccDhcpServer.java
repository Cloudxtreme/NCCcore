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

                            DatagramPacket sendReply(byte type, Long localIP, Long leaseIP, Long leaseNetmask, Long leaseRouter, Long leaseDNS1, int leaseTime) {
                                NccDhcpPacket pkt = null;
                                try {
                                    pkt = new NccDhcpPacket(recv, inPkt.getLength());
                                } catch (NccDhcpException e) {
                                    e.printStackTrace();
                                }

                                InetAddress ip = null;
                                InetAddress netmask = null;
                                InetAddress router = null;
                                InetAddress dns = null;
                                try {
                                    ip = InetAddress.getByName(NccUtils.long2ip(leaseIP));
                                    netmask = InetAddress.getByName(NccUtils.long2ip(leaseNetmask));
                                    router = InetAddress.getByName(NccUtils.long2ip(leaseRouter));
                                    dns = InetAddress.getByName(NccUtils.long2ip(leaseDNS1));
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }

                                byte[] dhcpReply = null;

                                try {
                                    dhcpReply = pkt.buildReply(type, InetAddress.getByName(NccUtils.long2ip(localIP)), ip, netmask, router, dns, leaseTime);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }

                                logger.debug("Send " + pkt.type2string(type) + " to " + inPkt.getAddress().getHostAddress() + ":" + inPkt.getPort());

                                try {
                                    DatagramPacket outPkt = new DatagramPacket(dhcpReply, dhcpReply.length, inPkt.getAddress(), 67);
                                    dhcpSocket.send(outPkt);

                                    return outPkt;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }

                            @Override
                            public void run() {

                                try {
                                    NccDhcpPacket pkt = new NccDhcpPacket(recv, inPkt.getLength());

                                    Long nullIP = null;
                                    try {
                                        nullIP = NccUtils.ip2long("0.0.0.0");
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_DISCOVER) {

                                        logger.debug("DHCPDISCOVER from " + inPkt.getAddress().toString());

                                        logger.debug("RelayAgent: " + pkt.getRelayAgent().getHostAddress() + " remoteID: " + pkt.getOpt82RemoteID() + " circuitID: " + pkt.getOpt82CircuitID() + " clientID: " + pkt.getClientID());

                                        // TODO: 4/20/16 set real local IP of outgoing iface
                                        InetAddress localIP = null;
                                        try {
                                            localIP = InetAddress.getByName("151.0.48.86");
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
                                            logger.debug("Empty remoteID in DHCPDISCOVER");
                                            try {
                                                sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }
                                            return;
                                        }

                                        NccDhcpBindData bindData = checkBind(remoteID, circuitID, clientMAC, relayAgent);
                                        if (bindData == null) {
                                            logger.error("User not binded in DHCPDISCOVER");
                                            try {
                                                sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }
                                            return;
                                        }

                                        NccDhcpLeaseData leaseData = new NccDhcpLeases().getLeaseByMAC(relayAgent, clientMAC);

                                        if (leaseData != null) {

                                            logger.debug("Found lease for " + relayAgent.toString() + " " + clientMAC);

                                            NccDhcpPoolData poolData = new NccDhcpPools().getPool(leaseData.leasePool);

                                            try {
                                                sendReply(NccDhcpPacket.DHCP_MSG_TYPE_OFFER, NccUtils.ip2long(localIP.getHostAddress()), leaseData.leaseIP, leaseData.leaseNetmask, leaseData.leaseRouter, leaseData.leaseDNS1, poolData.poolLeaseTime);
                                                return;
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }
                                        } else {

                                            try {
                                                NccDhcpRelayAgentData agentData = new NccDhcpRelayAgents().getAgentByIP(NccUtils.ip2long(agentIP.getHostAddress()));

                                                if (agentData != null) {
                                                    NccDhcpPoolData poolData = new NccDhcpPools().getPool(agentData.agentPool);

                                                    if (poolData != null) {
                                                        leaseData = new NccDhcpLeases().allocateLease(bindData.uid, poolData, clientMAC, remoteID, circuitID, NccUtils.ip2long(agentIP.getHostAddress()));

                                                        if (leaseData != null) {
                                                            sendReply(NccDhcpPacket.DHCP_MSG_TYPE_OFFER, NccUtils.ip2long(localIP.getHostAddress()), leaseData.leaseIP, leaseData.leaseNetmask, leaseData.leaseRouter, leaseData.leaseDNS1, poolData.poolLeaseTime);
                                                            new NccDhcpLeases().renewLease(leaseData);
                                                            return;
                                                        } else {
                                                            logger.error("Can't allocate lease");
                                                            sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                                            return;
                                                        }
                                                    } else {
                                                        logger.error("Pool for relay agent " + NccUtils.long2ip(relayAgent) + " not found");
                                                        sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                                        return;
                                                    }
                                                } else {
                                                    logger.debug("Relay agent " + NccUtils.long2ip(relayAgent) + " not found");
                                                }
                                                sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                                return;
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_REQUEST) {

                                        logger.debug("DHCPREQUEST from " + inPkt.getAddress().toString());

                                        logger.debug("RelayAgent: " + pkt.getRelayAgent().getHostAddress() + " remoteID: " + pkt.getOpt82RemoteID() + " circuitID: " + pkt.getOpt82CircuitID() + " clientID: " + pkt.getClientID());

                                        InetAddress agentIP = pkt.getRelayAgent();
                                        String clientMAC = pkt.getClientMAC();
                                        String remoteID = pkt.getOpt82RemoteID();
                                        String circuitID = pkt.getOpt82CircuitID();

                                        if (remoteID.equals("")) {
                                            logger.debug("Empty remoteID");
                                        }
                                        // TODO: 4/20/16 set real local IP of outgoing iface
                                        InetAddress localIP = null;
                                        try {
                                            localIP = InetAddress.getByName("151.0.48.86");
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }

                                        Long relayAgent = null;
                                        try {
                                            relayAgent = NccUtils.ip2long(agentIP.getHostAddress());
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }

                                        InetAddress clientIP = pkt.getClientIPAddress();
                                        InetAddress reqIP = pkt.getAddressRequest();

                                        NccDhcpLeaseData leaseData = null;

                                        if (!clientIP.getHostAddress().equals("0.0.0.0")) {     // renew lease

                                            logger.debug("Lease RENEW");

                                            leaseData = new NccDhcpLeases().getLeaseByMAC(relayAgent, clientMAC);

                                            if (leaseData != null) {

                                                try {
                                                    logger.debug("Found lease for " + NccUtils.long2ip(relayAgent) + " " + clientMAC);
                                                } catch (UnknownHostException e) {
                                                    e.printStackTrace();e.printStackTrace();
                                                }

                                                NccDhcpPoolData poolData = new NccDhcpPools().getPool(leaseData.leasePool);

                                                if (poolData != null) {
                                                    try {
                                                        new NccDhcpLeases().renewLease(leaseData);
                                                        sendReply(NccDhcpPacket.DHCP_MSG_TYPE_ACK, NccUtils.ip2long(localIP.getHostAddress()), leaseData.leaseIP, leaseData.leaseNetmask, leaseData.leaseRouter, leaseData.leaseDNS1, poolData.poolLeaseTime);
                                                        return;
                                                    } catch (UnknownHostException e) {
                                                        e.printStackTrace();
                                                    }

                                                } else {
                                                    logger.error("Pool " + leaseData.leasePool + " not found");
                                                    try {
                                                        sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                                        return;
                                                    } catch (UnknownHostException e) {
                                                        e.printStackTrace();
                                                    }
                                                    return;
                                                }

                                            } else {
                                                logger.error("Lease for " + clientMAC + "not found");
                                                try {
                                                    sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                                    return;
                                                } catch (UnknownHostException e) {
                                                    e.printStackTrace();
                                                }
                                                return;
                                            }
                                        } else if (reqIP != null) { // accept new lease

                                            logger.debug("Lease ACCEPT");

                                            if (remoteID.equals("")) {
                                                logger.debug("Empty remoteID in DHCPREQUEST");
                                                try {
                                                    sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                                } catch (UnknownHostException e) {
                                                    e.printStackTrace();
                                                }
                                                return;
                                            }

                                            NccDhcpBindData bindData = checkBind(remoteID, circuitID, clientMAC, relayAgent);
                                            if (bindData == null) {
                                                logger.error("User not binded in DHCPREQUEST");
                                                try {
                                                    sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                                } catch (UnknownHostException e) {
                                                    e.printStackTrace();
                                                }
                                                return;
                                            }

                                            try {
                                                leaseData = new NccDhcpLeases().acceptLease(NccUtils.ip2long(reqIP.getHostAddress()), clientMAC, remoteID, circuitID);

                                                if (leaseData != null) {

                                                    NccDhcpPoolData poolData = new NccDhcpPools().getPool(leaseData.leasePool);

                                                    try {
                                                        sendReply(NccDhcpPacket.DHCP_MSG_TYPE_ACK, NccUtils.ip2long(localIP.getHostAddress()), leaseData.leaseIP, leaseData.leaseNetmask, leaseData.leaseRouter, leaseData.leaseDNS1, poolData.poolLeaseTime);
                                                        return;
                                                    } catch (UnknownHostException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    logger.error("Lease not found for " + clientMAC);
                                                    try {
                                                        sendReply(NccDhcpPacket.DHCP_MSG_TYPE_NAK, NccUtils.ip2long(localIP.getHostAddress()), nullIP, nullIP, nullIP, nullIP, 0);
                                                        return;
                                                    } catch (UnknownHostException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_RELEASE) {

                                        logger.debug("DHCPRELEASE from " + inPkt.getAddress().toString());
                                        logger.debug("RelayAgent: " + pkt.getRelayAgent().getHostAddress() + " remoteID: " + pkt.getOpt82RemoteID() + " circuitID: " + pkt.getOpt82CircuitID() + " clientID: " + pkt.getClientID());
                                    }
                                } catch (NccDhcpException e) {
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
