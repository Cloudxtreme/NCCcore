package com.NccDhcp;

import org.apache.directory.server.dhcp.messages.DhcpMessage;

import java.util.ArrayList;

public class NccRequestManager {

    private static final Long expire = 10L;
    private ArrayList<NccDhcpMessage> requests = new ArrayList<>();

    public void addRequest(DhcpMessage dhcpMessage){
        NccDhcpMessage nccDhcpMessage = new NccDhcpMessage();

        nccDhcpMessage.setExpireTime(System.currentTimeMillis() / 1000L + this.expire);
        nccDhcpMessage.setDhcpMessage(dhcpMessage);

        this.requests.add(nccDhcpMessage);
    }

    public DhcpMessage getRequest(int transationId){

        for(NccDhcpMessage msg: this.requests){
            if(msg.getDhcpMessage().getTransactionId() == transationId){
                return msg.getDhcpMessage();
            }
        }

        return null;
    }

    public void expireRequests(){
        Long nowTime = System.currentTimeMillis() / 1000L;
        for(NccDhcpMessage msg: this.requests){
            if(nowTime - msg.getExpireTime() > expire) this.requests.remove(msg);
        }
    }
}
