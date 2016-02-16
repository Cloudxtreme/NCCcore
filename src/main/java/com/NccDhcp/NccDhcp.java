package com.NccDhcp;

import com.NccSessions.NccSessions;
import com.NccSessions.NccSessionsException;
import com.NccSystem.NccUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.netty.DhcpServer;
import org.apache.directory.server.dhcp.options.DhcpOption;
import org.apache.directory.server.dhcp.options.OptionsField;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

public class NccDhcp {
    private static Logger logger = Logger.getLogger(NccDhcp.class);

    public void start() {

        NccLeaseManager nccLeaseManager = new NccLeaseManager();

        DhcpServer dhcpServer = new DhcpServer(nccLeaseManager);
        try {
            logger.info("Starting NccDhcp server");
            dhcpServer.addDefaultInterfaces();
            dhcpServer.addInterface(InterfaceAddress.forString("93.170.48.5"));
            dhcpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
