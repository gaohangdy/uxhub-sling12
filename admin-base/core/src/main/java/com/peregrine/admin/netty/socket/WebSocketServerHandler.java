package com.peregrine.admin.netty.socket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
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
import static io.netty.handler.codec.http.HttpHeaders.*;

public final class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final String WEBSOCKET_PATH = "/hellowebsocket";

  private static final String HOST = "host";

  private WebSocketServerHandshaker handshaker;

  private DefaultChannelGroup group;

  private ConcurrentHashMap<Channel, String> infos;

  public WebSocketServerHandler(DefaultChannelGroup group, ConcurrentHashMap<Channel, String> infos) {
    this.group = group;
    this.infos = infos;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    // TODO Auto-generated method stub
    if (msg instanceof FullHttpRequest) {
      handleHttpRequest(ctx, (FullHttpRequest) msg);
    } else if (msg instanceof WebSocketFrame) {
      handleWebSocketFrame(ctx, (WebSocketFrame) msg);
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

    // 要求Upgrade为websocket，过滤掉get/Post
    if (!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
      // 若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
      sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
          HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
      return;
    }

    getRequestUri(ctx, req);

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

  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
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
    log.info(">>>>>>>>>>>Server receive message : {}" + request);
    TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
        + " SERVER_RETURN_MSG " + " : " + request);
    // 群发
    // ChannelSupervise.send2All(tws);
    send2All(group, tws);
    // 返回【谁发的发给谁】
    // ctx.channel().writeAndFlush(tws);
  }

  /**
   * 拒绝不合法的请求，并返回错误信息
   *
   */
  private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
    // 返回应答给客户端
    if (res.getStatus().code() != HttpServletResponse.SC_OK) {
      ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
      res.content().writeBytes(buf);
      buf.release();
    }
    ChannelFuture f = ctx.channel().writeAndFlush(res);
    // 如果是非Keep-Alive，关闭连接
    if (!isKeepAlive(req) || res.getStatus().code() != HttpServletResponse.SC_OK) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }

  // 获取WebSocket地址
  private static String getWebSocketLocation(FullHttpRequest req) {
    return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
  }

  private void getRequestUri(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    // 拿到请求地址
    String uri = req.getUri();
    // 判断是不是websocket请求，如果是拿出我们传递的参数 userid
    String origin = req.headers().get("Origin");
    if (null == origin) {
      log.info("origin is null");
      // ctx.close();
    } else {
      if (null != uri && uri.contains(WEBSOCKET_PATH) && uri.contains("?")) {
        String[] uriArray = uri.split("\\?");
        if (null != uriArray && uriArray.length > 1) {
          String[] paramsArray = uriArray[1].split("=");
          if (null != paramsArray && paramsArray.length > 1) {
            String srcuserid = paramsArray[1];
            log.info("extract {}", srcuserid);
            // ChannelSupervise.addChannel(srcuserid, ctx.channel());
            group.add(ctx.channel());
            infos.put(ctx.channel(), srcuserid);
          }
        }
        req.setUri(WEBSOCKET_PATH);
      } else {
        log.info("Forced disconnection of {} connection is not allowed", origin);
        // ctx.close();
      }
    }
  }

  public static void send2All(DefaultChannelGroup group, TextWebSocketFrame tws) {
    group.forEach(channelItem -> {
      channelItem.writeAndFlush(tws);
    });
  }

  // 当web客户端销毁时触发，
  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      // 当handlerRemoved 被触发时候，channelGroup会自动移除对应的channel
      // 这里需要移除 自定义 Channelmap 中的Channel
      group.remove(ctx.channel());
      infos.remove(ctx.channel());
      log.info(">>>>>>>>>>>>> channelId:{} close down");
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      // 当handlerRemoved 被触发时候，channelGroup会自动移除对应的channel
      // 这里需要移除 自定义 Channelmap 中的Channel
      group.add(ctx.channel());
      // infos.put(key, value)
      // infos.remove(ctx.channel());
      log.info(">>>>>>>>>>>>> channelId:{} close down");
  }
}
