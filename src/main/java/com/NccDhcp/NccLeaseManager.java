package com.NccDhcp;

import com.Ncc;
import com.NccSessions.NccSessions;
import com.NccSessions.NccSessionsException;
import com.NccSystem.NccUtils;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.DhcpOption;
import org.apache.directory.server.dhcp.options.OptionsField;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.UnknownHostException;

class NccLeaseManager implements LeaseManager {

    private static Logger logger = Logger.getLogger(NccDhcp.class);
    private NccRequestManager nccRequestManager = new NccRequestManager();

    @Override
    public DhcpMessage leaseOffer(@Nonnull DhcpRequestContext dhcpRequestContext, @Nonnull DhcpMessage dhcpMessage, @CheckForNull InetAddress inetAddress, @CheckForSigned long l) throws DhcpException {
        logger.debug("DHCP leaseOffer: messageType=" + dhcpMessage.getMessageType() + " " + dhcpMessage.getHardwareAddress());

        Long leasedIP = 0L;

        DhcpMessage reply = new DhcpMessage();
        try {
            NccSessions nccSessions = new NccSessions();
            // TODO: 27.01.2016 Select IP from TariffScale pools
            leasedIP = nccSessions.getIPFromPool(null);
        } catch (NccSessionsException e) {
            e.printStackTrace();
        }

        try {
            reply.setMessageType(MessageType.DHCPOFFER);
            reply.setRelayAgentAddress(dhcpMessage.getRelayAgentAddress());
            reply.setAssignedClientAddress(InetAddress.getByName(NccUtils.long2ip(leasedIP)));
            reply.setTransactionId(dhcpMessage.getTransactionId());
            reply.setHardwareAddress(dhcpMessage.getHardwareAddress());
            reply.setOp(DhcpMessage.OP_BOOTREPLY);

            reply.setFlags((short) 0x8000);

            OptionsField options = new OptionsField();

            NccDhcpOptions nccDhcpOptions = new NccDhcpOptions();

            // TODO: 27.01.2016 Get options from DB
            options.add(nccDhcpOptions.setOption(NccDhcpOptions.NCCDHCP_NETMASK, InetAddress.getByName("255.255.255.255").getAddress()));
            options.add(nccDhcpOptions.setOption(NccDhcpOptions.NCCDHCP_GATEWAY, InetAddress.getByName("93.170.48.5").getAddress()));
            options.add(nccDhcpOptions.setOption(NccDhcpOptions.NCCDHCP_DNS_SERVER, InetAddress.getByName("151.0.48.59").getAddress()));

            reply.setOptions(options);

            nccRequestManager.addRequest(reply);

            return reply;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DhcpMessage leaseRequest(@Nonnull DhcpRequestContext dhcpRequestContext, @Nonnull DhcpMessage dhcpMessage, @Nonnull InetAddress inetAddress, @CheckForSigned long l) throws DhcpException {
        logger.debug("DHCP leaseRequest: messageType=" + dhcpMessage.getMessageType() + " " + dhcpMessage.getHardwareAddress());

        DhcpMessage findRequest = nccRequestManager.getRequest(dhcpMessage.getTransactionId());
        DhcpMessage reply = dhcpMessage;

        if (findRequest == null) {
            logger.error("DhcpMessage not found in requestManager.");
            return null;
        }

        reply.setMessageType(MessageType.DHCPACK);
        reply.setOp(DhcpMessage.OP_BOOTREPLY);
        reply.setTransactionId(dhcpMessage.getTransactionId());

        NccDhcpOptions nccDhcpOptions = new NccDhcpOptions();

        reply.setAssignedClientAddress(findRequest.getAssignedClientAddress());

        OptionsField options = findRequest.getOptions();
        DhcpOption optNetmask = options.get(NccDhcpOptions.NCCDHCP_NETMASK);
        DhcpOption optGateway = options.get(NccDhcpOptions.NCCDHCP_GATEWAY);
        DhcpOption optNameServer = options.get(NccDhcpOptions.NCCDHCP_NAME_SERVER);
        DhcpOption optDNSServer = options.get(NccDhcpOptions.NCCDHCP_DNS_SERVER);

        if (optNetmask != null)
            options.add(nccDhcpOptions.setOption(NccDhcpOptions.NCCDHCP_NETMASK, optNetmask.getData()));
        if (optGateway != null)
            options.add(nccDhcpOptions.setOption(NccDhcpOptions.NCCDHCP_GATEWAY, optGateway.getData()));
        if (optNameServer != null)
            options.add(nccDhcpOptions.setOption(NccDhcpOptions.NCCDHCP_NAME_SERVER, optNameServer.getData()));
        if (optDNSServer != null)
            options.add(nccDhcpOptions.setOption(NccDhcpOptions.NCCDHCP_DNS_SERVER, optDNSServer.getData()));

        reply.setOptions(options);
        return dhcpMessage;
    }

    @Override
    public boolean leaseDecline(@Nonnull DhcpRequestContext dhcpRequestContext, @Nonnull DhcpMessage dhcpMessage, @Nonnull InetAddress inetAddress) throws DhcpException {
        logger.debug("DHCP leaseDecline: messageType=" + dhcpMessage.getMessageType() + " " + dhcpMessage.getHardwareAddress());
        return false;
    }

    @Override
    public boolean leaseRelease(@Nonnull DhcpRequestContext dhcpRequestContext, @Nonnull DhcpMessage dhcpMessage, @Nonnull InetAddress inetAddress) throws DhcpException {
        logger.debug("DHCP leaseRelease: messageType=" + dhcpMessage.getMessageType() + " " + dhcpMessage.getHardwareAddress());
        return false;
    }
}
