package com.kevinlab.controller;

import com.kevinlab.NettyServerStartProcess;
import com.kevinlab.service.ProtocalService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;

@Controller
public class NettyController {

    private static final Logger logger = LogManager.getLogger(NettyController.class);

    private final ProtocalService protocalService;

    public NettyController(ProtocalService protocalService) {
        this.protocalService = protocalService;
    }

    @RequestMapping(value = "/netty", method = {RequestMethod.GET})
    @ResponseBody
    public void Netty(HttpServletRequest request, HttpServletResponse res) throws Exception {

        InetAddress inetAddress = InetAddress.getLocalHost();

        System.out.println("Netty");

        logger.info("Netty Controller");
        logger.info("---------- Log 테스트 ---------");
        logger.info("IP : " + inetAddress);

        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "", method = {RequestMethod.GET})
    @ResponseBody
    public void start(HttpServletRequest request, HttpServletResponse res) throws Exception {
        logger.info("Netty Controller");
        logger.info("---------- Log 테스트 ---------");
        new NettyServerStartProcess(this.protocalService);
    }
}
