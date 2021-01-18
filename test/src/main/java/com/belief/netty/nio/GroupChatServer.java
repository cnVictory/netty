package com.belief.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class GroupChatServer {

    // selector
    private Selector selector;
    // serverSocketChannel
    private ServerSocketChannel serverSocketChannel;
    // 监听端口
    private static final int PORT = 6377;

    // 构造
    public GroupChatServer() {

        try {

            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            // 绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            // 设置非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 注册serverSocketChannel
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 监听
    public void listen() {

        try {

            while (true) {
                if (selector.select(1000) == 0) {
                    System.out.println("没有客户端连接");
                    continue;
                }

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    // 如果是Accept连接事件
                    if (selectionKey.isAcceptable()) {
                        // accept方法本身会阻塞，但是因为已经判断了是否有Accept事件，所以这里不阻塞，直接得到sc通道
                        SocketChannel sc = serverSocketChannel.accept();
                        // 将新生成的socketChannel注册到selector上，监听Read事件
                        sc.register(selector, SelectionKey.OP_READ);
                        // 设置非阻塞
                        sc.configureBlocking(false);
                        System.out.println(sc.getRemoteAddress().toString() + " 上线了");
                    }

                    // 可读的事件
                    if (selectionKey.isReadable()) {
                        read(selectionKey);
                    }

                    // 从发生事件的selectionKeys列表中移除已经处理的自己
                    iterator.remove();
                }
            }

        } catch (IOException e) {

        } finally {

        }
    }

    /**
     * 读取通道数据
     * @param selectionKey
     */
    public void read(SelectionKey selectionKey) {

        SocketChannel sc = null;

        try {
            // 通过selectionKey获取绑定的通道
            sc = (SocketChannel) selectionKey.channel();
            // 获取buffer
            ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
            // 把通道中的数据读到buffer
            int read = sc.read(buffer);
            if (read > 0) {
                String msg = new String(buffer.array());
                System.out.println("客户端 " + sc.getRemoteAddress().toString() + " 发送信息：" + msg);

                // 向其他客户端转发消息
                publish(msg, sc);
            }

        } catch (IOException e) {

            try {
                System.out.println(sc.getRemoteAddress() + " 离线");
                // 取消注册
                selectionKey.cancel();
                // 关闭通道
                sc.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        } finally {

        }

    }

    /**
     * 转发，排除自己
     * @param msg
     * @param self
     */
    private void publish(String msg, SocketChannel self) throws IOException{

        System.out.println("服务器转发消息中....");
        for (SelectionKey selectionKey : selector.keys()) {
            Channel sc = selectionKey.channel();
            if (sc instanceof SocketChannel && sc != self) {
                SocketChannel dest = (SocketChannel) sc;
                // 将消息写入通道
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                dest.write(buffer);
            }
        }
    }

}
