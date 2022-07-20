/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peregrine.admin.netty.socket;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author P0114764
 */
public class ChannelSupervise {

    // 用于记录的管理所有客户端的Channel
    private static ChannelGroup GlobalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 自定义的map ，本例中用于区别不同user建立的Channel
    private static ConcurrentMap<Channel,String> ChannelMap = new ConcurrentHashMap();

    public static void addChannel(String userid, Channel channel) {
        GlobalGroup.add(channel);
        ChannelMap.put(channel, userid);
    }

    public static void removeChannel(Channel channel) {
        GlobalGroup.remove(channel);
        ChannelMap.remove(channel);
    }

//    public static Channel findChannel(Channel channel) {
//        return GlobalGroup.
//    }

    // 消息群发
    public static void send2All(TextWebSocketFrame tws) {
        Set<Map.Entry<Channel,String>> entries = ChannelMap.entrySet();
        Iterator<Map.Entry<Channel,String>> iterator1 = entries.iterator();
        while (iterator1.hasNext()){
            Map.Entry<Channel,String> next = iterator1.next();
            next.getKey().writeAndFlush(tws);
        }
//        GlobalGroup.forEach(channelItem -> {
//            channelItem.writeAndFlush(tws);
//        });
    }
}
