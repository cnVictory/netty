package com.belief.netty.customprotocol;

import com.belief.netty.nettychat.MyByteToLongDecoder;
import com.belief.netty.nettychat.MyLongToByteEncoder;
import com.belief.netty.nettychat.NettyServerLongHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyChatServer {

    private int port;

    public static void main(String[] args) throws InterruptedException {

        new NettyChatServer(6668).run();

    }

    public NettyChatServer(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {

        // 这里其实就是创建2个线程，或者说是2组线程，一组是bossGroup用于管理socket的连接，
        // 另外一组就是workerGroup用于处理socket的读写事件
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            // option和childOption方法，是给每一个连接到服务端的socket连接，设置一些基本属性
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new MyByteToMessageDecoder())
                                    .addLast(new MyMessageToByteEncoder())
                                    .addLast(new MyServerHandler());

                        }
                    });

            System.out.println("server is ready ...");
            /**
             * 绑定端口，并启动
             * 这里非常重要，主要分为4个部分
             * 1. 创建服务端channel   2. 初始化channel  3. 注册selector   4. 绑定端口
             */
            // bind方法是服务端channel创建的入口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            channelFuture.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
