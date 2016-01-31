package com.manning.nettyinaction.examples.gevent.portforwarder;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class PortForwarder {
  final private int localPort;
  final private String remoteHost;
  final private int remotePort;

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: " + PortForwarder.class.getSimpleName() +
          "local-port remote-host remote-port");
      System.exit(-1);
    }
    PortForwarder portForwarder = new PortForwarder(Integer.parseInt(args[0]),
        args[1], Integer.parseInt(args[2]));
    try {
      portForwarder.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public PortForwarder(int localPort, String remoteAddress, int remotePort) {
    this.localPort = localPort;
    this.remoteHost = remoteAddress;
    this.remotePort = remotePort;
  }

  public class ChannelInitializerWithForwarder extends ChannelInitializer<SocketChannel> {
    @Override
    public void initChannel(final SocketChannel ch1) throws Exception {
      System.out.println("initChannel for handler");
      final PortForwarderHandler handler1 = new PortForwarderHandler();
      ch1.pipeline().addLast(handler1);
      EventLoopGroup group2 = new NioEventLoopGroup();
      final PortForwarderHandler handler2 = new PortForwarderHandler();
      try {
        final Bootstrap b2 = new Bootstrap();
        b2.group(group2)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(remoteHost, remotePort))
            .handler(new ChannelInitializer<SocketChannel>() {
              @Override
              public void initChannel(SocketChannel ch2) throws Exception {
                System.out.println("initChannel for sub-handler");
                handler1.setPeerChannel(ch2);
                handler1.setPeerBootstrap(b2);
                handler2.setPeerChannel(ch1);
                ch2.pipeline().addLast(handler2);
              }
            });
        b2.connect().sync();
      } catch (Exception e) {
        group2.shutdownGracefully().sync();
        throw(e);
      }
    }
  }

  public void start() throws Exception {
    EventLoopGroup group1 = new NioEventLoopGroup();
    final PortForwarderHandler handler1 = new PortForwarderHandler();
    // TODO shutdown group and channel, handlers
    try { // initialize the client part
      ServerBootstrap b1 = new ServerBootstrap();
      b1.group(group1)
          .channel(NioServerSocketChannel.class)
          .localAddress(new InetSocketAddress(localPort))
          .childHandler(new ChannelInitializerWithForwarder());
      ChannelFuture f = b1.bind().sync();
      System.out.println("Portforwarder for " + remoteHost + ":" + remotePort +
          " is started at port " + localPort);
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      group1.shutdownGracefully().sync();
      throw(e);
    }
  }
}
