package com.belief.netty.nio;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class ServerSocketChannelTest {

    @Test
    public void testServerSocketChannel() throws Exception{

        // 创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 得到一个Selector对象
        Selector selector = Selector.open();

        // 绑定一个端口进行监听
        serverSocketChannel.socket().bind(new InetSocketAddress(6378));

        // 设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);

        // 把serverSocketChannel 注册到 Selector上，并监听 OP_ACCEPT事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 循环等待客户端连接
        while (true) {

            // 等待1秒，如果没有事件发生，返回
            if (selector.select(1000) == 0) {
                System.out.println("服务器等待了1秒");
                continue;
            }

            // 有事件发生
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            // 遍历迭代器
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {

                // 可以根据selectionKey获取channel
                SelectionKey selectionKey = iterator.next();

                // 如果是新的连接，需要创建一个socketChannel来进行处理
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("客户端连接，服务端生成socketChannel = " + socketChannel.hashCode());

                    // 设置socketChannel 为非阻塞
                    socketChannel.configureBlocking(false);

                    // 将生成的socketChannel注册到selector，并关注事件为OP_READ
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

                // 如果触发了读事件
                if (selectionKey.isReadable()) {

                    // 通过 selectionKey 获取到对应的channel
                    SocketChannel channel = (SocketChannel)selectionKey.channel();

                    // 获取到channel 关联的byteBuffer
                    ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();

                    // channel 读取 buffer 数据
                    channel.read(byteBuffer);

                    System.out.println("客户端发送的信息 = " + new String(byteBuffer.array()));

                }

                // 从当前返回的selectedKeys 中移除已经被处理的这个selectionKey
                iterator.remove();
            }
        }

    }


    @Test
    public void testSocketChannel() throws Exception{

        // 得到一个客户端通道
        SocketChannel socketChannel = SocketChannel.open();

        // 设置非阻塞
        socketChannel.configureBlocking(false);

        // 设置客户端的socket需要连接的ip和端口
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6378);

        // 连接服务器
        if (!socketChannel.connect(inetSocketAddress)) {

            while (!socketChannel.finishConnect()) {
                System.out.println("因为连接需要时间，客户端不会阻塞，可以做其他工作");
            }
        }

        // 如果连接成功了，就发送数据
        String str = "come on , 你好";

        // 发送数据，将buffer数据写入channel
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
        socketChannel.write(buffer);

        System.in.read();
    }

}
