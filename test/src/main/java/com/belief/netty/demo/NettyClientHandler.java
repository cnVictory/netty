package com.belief.netty.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("已经连接.. 发送消息.. ");
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("客户端 " + ctx.channel().localAddress() + " 已经连接，向你发送消息", CharsetUtil.UTF_8));
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("接收到服务端消息.." + byteBuf.toString(CharsetUtil.UTF_8));
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("读取完服务端消息..并向服务端端发送消息..");
//        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("hi~ server ，this is 客户端", CharsetUtil.UTF_8));
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生异常...");
        super.exceptionCaught(ctx, cause);
    }
}
