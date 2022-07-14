/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peregrine.admin.netty.socket;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 *
 * @author P0114764
 */
@Component(
        service = WebSocket.class,
        immediate = true,
        property = {
            SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Copy Image Transformation (transformation name: vips:convert",
            SERVICE_VENDOR + EQUALS + PER_VENDOR
        }
)
@Designate(
        ocd = WebSocketServer.Configuration.class
)
public class WebSocketServer implements WebSocket {

    private static final int DEFAULT_PORT = 35729;
    private static final int MAX_CONTENT_LENGTH = 65536;

    @ObjectClassDefinition(
            name = "netty web socket xx",
            description = "netty web socket xx "
    )
    @interface Configuration {

        @AttributeDefinition(
                name = "Enabled",
                description = "Flag to enabled / disabled that service",
                required = true
        )
        boolean enabled() default true;

        @AttributeDefinition(
                name = "port",
                description = "Transformation Name used to find it in the Rendition Configuration",
                required = true
        )
        String transformationName() default "35729";
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) {
        //configure(configuration);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //this.broadcastGroup = new NioEventLoopGroup(1);
        try {
            //创建服务器端的启动对象，配置参数 
            ServerBootstrap bootstrap = new ServerBootstrap();
            //设置两个线程组 
            bootstrap.group(bossGroup, workerGroup)
                    //使用NioServerSocketChannel 作为服务器的通道实现 
                    .channel(NioServerSocketChannel.class)
                    //设置线程队列得到连接个数 
                    //.option(ChannelOption.SO_BACKLOG, 128) 
                    //设置保持活动连接状态 
                    //.childOption(ChannelOption.SO_KEEPALIVE, true) 
                    //给workerGroup 的 EventLoop 对应的管道设置处理器 
                    .childHandler(new WebSocketServerInitializer());

            //启动服务器并绑定一个端口并且同步生成一个 ChannelFuture 对象 
            ChannelFuture cf = bootstrap.bind(DEFAULT_PORT).sync();

            if (cf.isSuccess()) {
                System.out.println("websocket server start---------------");
            }

            //对关闭通道进行监听 
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println("------ message server error ------" + e.getMessage());
            e.printStackTrace();
        } finally {
            //发送异常关闭 
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("------ 发送异常关闭  ------");
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
            /**
             * 对于websocket数据是以 帧 形式传递 浏览器请求时 ws://localhost:35729/hello 其中
             * hello会与下面的对应 WebSocketServerProtocolHandler
             * 核心功能是将http协议升级为ws协议，保持长链接
             */
            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
            //自定义handler 
            pipeline.addLast("handler", new WebSocketServerHandler());
        }
    }
}
