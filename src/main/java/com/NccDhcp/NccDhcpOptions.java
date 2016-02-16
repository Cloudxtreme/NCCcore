package com.NccDhcp;

import org.apache.directory.server.dhcp.options.DhcpOption;
import org.apache.directory.server.dhcp.options.OptionsField;

public class NccDhcpOptions {
    public static final byte NCCDHCP_NETMASK = 1;
    public static final byte NCCDHCP_GATEWAY = 3;
    public static final byte NCCDHCP_NTP_SERVER = 4;
    public static final byte NCCDHCP_NAME_SERVER = 5;
    public static final byte NCCDHCP_DNS_SERVER = 6;

    class NccDhcpOptionNetmask extends DhcpOption {

        @Override
        public byte getTag(){
            return NCCDHCP_NETMASK;
        }
    }

    class NccDhcpOptionGateway extends DhcpOption {

        @Override
        public byte getTag(){
            return NCCDHCP_GATEWAY;
        }
    }

    class NccDhcpOptionNtpServer extends DhcpOption {

        @Override
        public byte getTag(){
            return NCCDHCP_NTP_SERVER;
        }
    }

    class NccDhcpOptionNameServer extends DhcpOption {

        @Override
        public byte getTag(){
            return NCCDHCP_NAME_SERVER;
        }
    }

    class NccDhcpOptionDNSServer extends DhcpOption {

        @Override
        public byte getTag(){
            return NCCDHCP_DNS_SERVER;
        }
    }

    public DhcpOption setOption(byte option, byte[] value){

        switch (option){
            case NCCDHCP_NETMASK:
                DhcpOption optionNetmask = new NccDhcpOptionNetmask();
                optionNetmask.setData(value);
                return optionNetmask;
            case NCCDHCP_GATEWAY:
                DhcpOption optionGateway = new NccDhcpOptionGateway();
                optionGateway.setData(value);
                return optionGateway;
            case NCCDHCP_NTP_SERVER:
                DhcpOption optionNtpServer = new NccDhcpOptionNtpServer();
                optionNtpServer.setData(value);
                return optionNtpServer;
            case NCCDHCP_NAME_SERVER:
                DhcpOption optionNameServer = new NccDhcpOptionNameServer();
                optionNameServer.setData(value);
                return optionNameServer;
            case NCCDHCP_DNS_SERVER:
                DhcpOption optionDNSServer = new NccDhcpOptionDNSServer();
                optionDNSServer.setData(value);
                return optionDNSServer;
            default:
                break;
        }

        return null;
    }
}
