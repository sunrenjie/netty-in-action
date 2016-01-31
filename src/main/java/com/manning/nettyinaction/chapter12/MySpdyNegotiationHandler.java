package com.manning.nettyinaction.chapter12;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.spdy.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import org.eclipse.jetty.npn.NextProtoNego;

import javax.net.ssl.SSLEngine;

public class MySpdyNegotiationHandler extends ApplicationProtocolNegotiationHandler {
  private final int maxSpdyContentLength;
  private final int maxHttpContentLength;

  public MySpdyNegotiationHandler(int maxSpdyContentLength, int maxHttpContentLength) {
    super(ApplicationProtocolNames.SPDY_3_1);
    this.maxSpdyContentLength = maxSpdyContentLength;
    this.maxHttpContentLength = maxHttpContentLength;
  }

  protected void configureHttp2(ChannelHandlerContext ctx) throws Exception {
    ChannelPipeline p = ctx.pipeline();
  }

  protected void configureHttp1(ChannelHandlerContext ctx) throws Exception {
    ChannelPipeline p = ctx.pipeline();
    p.addLast(new HttpServerCodec());
  }

  @Override
  protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
    if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
      configureHttp2(ctx);
    } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
      configureHttp1(ctx);
    } else {
      throw new IllegalStateException("unknown protocol: " + protocol);
    }
  }
}
