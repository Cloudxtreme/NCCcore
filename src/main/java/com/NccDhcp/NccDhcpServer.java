package com.NccDhcp;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class NccDhcpServer {

    private byte[] byteSubstr(byte[] data, int start, int len) {
        byte[] res = new byte[len];
        Integer p = 0;

        for (Integer i = start; i < start + len; i++) {
            res[p] = data[i];
            p++;
        }


        return res;
    }

    private byte[] substr2ba(byte[] data, int start, byte[] substr) {
        byte[] res = data;

        for (int i = start; i < start + substr.length; i++) {
            res[i] = substr[i - start];
        }

        return res;
    }

    private int ba2int(byte[] b) {

        if (b.length == 1) return b[0];

        if (b.length == 4) return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;

        return 0;
    }

    private NccDhcpPacket getDhcpPacket(byte[] data, int dataLen) {
        NccDhcpPacket pkt = new NccDhcpPacket();

        pkt.dhcpMsgType = byteSubstr(data, 0, 1);
        pkt.dhcpHwType = byteSubstr(data, 1, 1);
        pkt.dhcpHwAddrLen = byteSubstr(data, 2, 1);
        pkt.dhcpHops = byteSubstr(data, 3, 1);
        pkt.dhcpTransID = byteSubstr(data, 4, 4);
        pkt.dhcpSecsElapsed = byteSubstr(data, 8, 2);
        pkt.dhcpBootpFlags = byteSubstr(data, 0x34 - 0x2a, 2);
        pkt.dhcpClientIPAddress = byteSubstr(data, 0x36 - 0x2a, 4);
        pkt.dhcpYourIPAddress = byteSubstr(data, 0x3a - 0x2a, 4);
        pkt.dhcpNextServer = byteSubstr(data, 0x3e - 0x2a, 4);
        pkt.dhcpRelayAgentIP = byteSubstr(data, 0x42 - 0x2a, 4);
        pkt.dhcpClientMACAddress = byteSubstr(data, 0x46 - 0x2a, 6);
        pkt.dhcpMagicCookie = byteSubstr(data, 0x116 - 0x2a, 4);

        for (int i = 0x011a - 0x2a; i < dataLen; i++) {
            if (data[i] == 53) {
                i++;
                Integer len = ba2int(byteSubstr(data, i, 1));
//                System.out.println("OPT53: len=" + len);
                if (len < 1) break;
                i++;
                pkt.dhcpMsgType = byteSubstr(data, i, 1);
//                System.out.println("OPT53: MsgType=" + ba2int(pkt.dhcpMsgType));
                i += len;
            }

            if (data[i] == 61) {
                i++;
                Integer len = ba2int(byteSubstr(data, i, 1));
                if (len < 1) break;
                i++;
//                System.out.println("OPT61: len=" + len);
                i += len;
            }

            if (data[i] == 12) {
                i++;
                Integer len = ba2int(byteSubstr(data, i, 1));
                if (len < 1) break;
                i++;
//                System.out.println("OPT12: len=" + len);
                i += len;
            }

            if (data[i] == 60) {
                i++;
                Integer len = ba2int(byteSubstr(data, i, 1));
                if (len < 1) break;
                i++;
//                System.out.println("OPT60: len=" + len);
                i += len;
            }

            if (data[i] == 55) {
                i++;
                Integer len = ba2int(byteSubstr(data, i, 1));
                if (len < 1) break;
                i++;
//                System.out.println("OPT55: len=" + len);
                i += len;
            }

            if (data[i] == 82) {
                i++;
                Integer len = ba2int(byteSubstr(data, i, 1));
                if (len < 1) break;
                i++;
//                System.out.println("OPT82: len=" + len);
                for (int j = i; j < i + len; j++) {
                    if (data[j] == 1) {
                        j++;
                        int slen = ba2int(byteSubstr(data, j, 1));
//                        System.out.println("OPT82 sub 1 len=" + slen);
                        if (slen < 1) break;
                        j++;
                        pkt.dhcpOpt82CircuitID = byteSubstr(data, j, slen);
                        j += slen;
                    }

                    if (data[j] == 2) {
                        j++;
                        int slen = ba2int(byteSubstr(data, j, 1));
//                        System.out.println("OPT82 sub 2 len=" + slen);
                        if (slen < 1) break;
                        j++;
                        pkt.dhcpOpt82RemoteID = byteSubstr(data, j, slen);
                        j += slen;
                    }

                    if (data[j] == 9) {
                        j++;
                        int slen = ba2int(byteSubstr(data, j, 1));
//                        System.out.println("OPT82 sub 6 len=" + slen);
                        if (slen < 1) break;
                        j++;
                        pkt.dhcpOpt82VendorID = byteSubstr(data, j, slen);
                        j += slen;
                    }
                }
                i += len;
            }

        }

        if (!DatatypeConverter.printHexBinary(pkt.dhcpMagicCookie).equals("63825363")) return null;

        return pkt;
    }

    private byte[] buildDhcpReply(NccDhcpPacket pkt) {
        byte[] data = new byte[332];

        data[0] = 0x02;
        data[1] = 0x01;
        data[2] = 0x06;
        data[3] = 0x00;

        byte[] bootpFlags = {0, 0};
        byte[] padding10 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] magic = {0x63, (byte) 0x82, 0x53, 0x63};
        byte[] opt53 = {53, 1, pkt.replyMsgType};
        byte[] opt54 = {54, 4};
        byte[] opt51 = {51, 4};
        byte[] opt1 = {1, 4};
        byte[] opt3 = {3, 4};
        byte[] opt6 = {6, 4};
        byte[] opt255 = {(byte) 0xff};

        data = substr2ba(data, 4, pkt.dhcpTransID);
        data = substr2ba(data, 8, pkt.dhcpSecsElapsed);
        data = substr2ba(data, 10, bootpFlags);
        data = substr2ba(data, 12, pkt.replyClientIP);
        data = substr2ba(data, 16, pkt.replyYourIP);
        data = substr2ba(data, 20, pkt.replyNextServer);
        data = substr2ba(data, 24, pkt.dhcpRelayAgentIP);
        data = substr2ba(data, 28, pkt.dhcpClientMACAddress);
        data = substr2ba(data, 34, padding10);
        data = substr2ba(data, 236, magic);
        data = substr2ba(data, 240, opt53);
        data = substr2ba(data, 243, opt54);
        data = substr2ba(data, 245, pkt.replyServerID);
        data = substr2ba(data, 249, opt51);
        data = substr2ba(data, 251, pkt.replyLeaseTime);
        data = substr2ba(data, 255, opt1);
        data = substr2ba(data, 257, pkt.replySubnetMask);
        data = substr2ba(data, 261, opt3);
        data = substr2ba(data, 263, pkt.replyRouter);
        data = substr2ba(data, 267, opt6);
        data = substr2ba(data, 269, pkt.replyDNS);
        data = substr2ba(data, 273, opt255);

        return data;
    }

    public void start() {
        try {
            DatagramSocket sock = new DatagramSocket(67);

            byte[] recv = new byte[1024];

            while (true) {
                try {
                    DatagramPacket inPkt = new DatagramPacket(recv, recv.length);
                    sock.receive(inPkt);

                    System.out.println("RECVd: " + inPkt.getLength() + " offset: " + inPkt.getOffset());

                    NccDhcpPacket pkt = getDhcpPacket(recv, inPkt.getLength());

                    if (pkt == null) {
                        System.out.println("Wrong packet received");
                    } else {
                        System.out.println("DHCP packet msgType=" + ba2int(pkt.dhcpMsgType));

                        if (ba2int(pkt.dhcpMsgType) == 1) {

                            System.out.println("DHCP OFFER");

                            pkt.dhcpMsgType = new byte[]{2};
                            pkt.replyMsgType = 2;
                            pkt.replyClientIP = new byte[]{0, 0, 0, 0};
                            pkt.replyYourIP = new byte[]{(byte) 0xac, (byte) 0x11, (byte) 0x00, (byte) 0x30};
                            pkt.replyNextServer = new byte[]{0, 0, 0, 0};
                            //pkt.replyServerID = new byte[]{0x5d, (byte) 0xaa, 0x30, 0x08};
                            pkt.replyServerID = new byte[]{(byte) 0xac, (byte) 0x11, (byte) 0x00, (byte) 0x01};
                            pkt.replySubnetMask = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, 0x00};
                            pkt.replyRouter = new byte[]{(byte) 0xac, (byte) 0x11, (byte) 0x00, (byte) 0x01};
                            pkt.replyDNS = new byte[]{(byte) 0x97, (byte) 0x00, 0x30, 0x3b};
                            pkt.replyLeaseTime = new byte[]{0, 0, 0x01, 0x2c};
                            byte[] reply = buildDhcpReply(pkt);

                            DatagramPacket outPkt = new DatagramPacket(reply, reply.length, inPkt.getAddress(), inPkt.getPort());

                            sock.send(outPkt);
                        }

                        if (ba2int(pkt.dhcpMsgType) == 3) {

                            System.out.println("DHCP ACK");

                            pkt.dhcpMsgType = new byte[]{5};
                            pkt.replyMsgType = 5;
                            //pkt.dhcpRelayAgentIP = new byte[]{0, 0, 0, 0};
                            pkt.replyClientIP = new byte[]{0, 0, 0, 0};
                            pkt.replyYourIP = new byte[]{(byte) 0xac, (byte) 0x11, (byte) 0x00, (byte) 0x30};
                            pkt.replyNextServer = new byte[]{0, 0, 0, 0};
                            //pkt.replyServerID = new byte[]{0x5d, (byte) 0xaa, 0x30, 0x08};
                            pkt.replyServerID = new byte[]{(byte) 0xac, (byte) 0x11, (byte) 0x00, (byte) 0x01};
                            pkt.replySubnetMask = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0x00, 0x00};
                            pkt.replyRouter = new byte[]{(byte) 0xac, (byte) 0x11, (byte) 0x00, (byte) 0x01};
                            pkt.replyDNS = new byte[]{(byte) 0x97, (byte) 0x00, 0x30, 0x3b};
                            pkt.replyLeaseTime = new byte[]{0, 0, 0x01, 0x2c};
                            byte[] reply = buildDhcpReply(pkt);

                            DatagramPacket outPkt = new DatagramPacket(reply, reply.length, inPkt.getAddress(), inPkt.getPort());

                            sock.send(outPkt);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
