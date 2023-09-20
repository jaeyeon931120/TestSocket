package com.kevinlab.netty;

import com.kevinlab.controller.NettyController;
import org.apache.ibatis.io.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.util.Properties;

public class NettyStartProcess {

    private static final Logger logger = LogManager.getLogger(NettyController.class);

    public static void main(String[] args) throws Exception {
        String resource = "properties/kevinlab.properties";
        logger.info("===== Netty Client Test =====");

        Properties properties = new Properties();

        Reader reader = Resources.getResourceAsReader(resource);
        properties.load(reader);

        String host = properties.getProperty("test.host");
        int port = Integer.parseInt(properties.getProperty("test.port"));

        String[] msgArr = {"0x53 0x01 0x00 0x0F 0x69 0xB8 0x00 0x01 0x2F 0xD1 " +
                "0x00 0x23 0x31 0x35 0x12 0x32 0x30 0x32 0x32 0x30 " +
                "0x31 0x30 0x37 0x31 0x31 0x31 0x37 0x33 0x31 0x1F " +
                "0x00 0x00 0x03 0xE8 0x42 0xCA 0x38 0x52 0x00 0x00 " +
                "0x00 0x00 0x12 0x34 0x00 0x00 0x09 0x00 0x00 0x00 " +
                "0x12 0x34 0x00 0x00 0x08 0x00 0x00 0x00 0x12 0x34 " +
                "0x00 0x00 0x07 0x00 0x12 0x34 0x56 0x78 0x23 0x45 " +
                "0x67 0x89 0x12 0x34 0x56 0x78 0x00 0x00 0x06 0x00 " +
                "0x54 0x62 0xBF"};
        logger.info("========= host : " + host + "========");
        logger.info("========= port : " + port + "========");
        logger.info("========= message : " + msgArr.toString() + "========");
        new NettyClient(host, port, msgArr).run();
    }

}
