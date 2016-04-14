package com.NccDhcp;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;

public class NccDhcpServer {
    private static Logger logger = Logger.getLogger(NccDhcp.class);

    private static DatagramSocket dhcpSocket;

    public NccDhcpServer() {
        try {
            this.dhcpSocket = new DatagramSocket(67);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() {
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

                                NccDhcpPacket pkt = new NccDhcpPacket(recv, inPkt.getLength());

                                if (pkt == null) {
                                    System.out.println("Wrong packet received");
                                } else {
                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_DISCOVER) {

                                        logger.debug("DHCPDISCOVER from " + inPkt.getAddress().toString());

                                        try {
                                            InetAddress ip = InetAddress.getByName("172.17.0.54");
                                            InetAddress netmask = InetAddress.getByName("255.255.0.0");
                                            InetAddress gateway = InetAddress.getByName("172.17.0.1");
                                            InetAddress dns = InetAddress.getByName("151.0.48.67");

                                            byte[] dhcpReply = pkt.buildReply(NccDhcpPacket.DHCP_MSG_TYPE_OFFER, ip, netmask, gateway, dns, 300);

                                            DatagramPacket outPkt = new DatagramPacket(dhcpReply, dhcpReply.length, inPkt.getAddress(), inPkt.getPort());
                                            try {
                                                dhcpSocket.send(outPkt);
                                            } catch (IOException e) {
                                                logger.error("Can't write to socket");
                                            }
                                        } catch (UnknownHostException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_REQUEST) {

                                        logger.debug("DHCPREQUEST from " + inPkt.getAddress().toString());

                                        try {
                                            InetAddress ip = InetAddress.getByName("172.17.0.54");
                                            InetAddress netmask = InetAddress.getByName("255.255.0.0");
                                            InetAddress gateway = InetAddress.getByName("172.17.0.1");
                                            InetAddress dns = InetAddress.getByName("151.0.48.67");

                                            byte[] dhcpReply = pkt.buildReply(NccDhcpPacket.DHCP_MSG_TYPE_ACK, ip, netmask, gateway, dns, 300);

                                            DatagramPacket outPkt = new DatagramPacket(dhcpReply, dhcpReply.length, inPkt.getAddress(), inPkt.getPort());
                                            try {
                                                dhcpSocket.send(outPkt);
                                            } catch (IOException e) {
                                                logger.error("Can't write to socket");
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (pkt.getType() == NccDhcpPacket.DHCP_MSG_TYPE_RELEASE) {

                                        logger.debug("DHCPRELEASE from " + inPkt.getAddress().toString());
                                    }
                                }
                            }
                        });

                        dhcpReceiveThread.start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        dhcpMainThread.start();
    }
}
