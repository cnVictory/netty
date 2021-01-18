package com.belief.netty.nettychat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class NettyChatClient {

    private String host;
    private int port;

    public NettyChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws InterruptedException {

        EventLoopGroup clientGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast("decoder", new StringDecoder()).addLast("encoder",
//                                    new StringEncoder()).addLast(new NettyChatClientHandler());
                            ch.pipeline().addLast(new MyLongToByteEncoder())
                                    .addLast(new MyByteToLongDecoder())
                                    .addLast(new MyClientHandler());

                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();

            // 得到channel，发送客户端输入的信息
            Channel channel = channelFuture.channel();
            System.out.println("------------" + channel.remoteAddress() + "-----------");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                channel.writeAndFlush(msg + "\n");
            }
        } finally {
            clientGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyChatClient("127.0.0.1", 6668).run();
    }
}
