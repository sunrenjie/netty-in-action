package com.manning.nettyinaction.examples.gevent.portforwarder;

import java.net.InetSocketAddress;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


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

  public void start() throws Exception {
    EventLoopGroup group1 = new NioEventLoopGroup();
    try {
      ServerBootstrap b1 = new ServerBootstrap();
      b1.group(group1)
          .channel(NioServerSocketChannel.class)
          .localAddress(new InetSocketAddress(localPort))
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast(
                  new LoggingHandler(LogLevel.INFO),
                  new PortForwarderInitHandler(remoteHost, remotePort));
            }
          });
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
