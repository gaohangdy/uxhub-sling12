/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peregrine.admin.netty.socket;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author P0114764
 */

//将此类声明为service
@Component(
        service = WebSocket.class,
        immediate = true,
        property = {
            SERVICE_DESCRIPTION + EQUALS + "Netty WebSocket Service",
            SERVICE_VENDOR + EQUALS + PER_VENDOR
        }
)
//指定为 osgi -》 com.peregrine.admin.netty.socket.WebSocketServer
@Designate(
        ocd = WebSocketServer.Configuration.class
)
//必须实现接口，否则编译时会报错
public class WebSocketServer implements WebSocket {
    
    private Logger log = LoggerFactory.getLogger(this.getClass()); // 日志对象

    //默认端口
    private static final int DEFAULT_PORT = 35729;
    //请求最大内容
    private static final int MAX_CONTENT_LENGTH = 65536;

    // Configuration 的名字跟介绍
    @ObjectClassDefinition(
            name = "netty web socket xx name",
            description = "netty web socket xx description"
    )
    // 在 Configuration 中添加字段 “Enabled”和“port”，指定启动状态和端口的默认值
    @interface Configuration {

        @AttributeDefinition(
                name = "Enabled",
                description = "Flag to enabled / disabled that service",
                required = true
        )
        boolean enabled() default true;

        @AttributeDefinition(
                name = "port",
                description = "Port number used by websocket server",
                required = true
        )
        String transformationName() default "35729";
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) {
        // 获取 Reactor 线程池
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建服务器端的启动对象，用于配置TCP相关参数 
            ServerBootstrap bootstrap = new ServerBootstrap();
            //设置两个线程组 
            bootstrap.group(bossGroup, workerGroup)
                    //使用NioServerSocketChannel 作为服务器的通道实现 
                    .channel(NioServerSocketChannel.class)
                    //设置线程队列得到连接个数 
                    .option(ChannelOption.SO_BACKLOG, 128) 
                    //设置保持活动连接状态 
                    .childOption(ChannelOption.SO_KEEPALIVE, true) 
                    //给workerGroup 的 EventLoop 对应的管道设置处理器 
                    .childHandler(new WebSocketServerInitializer());

            //启动服务器并绑定一个端口并且同步生成一个 ChannelFuture 对象 
            ChannelFuture cf = bootstrap.bind(DEFAULT_PORT).sync();

            if (cf.isSuccess()) {
                log.info("websocket server start---------------");
            }

            //对关闭通道进行监听 
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            log.info("------ message server error ------" + e.getMessage());
            e.printStackTrace();
        } finally {
            //发送异常关闭 
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("------ send Exception close ------");
        }
    }

    class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            /**
             * 因为基于http协议，使用http的编解码器
             */
            pipeline.addLast("codec-http", new HttpServerCodec());
            /**
             * http数据传输过程是分段的，HttpObjectAggregator，就是可以将多段聚合
             * 当浏览器发送大量数据时，就会发出多次http请求
             */
            pipeline.addLast("aggregator", new HttpObjectAggregator(MAX_CONTENT_LENGTH));
            //加入chunked主要作用是支持异步发送的码流（大文件件传输），但不专用过多的内存，防止java内存溢出
            pipeline.addLast(new ChunkedWriteHandler());
            /**
             * 对于websocket数据是以 帧 形式传递 浏览器请求时 ws://localhost:35729/hello 其中
             * hello会与下面的对应 WebSocketServerProtocolHandler
             * 核心功能是将http协议升级为ws协议，保持长链接
             */
            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
             //自定义handler - 处理业务逻辑,因为要在handler 抛出之前处理http参数，所以放在handler前
            pipeline.addLast("handler", new WebSocketServerHandler());
        }
    }
}
