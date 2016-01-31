package com.manning.nettyinaction.examples.gevent.portforwarder;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.omg.CORBA.UNKNOWN;

@ChannelHandler.Sharable
public class PortForwarderHandler extends ChannelInboundHandlerAdapter {
  private enum PeerStatus {
    UNKNOWN, DOWN, ACTIVE;
  }

  private SocketChannel peerChannel;
  private Bootstrap peerBootstrap;
  private PeerStatus peerStatus = PeerStatus.UNKNOWN;

  public void setPeerChannel(SocketChannel peerChannel) {
    this.peerChannel = peerChannel;
  }

  public void setPeerBootstrap(Bootstrap peerBootstrap) {
    this.peerBootstrap = peerBootstrap;
    this.peerStatus = PeerStatus.DOWN;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    if (peerBootstrap != null) {
      assert(peerStatus == PeerStatus.DOWN); // TODO only one connection is allowed!
      peerBootstrap.connect();
      peerStatus = PeerStatus.ACTIVE;
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    assert(this.peerChannel != null);
    ByteBuf in = (ByteBuf) msg;
    System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8));
    this.peerChannel.writeAndFlush(in);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx,
                              Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
