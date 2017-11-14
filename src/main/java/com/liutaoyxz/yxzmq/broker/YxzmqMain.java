package com.liutaoyxz.yxzmq.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server 主类,放弃netty, 采用jdk 自带的nio
 */
public class YxzmqMain {

    private static final int DEFAULT_PORT = 11171;

    private static final Logger LOGGER = LoggerFactory.getLogger(YxzmqMain.class);

    public static void main(String[] args) throws InterruptedException {
        Server server = new DefaultServer();
        server.start();
        LOGGER.info("start .........");


    }

}
