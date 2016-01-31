package com.manning.nettyinaction.examples.gevent.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.io.EOFException;
import java.nio.charset.Charset;

@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
  private final ByteBuf welcome = Unpooled.unreleasableBuffer(
      Unpooled.copiedBuffer("Welcome to the echo server! Type quit or Ctrl+D to exit.\r\n",
          Charset.forName("UTF-8")));

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf in = (ByteBuf) msg;
    String s = in.toString(CharsetUtil.UTF_8);
    String t = s.trim();
    if (t.equals("")) {
      throw new EOFException("client disconnected (empty input)");
    } else if (s.trim().equals("quit")) {
      throw new EOFException("client quit");
    }

    System.out.println("Server received: " + t);
    ctx.writeAndFlush(in); // to respond immediately
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    // Nothing to do.
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.writeAndFlush(welcome.duplicate());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    if (cause.getClass() == EOFException.class) {
      ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
          ChannelFutureListener.CLOSE);
      System.out.println("To close connection from " + ctx.channel().remoteAddress()
          + ": " + cause.getMessage());
    }
  }
}
