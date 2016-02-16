/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.service.Dhcp6LeaseManager;
import org.anarres.dhcp.v6.service.Dhcp6Service;
import org.anarres.dhcp.v6.service.LeaseManagerDhcp6Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marosmars
 */
public class Dhcp6Server {

    private static final Logger LOG = LoggerFactory.getLogger(Dhcp6Server.class);

    private final Dhcp6Service service;
    private final int port;
    private NioDatagramChannel channel;
    private static final byte[] SERVER_ID = new byte[] { 0, 1 }; // TODO invalid type code
    // see https://tools.ietf.org/html/rfc3315#section-9.2

    public Dhcp6Server(@Nonnull Dhcp6Service service, @Nonnegative int port) {
        this.service = service;
        this.port = port;
    }

    public Dhcp6Server(@Nonnull Dhcp6Service service) {
        this(service, Dhcp6Service.SERVER_PORT);
    }

    public Dhcp6Server(@Nonnull Dhcp6LeaseManager manager, @Nonnegative int port, final DuidOption.Duid serverId) {
        this(new LeaseManagerDhcp6Service(manager, serverId), port);
    }

    public Dhcp6Server(@Nonnull Dhcp6LeaseManager manager, final DuidOption.Duid serverId) {
        this(new LeaseManagerDhcp6Service(manager, serverId));
    }

    @PostConstruct
    public void start() throws IOException, InterruptedException {
        ThreadFactory factory = new DefaultThreadFactory("dhcp-server");
        EventLoopGroup group = new NioEventLoopGroup(0, factory);

        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(NioDatagramChannel.class);
        b.handler(new Dhcp6Handler(service, SERVER_ID));
        channel = (NioDatagramChannel) b.bind(port).sync().channel();

        LOG.info("DHCPv6 server started on : {}, with id: {}", channel.localAddress(), SERVER_ID);

        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface netIf = networkInterfaces.nextElement();

            final InetSocketAddress relayGroup = new InetSocketAddress(InetAddress.getByName("FF02::1:2"), port);
            channel.joinGroup(relayGroup, netIf);
            final InetSocketAddress serverGroup = new InetSocketAddress(InetAddress.getByName("FF02::1:3"), port);
            channel.joinGroup(serverGroup, netIf);
        }
    }

    @PreDestroy
    public void stop() throws IOException, InterruptedException {
        EventLoop loop = channel.eventLoop();
        channel.close().sync();
        channel = null;
        loop.shutdownGracefully();
    }
}
