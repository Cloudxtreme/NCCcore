package com.NccDhcp;

import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    public byte[] dhcpClientAddressRequest;
    public byte[] dhcpRawOpt57;
    public byte[] dhcpRawOpt60;
    public byte[] dhcpRawOpt61;
    public byte[] dhcpRawOpt82;

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
    private static int DHCP_OPTION_43_VENDOR_SPECIFIC = 43;
    private static int DHCP_OPTION_50_ADDRESS_REQ = 50;
    private static int DHCP_OPTION_53_MSG_TYPE = 53;
    private static int DHCP_OPTION_55_PARAM_LIST = 55;
    private static int DHCP_OPTION_60_CLASS_ID = 60;
    private static int DHCP_OPTION_61_CLIENT_ID = 61;
    private static int DHCP_OPTION_82_RELAY_AGENT = 82;
    private static int DHCP_OPTION_255 = 255;

    public static final byte DHCP_MSG_TYPE_DISCOVER = 1;
    public static final byte DHCP_MSG_TYPE_OFFER = 2;
    public static final byte DHCP_MSG_TYPE_REQUEST = 3;
    public static final byte DHCP_MSG_TYPE_DECLINE = 4;
    public static final byte DHCP_MSG_TYPE_ACK = 5;
    public static final byte DHCP_MSG_TYPE_NAK = 6;
    public static final byte DHCP_MSG_TYPE_RELEASE = 7;
    public static final byte DHCP_MSG_TYPE_INFORM = 8;

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

                this.dhcpRawOpt61 = new byte[len];
                int m = 0;
                for (int n = i; n < i + len; n++) {
                    this.dhcpRawOpt61[m] = data[n];
                    m++;
                }

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

                this.dhcpRawOpt60 = new byte[len];
                int m = 0;
                for (int n = i; n < i + len; n++) {
                    this.dhcpRawOpt60[m] = data[n];
                    m++;
                }

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
                this.dhcpClientAddressRequest = baSubstr(data, i, len);
                i += len - 1;
                continue;
            }

            if (data[i] == DHCP_OPTION_82_RELAY_AGENT) {
                i++;

                Integer len = ba2int(baSubstr(data, i, 1));
                if (len < 1) break;

                i++;

                this.dhcpRawOpt82 = new byte[len];
                int m = 0;
                for (int n = i; n < i + len; n++) {
                    this.dhcpRawOpt82[m] = data[n];
                    m++;
                }

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

    public String type2string(byte type){

        switch (type){
            case DHCP_MSG_TYPE_DISCOVER:
                return "DHCPDISCOVER";
            case DHCP_MSG_TYPE_REQUEST:
                return "DHCPREQUEST";
            case DHCP_MSG_TYPE_DECLINE:
                return "DHCPDECLINE";
            case DHCP_MSG_TYPE_OFFER:
                return "DHCPOFFER";
            case DHCP_MSG_TYPE_RELEASE:
                return "DHCPRELEASE";
            case DHCP_MSG_TYPE_INFORM:
                return "DHCPINFORM";
            case DHCP_MSG_TYPE_ACK:
                return "DHCPACK";
            case DHCP_MSG_TYPE_NAK:
                return "DHCPNAK";
            default:
                return "unknown";
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

    String ba2mac(byte[] data) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02X%s", data[i], (i < data.length - 1) ? ":" : ""));
        }

        return sb.toString();
    }

    int getType() {
        int type = ba2int(this.dhcpMsgType);

        return type;
    }

    String getClientID() {
        if (this.dhcpClientID != null) {
            return ba2mac(this.dhcpClientID);
        }

        return "";
    }

    String getOpt82RemoteID() {

        if (this.dhcpOpt82RemoteID != null) {
            return ba2mac(this.dhcpOpt82RemoteID);
        }

        return "";
    }

    String getOpt82CircuitID() {

        if (this.dhcpOpt82CircuitID != null) {
            return ba2mac(this.dhcpOpt82CircuitID);
        }

        return "";
    }

    InetAddress getRelayAgent() {

        if (this.dhcpRelayAgentIP != null) {
            try {
                return InetAddress.getByAddress(this.dhcpRelayAgentIP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    InetAddress getClientIPAddress() {

        if (this.dhcpClientIPAddress != null) {
            try {
                return InetAddress.getByAddress(this.dhcpClientIPAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    String getClientMAC() {

        if (this.dhcpClientMACAddress != null) {
            return ba2mac(this.dhcpClientMACAddress);
        }

        return null;
    }

    InetAddress getAddressRequest() {

        if (this.dhcpClientAddressRequest != null) {
            try {
                return InetAddress.getByAddress(this.dhcpClientAddressRequest);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    byte[] buildReply(byte type, InetAddress localIP, InetAddress clientIP, InetAddress netmask, InetAddress router, InetAddress dns, int leaseTime) {

        int PKT_LEN = 360;

        byte[] data = new byte[PKT_LEN];
        int p = 0;

        // msg reply
        data[0] = 0x02;
        p++;

        // hwType
        data[1] = 0x01;
        p++;

        // hwAddrLen
        data[2] = 0x06;
        p++;

        // hops
        data[3] = 0x00;
        p++;

        // 0000 - unicast, 8000 - broadcast
        byte[] bootpFlags = this.dhcpBootpFlags;
        byte[] zaddr = {0, 0, 0, 0};
        byte[] padding10 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] opt53 = {53, 1, type};
        byte[] opt54 = {54, 4};
        byte[] opt51 = {51, 4};
        byte[] opt50 = {50, 4};
        byte[] opt43 = {43, 6};
        byte[] opt1 = {1, 4};
        byte[] opt3 = {3, 4};
        byte[] opt6 = {6, 4};
        byte[] opt255 = {(byte) 0xff};

        if (localIP == null) localIP = router;

        data = baInsert(data, p, this.dhcpTransID);
        p += this.dhcpTransID.length;

        data = baInsert(data, p, this.dhcpSecsElapsed);
        p += this.dhcpSecsElapsed.length;

        data = baInsert(data, p, bootpFlags);
        p += bootpFlags.length;

        // ciaddr
        data = baInsert(data, p, zaddr);
        p += zaddr.length;

        // yiaddr
        data = baInsert(data, p, clientIP.getAddress());
        p += clientIP.getAddress().length;

        // next server
        data = baInsert(data, p, router.getAddress());
        p += router.getAddress().length;

        // relay agent
        data = baInsert(data, p, this.dhcpRelayAgentIP);
        p += this.dhcpRelayAgentIP.length;

        data = baInsert(data, p, this.dhcpClientMACAddress);
        p += this.dhcpClientMACAddress.length;

        data = baInsert(data, p, padding10);
        p += padding10.length;

        // server hostname
        p += 64;

        // boot file
        p += 128;

        data = baInsert(data, p, DHCP_MAGIC_COOKIE);
        p += DHCP_MAGIC_COOKIE.length;

        // netmask
        data = baInsert(data, p, opt1);
        p += opt1.length;
        data = baInsert(data, p, netmask.getAddress());
        p += netmask.getAddress().length;

        // router
        data = baInsert(data, p, opt3);
        p += opt3.length;
        data = baInsert(data, p, router.getAddress());
        p += router.getAddress().length;

        // dns
        data = baInsert(data, p, opt6);
        p += opt6.length;
        data = baInsert(data, p, dns.getAddress());
        p += dns.getAddress().length;

        // msg type
        data = baInsert(data, p, opt53);
        p += opt53.length;

        // req address
        data = baInsert(data, p, opt50);
        p += opt50.length;
        data = baInsert(data, p, clientIP.getAddress());
        p += clientIP.getAddress().length;

        // lease time
        data = baInsert(data, p, opt51);
        p += opt51.length;
        data = baInsert(data, p, ByteBuffer.allocate(4).putInt(leaseTime).array());
        p += ByteBuffer.allocate(4).putInt(leaseTime).array().length;

        // vendor specific
        data = baInsert(data, p, opt43);
        p += opt43.length;
        data = baInsert(data, p, new byte[]{(byte) 0x01, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02});
        p += 6;

        // dhcp server
        data = baInsert(data, p, opt54);
        p += opt54.length;
        data = baInsert(data, p, router.getAddress());
        p += router.getAddress().length;

        // opt61
//        if (this.dhcpRawOpt61 != null) {
//            byte[] opt61 = {61, (byte) this.dhcpRawOpt61.length};
//            data = baInsert(data, p, opt61);
//            p += opt61.length;
//            data = baInsert(data, p, this.dhcpRawOpt61);
//            p += this.dhcpRawOpt61.length;
//        }

        // opt60
//        if (this.dhcpRawOpt60 != null) {
//            byte[] opt60 = {60, (byte) this.dhcpRawOpt60.length};
//            data = baInsert(data, p, opt60);
//            p += opt60.length;
//            data = baInsert(data, p, this.dhcpRawOpt60);
//            p += this.dhcpRawOpt60.length;
//        }

        // opt82
        if (this.dhcpRawOpt82 != null) {
            byte[] opt82 = {82, (byte) this.dhcpRawOpt82.length};
            data = baInsert(data, p, opt82);
            p += opt82.length;
            data = baInsert(data, p, this.dhcpRawOpt82);
            p += this.dhcpRawOpt82.length;
        }

        // end
        data = baInsert(data, p, opt255);

        return data;
    }
}
