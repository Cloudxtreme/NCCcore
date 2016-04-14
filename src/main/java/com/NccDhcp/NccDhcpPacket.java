package com.NccDhcp;

import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class NccDhcpPacket {
    private static Logger logger = Logger.getLogger(NccDhcpPacket.class);

    public byte[] dhcpMsgType;
    public byte[] dhcpHwType;
    public byte[] dhcpHwAddrLen;
    public byte[] dhcpHops;
    public byte[] dhcpTransID;
    public byte[] dhcpSecsElapsed;
    public byte[] dhcpBootpFlags;
    public byte[] dhcpClientIPAddress;
    public byte[] dhcpYourIPAddress;
    public byte[] dhcpNextServer;
    public byte[] dhcpRelayAgentIP;
    public byte[] dhcpClientMACAddress;
    public byte[] dhcpMagicCookie;
    public byte[] dhcpClientID;

    public byte[] dhcpOpt82CircuitID;
    public byte[] dhcpOpt82RemoteID;
    public byte[] dhcpOpt82VendorID;

    public byte[] replyYourIP;
    public byte[] replyClientIP;
    public byte[] replyNextServer;
    public byte replyMsgType;
    public byte[] replyServerID;
    public byte[] replyLeaseTime;
    public byte[] replySubnetMask;
    public byte[] replyRouter;
    public byte[] replyDNS;

    private static byte[] DHCP_MAGIC_COOKIE = {(byte) 0x63, (byte) 0x82, (byte) 0x53, (byte) 0x63};

    private static int DHCP_MSG_TYPE_OFFSET = 0;
    private static int DHCP_HW_TYPE_OFFSET = 1;
    private static int DHCP_HW_ADDR_LEN_OFFSET = 2;
    private static int DHCP_HOPS_OFFSET = 3;
    private static int DHCP_TRANS_ID_OFFSET = 4;
    private static int DHCP_SECS_ELAPSED_OFFSET = 8;
    private static int DHCP_BOOTP_FLAGS_OFFSET = 0x0A;
    private static int DHCP_CIADDR_OFFSET = 0x0C;
    private static int DHCP_YIADDR_OFFSET = 0x10;
    private static int DHCP_NEXT_SERVER_OFFSET = 0x14;
    private static int DHCP_RELAY_AGENT_OFFSET = 0x18;
    private static int DHCP_CLIENT_MAC_OFFSET = 0x1C;
    private static int DHCP_MAGIC_COOKIE_OFFSET = 0xEC;
    private static int DHCP_OPTIONS_OFFSET = 0xF0;

    private static int DHCP_OPTION_12_HOSTNAME = 12;
    private static int DHCP_OPTION_50_ADDRESS_REQ = 50;
    private static int DHCP_OPTION_53_MSG_TYPE = 53;
    private static int DHCP_OPTION_55_PARAM_LIST = 55;
    private static int DHCP_OPTION_60_CLASS_ID = 60;
    private static int DHCP_OPTION_61_CLIENT_ID = 61;
    private static int DHCP_OPTION_82_RELAY_AGENT = 82;
    private static int DHCP_OPTION_255 = 255;

    public static byte DHCP_MSG_TYPE_DISCOVER = 1;
    public static byte DHCP_MSG_TYPE_OFFER = 2;
    public static byte DHCP_MSG_TYPE_REQUEST = 3;
    public static byte DHCP_MSG_TYPE_DECLINE = 4;
    public static byte DHCP_MSG_TYPE_ACK = 5;
    public static byte DHCP_MSG_TYPE_NAK = 6;
    public static byte DHCP_MSG_TYPE_RELEASE = 7;
    public static byte DHCP_MSG_TYPE_INFORM = 8;

    private static int DHCP_RELAY_AGENT_CIRCUIT_ID = 1;
    private static int DHCP_RELAY_AGENT_REMOTE_ID = 2;
    private static int DHCP_RELAY_AGENT_VENDOR = 9;

    public NccDhcpPacket(byte[] data, int dataLen) throws NccDhcpException {

        if (dataLen < DHCP_OPTIONS_OFFSET) throw new NccDhcpException("Wrong DHCP packet len: " + dataLen);
        if (!baCompare(data, DHCP_MAGIC_COOKIE_OFFSET, DHCP_MAGIC_COOKIE))
            throw new NccDhcpException("Incorrect packet format, DHCP_MAGIC_COOKIE not found");

        this.dhcpMsgType = baSubstr(data, DHCP_MSG_TYPE_OFFSET, 1);
        this.dhcpHwType = baSubstr(data, DHCP_HW_TYPE_OFFSET, 1);
        this.dhcpHwAddrLen = baSubstr(data, DHCP_HW_ADDR_LEN_OFFSET, 1);
        this.dhcpHops = baSubstr(data, DHCP_HOPS_OFFSET, 1);
        this.dhcpTransID = baSubstr(data, DHCP_TRANS_ID_OFFSET, 4);
        this.dhcpSecsElapsed = baSubstr(data, DHCP_SECS_ELAPSED_OFFSET, 2);
        this.dhcpBootpFlags = baSubstr(data, DHCP_BOOTP_FLAGS_OFFSET, 2);
        this.dhcpClientIPAddress = baSubstr(data, DHCP_CIADDR_OFFSET, 4);
        this.dhcpYourIPAddress = baSubstr(data, DHCP_YIADDR_OFFSET, 4);
        this.dhcpNextServer = baSubstr(data, DHCP_NEXT_SERVER_OFFSET, 4);
        this.dhcpRelayAgentIP = baSubstr(data, DHCP_RELAY_AGENT_OFFSET, 4);
        this.dhcpClientMACAddress = baSubstr(data, DHCP_CLIENT_MAC_OFFSET, 6);
        this.dhcpMagicCookie = baSubstr(data, DHCP_MAGIC_COOKIE_OFFSET, 4);

        for (int i = DHCP_OPTIONS_OFFSET; i < dataLen; i++) {
            if (data[i] == DHCP_OPTION_53_MSG_TYPE) {
                i++;
                Integer len = ba2int(baSubstr(data, i, 1));
                if (len < 1) break;
                i++;
                this.dhcpMsgType = baSubstr(data, i, 1);
                i += len - 1;
                continue;
            }

            if (data[i] == DHCP_OPTION_61_CLIENT_ID) {
                i++;
                Integer len = ba2int(baSubstr(data, i, 1));
                if (len < 1) break;
                i++;
                this.dhcpClientID = baSubstr(data, i, len);
                i += len - 1;
                continue;
            }

            if (data[i] == DHCP_OPTION_12_HOSTNAME) {
                i++;
                Integer len = ba2int(baSubstr(data, i, 1));
                if (len < 1) break;
                i++;
                i += len - 1;
                continue;
            }

            if (data[i] == DHCP_OPTION_60_CLASS_ID) {
                i++;
                Integer len = ba2int(baSubstr(data, i, 1));
                if (len < 1) break;
                i++;
                i += len - 1;
                continue;
            }

            if (data[i] == DHCP_OPTION_55_PARAM_LIST) {
                i++;
                Integer len = ba2int(baSubstr(data, i, 1));
                if (len < 1) break;
                i++;
                i += len - 1;
                continue;
            }

            if (data[i] == DHCP_OPTION_50_ADDRESS_REQ) {
                i++;
                Integer len = ba2int(baSubstr(data, i, 1));
                if (len < 1) break;
                i++;
                i += len - 1;
                continue;
            }

            if (data[i] == DHCP_OPTION_82_RELAY_AGENT) {
                i++;
                Integer len = ba2int(baSubstr(data, i, 1));
                if (len < 1) break;
                i++;
                for (int j = i; j < i + len; j++) {
                    if (data[j] == DHCP_RELAY_AGENT_CIRCUIT_ID) {
                        j++;
                        int slen = ba2int(baSubstr(data, j, 1));
                        if (slen < 1) break;
                        j++;
                        this.dhcpOpt82CircuitID = baSubstr(data, j, slen);
                        j += slen - 1;
                        continue;
                    }

                    if (data[j] == DHCP_RELAY_AGENT_REMOTE_ID) {
                        j++;
                        int slen = ba2int(baSubstr(data, j, 1));
                        if (slen < 1) break;
                        j++;
                        this.dhcpOpt82RemoteID = baSubstr(data, j, slen);
                        j += slen - 1;
                        continue;
                    }

                    if (data[j] == DHCP_RELAY_AGENT_VENDOR) {
                        j++;
                        int slen = ba2int(baSubstr(data, j, 1));
                        if (slen < 1) break;
                        j++;
                        this.dhcpOpt82VendorID = baSubstr(data, j, slen);
                        j += slen - 1;
                        continue;
                    }
                }
                i += len - 1;
                continue;
            }

        }
    }

    private boolean baCompare(byte[] data, int start, byte[] clue) {
        for (int i = start; i < clue.length; i++) {
            if (data[i] != clue[start - i]) return false;
        }
        return true;
    }

    private byte[] baSubstr(byte[] data, int start, int len) {
        byte[] res = new byte[len];
        Integer p = 0;

        for (Integer i = start; i < start + len; i++) {
            res[p] = data[i];
            p++;
        }


        return res;
    }

    private byte[] baInsert(byte[] data, int start, byte[] substr) {
        byte[] res = data;

        for (int i = start; i < start + substr.length; i++) {
            res[i] = substr[i - start];
        }

        return res;
    }

    private int ba2int(byte[] b) {

        if (b.length == 1) return b[0];

        if (b.length == 2) return b[1] & 0xFF |
                (b[0] & 0xFF) << 8;

        if (b.length == 4) return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;

        return 0;
    }

    int getType() {
        int type = ba2int(this.dhcpMsgType);

        return type;
    }

    String getOpt82RemoteID() {

        if (this.dhcpOpt82RemoteID != null) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < this.dhcpOpt82RemoteID.length; i++) {
                sb.append(String.format("%02X%s", this.dhcpOpt82RemoteID[i], (i < this.dhcpOpt82RemoteID.length - 1) ? ":" : ""));
            }

            return sb.toString();
        }

        return "";
    }

    byte[] buildReply(byte type, InetAddress clientIP, InetAddress netmask, InetAddress gateway, InetAddress dns, int leaseTime) {

        byte[] data = new byte[332];

        data[0] = 0x02;
        data[1] = 0x01;
        data[2] = 0x06;
        data[3] = 0x00;

        byte[] bootpFlags = {0, 0};
        byte[] zaddr = {0, 0, 0, 0};
        byte[] padding10 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] opt53 = {53, 1, type};
        byte[] opt54 = {54, 4};
        byte[] opt51 = {51, 4};
        byte[] opt1 = {1, 4};
        byte[] opt3 = {3, 4};
        byte[] opt6 = {6, 4};
        byte[] opt255 = {(byte) 0xff};

        data = baInsert(data, 4, this.dhcpTransID);
        data = baInsert(data, 8, this.dhcpSecsElapsed);
        data = baInsert(data, 10, bootpFlags);
        data = baInsert(data, 12, zaddr);
        data = baInsert(data, 16, clientIP.getAddress());
        data = baInsert(data, 20, zaddr);
        data = baInsert(data, 24, this.dhcpRelayAgentIP);
        data = baInsert(data, 28, this.dhcpClientMACAddress);
        data = baInsert(data, 34, padding10);
        data = baInsert(data, 236, DHCP_MAGIC_COOKIE);
        data = baInsert(data, 240, opt53);
        data = baInsert(data, 243, opt54);
        data = baInsert(data, 245, gateway.getAddress());
        data = baInsert(data, 249, opt51);
        data = baInsert(data, 251, ByteBuffer.allocate(4).putInt(leaseTime).array());
        data = baInsert(data, 255, opt1);
        data = baInsert(data, 257, netmask.getAddress());
        data = baInsert(data, 261, opt3);
        data = baInsert(data, 263, gateway.getAddress());
        data = baInsert(data, 267, opt6);
        data = baInsert(data, 269, dns.getAddress());
        data = baInsert(data, 273, opt255);

        return data;
    }
}
