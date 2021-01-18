package com.belief.netty.nettychat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MyByteToLongDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("解码..");
        if (in.readableBytes() >= 8) {
            out.add(in.readLong());
        }
//        ctx.writeAndFlush(out);
    }
}
