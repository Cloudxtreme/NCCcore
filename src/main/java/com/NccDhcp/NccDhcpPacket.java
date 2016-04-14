package com.NccDhcp;

import java.net.InetAddress;

public class NccDhcpPacket {
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



    void setReply(int type, InetAddress clientIP, InetAddress netmask, InetAddress gateway, InetAddress dns, int leaseTime){

    }
}
