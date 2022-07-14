/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peregrine.admin.netty.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 *
 * @author P0114764
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println(">>>>>>>>>>>服务端收到消息：{}" + msg.text());
        
        /** 
         * 回复消息 
         */ 
        ctx.writeAndFlush(new TextWebSocketFrame("服务器收到了,并返回：" + msg.text()));
        }
    
    /** 
     * 当web客户端连接后，触发方法 
     * @param ctx 
     * @throws Exception 
     */ 
    @Override 
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception { 
        
        System.out.println(">>>>>>>>>>>> channelId:{} 连接触发");
        /** 
         * 这个ID是唯一的 
         */ 
        //System.out.println(">>>>>>>>>>>> channelId:{} 连接" + ctx.channel().id().asLongText()); 
 
        /** 
         *  这个ID不是唯一的 
         */ 
        //System.out.println(">>>>>>>>>>>> channelId:{} 连接" + ctx.channel().id().asShortText()); 
    }
    
    @Override 
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception { 
        System.out.println(">>>>>>>>>>>>> channelId:{} 关闭了");
    } 
    
    @Override 
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception { 
        System.out.println(">>>>>>>>发送异常：{}" + cause.getMessage()); 
        ctx.close(); 
    } 
    
}
