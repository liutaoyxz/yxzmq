package com.liutaoyxz.yxzmq.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * default server
 */
public class DefaultServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServer.class);

    private static final String DEFAULT_CHARSET = "utf-8";

    private ServerConfig config;

    private Lock lock = new ReentrantLock();

    private Condition sync = lock.newCondition();

    private volatile boolean stopFlag = false;

    private Selector selector;

    public void start() {
        try {

            lock.lock();
            if (config == null) {
                config = new ServerConfig();
            }
            this.selector = Selector.open();
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(config.getPort()));
            SelectionKey register = channel.register(selector, SelectionKey.OP_ACCEPT);
            LOGGER.info("yxzserver started on port {}", config.getPort());
            int ic = 0;
            for (; ; ) {
                if (stopFlag) {
                    break;
                }
                ic = selector.select();
                if (ic > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        try {
                            SelectionKey key = keys.next();
                            handleKey(key);
                        } finally {
                            keys.remove();
                        }
                    }
                }
            }

            sync.await();
        } catch (InterruptedException e) {
            LOGGER.info("server interrupted", e);
        } catch (IOException e) {
            LOGGER.error("start error", e);
        } finally {
            lock.unlock();
        }

    }

    public void stop() {
        this.stopFlag = true;
        System.exit(0);
    }

    public Server setConfig(ServerConfig config) {
        if (config == null) {
            throw new NullPointerException("config can not be null");
        }
        return this;
    }

    /**
     * 处理key
     * @param key
     * @throws IOException
     */
    private void handleKey(SelectionKey key) throws IOException {
        try {
            if (key.isAcceptable()) {
                LOGGER.info("client connected");
                SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                clientChannel.configureBlocking(false);
                clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(128));
            }
            if (key.isReadable()) {
                // 读取数据
                SocketChannel socketChannel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(2048);
                readLoop:
                while (true) {
                    int readCount = socketChannel.read(buffer);
                    if (readCount == -1) {
                        // 连接已经关闭
                        socketChannel.close();
                        break readLoop;
                    }
                    if (readCount == 0) {
                        //没有内容
                        break readLoop;
                    }
                    buffer.flip();
                    String msg = Charset.forName(DEFAULT_CHARSET).decode(buffer).toString();
                    LOGGER.info("receive message : {}", msg);
                    if (msg != null && msg.equals("stop")){
                        LOGGER.info("server stop");
                        stop();
                    }
                    buffer.clear();
                }
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
            if (key.isWritable()) {
                // 等待写入状态
            }
        } catch (CancelledKeyException e) {
            LOGGER.info("channel is cancelled");
        }
    }



}
