package com.voxelwind.server.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.voxelwind.server.VoxelwindServer;
import com.voxelwind.server.network.raknet.handler.TailHandler;
import com.voxelwind.server.network.raknet.handler.RakNetDatagramHandler;
import com.voxelwind.server.network.raknet.handler.RakNetDirectPacketHandler;
import com.voxelwind.server.network.raknet.DatagramRakNetPacketCodec;
import com.voxelwind.server.network.raknet.SimpleRakNetPacketCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class NettyVoxelwindNetworkListener extends ChannelInitializer<DatagramChannel> {
    private static final Logger LOGGER = LogManager.getLogger(NettyVoxelwindNetworkListener.class);
    private final Bootstrap bootstrap;
    private final InetSocketAddress address;
    private final VoxelwindServer server;
    private final boolean useSoReuseport;

    public NettyVoxelwindNetworkListener(VoxelwindServer voxelwindServer, String host, int port, boolean useSoReuseport) {
        this.server = voxelwindServer;
        this.address = new InetSocketAddress(host, port);
        this.useSoReuseport = useSoReuseport;
        if (Epoll.isAvailable()) {
            bootstrap = new Bootstrap()
                    .channel(EpollDatagramChannel.class)
                    .group(new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Voxelwind MCPE Listener - #%d").build()))
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(this);
            if (useSoReuseport) {
                bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
            }
        } else {
            bootstrap = new Bootstrap()
                    .channel(NioDatagramChannel.class)
                    .group(new NioEventLoopGroup(0, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Voxelwind MCPE Listener - #%d").build()))
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(this);
        }
    }

    @Override
    protected void initChannel(DatagramChannel channel) throws Exception {
        channel.pipeline()
                .addLast("simpleRaknetHandler", new SimpleRakNetPacketCodec())
                .addLast("voxelwindPacketHandler", new RakNetDirectPacketHandler(server))
                .addLast("raknetDatagramHandler", new DatagramRakNetPacketCodec(server))
                .addLast("voxelwindDatagramHandler", new RakNetDatagramHandler(server))
                .addLast("tailHandler", new TailHandler());
    }

    public void bind() {
        if (Epoll.isAvailable() && useSoReuseport) {
            // Can use SO_REUSEPORT so that multiple threads can bind to a port.
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                try {
                    ChannelFuture future = bootstrap.bind(address).await();
                    if (future.isSuccess()) {
                        LOGGER.debug("Bound listener #" + i + " for " + address);
                    } else {
                        LOGGER.error("Unable to bind listener #" + i + " for " + address, future.cause());
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.info("Interrupted while waiting for bind");
                }
            }
        } else {
            try {
                ChannelFuture future = bootstrap.bind(address).await();
                if (future.isSuccess()) {
                    LOGGER.debug("Bound listener for " + address);
                } else {
                    LOGGER.error("Unable to bind listener for " + address, future.cause());
                }
            } catch (InterruptedException e) {
                LOGGER.info("Interrupted while waiting for bind");
            }
        }
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
