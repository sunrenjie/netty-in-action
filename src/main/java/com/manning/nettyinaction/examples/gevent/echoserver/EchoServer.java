package com.manning.nettyinaction.examples.gevent.echoserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;

public class EchoServer {
  private final int port;

  public EchoServer(int port) {
    this.port = port;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
      System.exit(-1);
    }
    int port = Integer.parseInt(args[0]);
    System.out.println("Starting EchoServer at port " + port + " ...");
    EchoServer server = new EchoServer(port);
    server.serve();
  }

  public void serve() throws Exception {
    final EchoServerHandler serverHandler = new EchoServerHandler();
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(group).channel(NioServerSocketChannel.class)
          .localAddress(new InetSocketAddress(port))
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              System.out.println("Accepted connection from " + ch.remoteAddress());
              ch.pipeline().addLast(serverHandler);
            }
          });
      ChannelFuture f = b.bind().sync();
      f.channel().closeFuture().sync();
    } catch (Throwable ex) {
      ex.printStackTrace();
      group.shutdownGracefully().sync();
    }
  }
}
