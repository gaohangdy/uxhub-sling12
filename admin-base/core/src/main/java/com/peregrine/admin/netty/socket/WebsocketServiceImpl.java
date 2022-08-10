package com.peregrine.admin.netty.socket;

import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, service = WebsocketService.class, immediate = true)
@Designate(ocd = WebsocketServiceImpl.Configuration.class)
public class WebsocketServiceImpl implements WebsocketService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private static final boolean DEFAULT_ENABLED = false;

  private static final int DEFAULT_PORT = 35729;

  private static final int MAX_CONTENT_LENGTH = 65536;

  private int port;

  private boolean enable;

  private NioEventLoopGroup broadcastGroup;

  private NioEventLoopGroup bossGroup;

  private NioEventLoopGroup workerGroup;

  private DefaultChannelGroup group;

  private ConcurrentHashMap<Channel, String> infos;

  private Channel serverChannel;

  private boolean running;

  @ObjectClassDefinition(name = "Uxhub: Web Socket Service", description = "Push update messages for users in the instance")
  @interface Configuration {
    @AttributeDefinition(name = "Enable", description = "Enable")
    boolean enable() default DEFAULT_ENABLED;

    @AttributeDefinition(name = "Port", description = "Web Socket Server Port", required = true)
    int port() default DEFAULT_PORT;
  }

  @Activate
  void activate(Configuration configuration) throws Exception {
    setup(configuration);
  }

  @Modified
  void modified(Configuration configuration) throws Exception {
    deactivate();
    setup(configuration);
  }

  @Deactivate
  protected void deactivate() throws InterruptedException {
    try {
      if (running) {
        try {
          stopServer();
        } finally {
          running = false;
        }
      }
    } finally {
      if (broadcastGroup != null) {
        broadcastGroup.shutdownGracefully().sync();
      }
      if (bossGroup != null) {
        bossGroup.shutdownGracefully().sync();
      }
      if (workerGroup != null) {
        workerGroup.shutdownGracefully().sync();
      }
    }
  }

  private void setup(Configuration configuration) throws Exception {
    this.enable = configuration.enable();
    if (this.enable) {
      this.port = configuration.port();
      this.broadcastGroup = new NioEventLoopGroup(1);

      this.group = new DefaultChannelGroup("Component-update-push", broadcastGroup.next());
      this.infos = new ConcurrentHashMap<Channel, String>();

      startServer();

      running = true;
    }
  }

  private void startServer() throws Exception {
    log.info("Starting websocket server");

    this.bossGroup = new NioEventLoopGroup();
    this.workerGroup = new NioEventLoopGroup();

    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup);
    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.childHandler(new NioWebSocketChannelInitializer());

    this.serverChannel = bootstrap.bind(port).sync().channel();
    log.info("Netty Web socket server started at port {}.", port);
  }

  class NioWebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
      // TODO Auto-generated method stub
      ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast("logging", new LoggingHandler("DEBUG"));// 设置log监听器，并且日志级别为debug，方便观察运行流程
      pipeline.addLast("http-codec", new HttpServerCodec());// 设置解码器
      pipeline.addLast("aggregator", new HttpObjectAggregator(MAX_CONTENT_LENGTH));// 聚合器，使用websocket会用到
      pipeline.addLast("http-chunked", new ChunkedWriteHandler());// 用于大数据的分区传输
      pipeline.addLast(new WebSocketServerProtocolHandler("/hellowebsocket"));
      pipeline.addLast("handler", new WebSocketServerHandler(group, infos));// 自定义的业务handler
    }

  }

  private void stopServer() throws InterruptedException {
    serverChannel.close().sync();
  }

}
