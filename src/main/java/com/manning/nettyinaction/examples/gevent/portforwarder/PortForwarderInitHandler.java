package com.manning.nettyinaction.examples.gevent.portforwarder;

import com.manning.nettyinaction.examples.netty.socksproxy.DirectClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import com.manning.nettyinaction.examples.netty.socksproxy.RelayHandler;


@ChannelHandler.Sharable
public class PortForwarderInitHandler extends ChannelInboundHandlerAdapter {
  private final Bootstrap b = new Bootstrap();
  private final String remoteHost;
  private final int remotePort;


  public PortForwarderInitHandler(String remoteHost, int remotePort) {
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
    ByteBuf in = (ByteBuf) msg;
    Promise<Channel> promise = ctx.executor().newPromise();
    promise.addListener(
        new FutureListener<Channel>() {
          @Override
          public void operationComplete(final Future<Channel> future) throws Exception {
            final Channel requestChannel = future.getNow();
            assert(future.isSuccess());
            ctx.pipeline().remove(PortForwarderInitHandler.this);
            requestChannel.pipeline().addLast(
                new RelayHandler(ctx.channel()),
                new LoggingHandler(LogLevel.INFO));
            ctx.pipeline().addLast(
                new RelayHandler(requestChannel),
                new LoggingHandler(LogLevel.INFO));
            requestChannel.writeAndFlush(msg);
          }
        }
    );
    final Channel inboundChannel = ctx.channel();
    b.group(inboundChannel.eventLoop())
        .channel(NioSocketChannel.class)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .handler(new DirectClientHandler(promise));
    b.connect(remoteHost, remotePort);
  }
}
