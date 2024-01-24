package com.raidiam.trustframework.bank.listener;

import java.nio.charset.StandardCharsets;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.netty.NettyServerCustomizer;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import jakarta.inject.Singleton;
import static io.micronaut.http.netty.channel.ChannelPipelineCustomizer.HANDLER_HTTP_STREAM;

@Singleton
class BodyValidator implements BeanCreatedEventListener<NettyServerCustomizer.Registry>, NettyServerCustomizer {

    private static final String BODY_AGGREGATOR = "body-aggregator";
    private static final String BODY_VALIDATOR = "body-validator";
    private final Channel channel;
    private final HttpServerConfiguration serverConfiguration;

    public BodyValidator(@Nullable Channel channel, HttpServerConfiguration serverConfiguration) {
        this.channel = channel;
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    public NettyServerCustomizer.Registry onCreated(BeanCreatedEvent<Registry> event) {
        NettyServerCustomizer.Registry registry = event.getBean();
        registry.register(this);
        return registry;
    }

    @Override
    public NettyServerCustomizer specializeForChannel(Channel channel, ChannelRole role) {
        return new BodyValidator(channel, serverConfiguration);
    }


    @Override
    public void onStreamPipelineBuilt() {
        if (channel != null) {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addBefore(HANDLER_HTTP_STREAM, BODY_AGGREGATOR, new HttpObjectAggregator((int) serverConfiguration.getMaxRequestSize()));
            pipeline.addAfter(BODY_AGGREGATOR, BODY_VALIDATOR, new SimpleChannelInboundHandler<FullHttpRequest>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
                    byte[] bytes = ByteBufUtil.getBytes(msg.content());
                    // validate bytes
                    String s = new String(bytes, StandardCharsets.UTF_8);
                    if (s.contains("test")) {
                        ctx.writeAndFlush(
                                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(HttpResponseStatus.BAD_REQUEST.code(), "Request failed validation"))
                        ).addListener(listener -> channel.close());
                    } else {
                        ctx.fireChannelRead(msg);
                    }
                }
            });
        }
    }
}