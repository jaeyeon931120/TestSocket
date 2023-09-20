package com.kevinlab;

import com.kevinlab.controller.NettyController;
import com.kevinlab.service.ProtocalService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;

@Controller
public class NettyServerStartProcess {

    private static final Logger logger = LogManager.getLogger(NettyController.class);

    public NettyServerStartProcess(ProtocalService protocalService) {
        startServer(protocalService);
    }

    public void startServer(ProtocalService protocalService){

        try {
            String resource = "properties/kevinlab.properties";
            System.out.println("===== Server Running =====");
            logger.info("===== Netty Server Test =====");

            new NettyServer(protocalService).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
