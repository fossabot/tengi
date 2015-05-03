package com.noctarius.tengi.server.transport.impl.negotiation;

import com.noctarius.tengi.serialization.Serializer;
import com.noctarius.tengi.server.server.ConnectionManager;
import com.noctarius.tengi.server.transport.impl.http.HttpConnectionProcessor;
import com.noctarius.tengi.server.transport.impl.websocket.WebsocketConnectionProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;

public class WebsocketNegotiator
        extends ChannelInboundHandlerAdapter {

    private static final String WEBSOCKET_PATH = "/wss";

    private final ConnectionManager connectionManager;
    private final Serializer serializer;

    public WebsocketNegotiator(ConnectionManager connectionManager, Serializer serializer) {
        this.connectionManager = connectionManager;
        this.serializer = serializer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object)
            throws Exception {

        if (object instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) object;

            // Activate websocket handshake
            if (WEBSOCKET_PATH.equals(request.uri())) {
                switchToWebsocket(ctx, request);
            } else {
                switchToHttpLongPolling(ctx);
            }
            ctx.fireChannelRead(request);
        } else {
            ReferenceCountUtil.release(object);
        }
    }

    private void switchToHttpLongPolling(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast("http-connection-processor", new HttpConnectionProcessor(connectionManager, serializer));
        pipeline.remove(this);
    }

    private void switchToWebsocket(ChannelHandlerContext ctx, FullHttpRequest request) {
        String location = getWebSocketLocation(ctx, request);
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(location, null, true);
        WebSocketServerHandshaker handshaker = factory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), request);
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast("websocket-connection-processor",
                    new WebsocketConnectionProcessor(handshaker, connectionManager, serializer));
            pipeline.remove(this);
        }
    }

    private String getWebSocketLocation(ChannelHandlerContext ctx, FullHttpRequest request) {
        SslHandler handler = ctx.pipeline().get(SslHandler.class);
        String location = request.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_PATH;
        if (handler != null) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

}
