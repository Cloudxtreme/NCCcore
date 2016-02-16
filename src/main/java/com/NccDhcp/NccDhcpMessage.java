package com.NccDhcp;

import org.apache.directory.server.dhcp.messages.DhcpMessage;

public class NccDhcpMessage {
    private Long expireTime = 0L;
    private DhcpMessage dhcpMessage = new DhcpMessage();

    public Long getExpireTime(){
        return this.expireTime;
    }

    public void setExpireTime(Long expireTime){
        this.expireTime = expireTime;
    }

    public DhcpMessage getDhcpMessage(){
        return this.dhcpMessage;
    }

    public void setDhcpMessage(DhcpMessage dhcpMessage){
        this.dhcpMessage = dhcpMessage;
    }
}
