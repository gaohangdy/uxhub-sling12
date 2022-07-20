/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peregrine.admin.netty.socket;

import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author P0114764
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private Logger log = LoggerFactory.getLogger(this.getClass()); // 日志对象

    private static final String WEBSOCKET_PATH = "/hello";
    private static final String SERVER_RETURN_MSG = "Message returned by the server";

    private WebSocketServerHandshaker handshaker;

    /*
    经过测试，在 ws 的 uri 后面不能传递参数，不然在 netty 实现 websocket 协议握手的时候会出现断开连接的情况。
   针对这种情况在 websocketHandler 之前做了一层 地址过滤，然后重写
   request 的 uri，并传入下一个管道中，基本上解决了这个问题。
    * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info(">>>>>>>>>>>Server receive message：{}", msg.toString());
        if (msg instanceof FullHttpRequest) {
            //以http请求形式接入，但是走的是websocket
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            //处理websocket客户端的消息
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }
        // 返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        log.info(">>>>>>>>>>>Server receive message：{}" + request);
        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
                + " SERVER_RETURN_MSG " + "：" + request);
        // 群发
        ChannelSupervise.send2All(tws);
        // 返回【谁发的发给谁】
        // ctx.channel().writeAndFlush(tws);
    }

    /**
     * 唯一的一次http请求，用于创建websocket
     *
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // 拿到请求地址
        String uri = req.getUri();
        // 判断是不是websocket请求，如果是拿出我们传递的参数 userid
        String origin = req.headers().get("Origin");
        if (null == origin) {
            log.info("origin is null");
            ctx.close();
        } else {
            if (null != uri && uri.contains("/hello") && uri.contains("?")) {
                String[] uriArray = uri.split("\\?");
                if (null != uriArray && uriArray.length > 1) {
                    String[] paramsArray = uriArray[1].split("=");
                    if (null != paramsArray && paramsArray.length > 1) {
                        String srcuserid = paramsArray[1];
                        log.info("extract {}", srcuserid);
                        ChannelSupervise.addChannel(srcuserid, ctx.channel());
                    }
                }
                req.setUri("/hello");
            } else {
                log.info("Forced disconnection of {} connection is not allowed", origin);
                ctx.close();
            }
        }

        //要求Upgrade为websocket，过滤掉get/Post
        if (!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            log.info("handshaker == null");
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    /**
     * 拒绝不合法的请求，并返回错误信息
     *
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        // 如果是非Keep-Alive，关闭连接
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    // 当web客户端连接后触发, 将channel 放到channelGroup中统一管理
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ChannelSupervise.addChannel("useridchannelid", ctx.channel());
        log.info(">>>>>>>>>>>> channelId:{} Connection trigger");
    }

    // 当web客户端销毁时触发，
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 当handlerRemoved 被触发时候，channelGroup会自动移除对应的channel
        // 这里需要移除 自定义 Channelmap 中的Channel
        ChannelSupervise.removeChannel(ctx.channel());
        log.info(">>>>>>>>>>>>> channelId:{} close down");
    }

    // channel 通道 Read 读取 Complete 完成 在通道读取完成后会在这个方法里通知，对应可以做刷新操作 ctx.flush()
    // 当消息不超过buffer量时，优先调用，为了提升性能
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    // 获取WebSocket地址
    private static String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }
}
