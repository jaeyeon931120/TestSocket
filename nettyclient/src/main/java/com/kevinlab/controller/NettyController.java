package com.kevinlab.controller;

import com.kevinlab.netty.NettyClient;
import com.kevinlab.netty.service.NettyService;
import org.apache.ibatis.io.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Reader;
import java.util.Properties;

@Controller
@RequestMapping("/")
public class NettyController {

    @Resource
    NettyService nettyService;

    @Autowired
    public NettyController(NettyService nettyService) {
        this.nettyService = nettyService;
    }

    private static final Logger logger = LogManager.getLogger(NettyController.class);

    @RequestMapping(value = "/{parentPath}/{childPath}", method = {RequestMethod.GET})
    @ResponseBody
    public void Netty(@PathVariable("parentPath") String parentPath, @PathVariable("childPath") String childPath
            , HttpServletRequest request, HttpServletResponse res) throws Exception {

        logger.info("parentPath : " + parentPath + " childPath: " + childPath);
        logger.info("---------- Log 테스트 ---------");

        try {
            if (parentPath.equals("netty")) {
                if (childPath.equals("test")) {
                    String resource = "/properties/kevinlab.properties";
                    logger.info("===== Netty Client Test =====");

                    Properties properties = new Properties();

                    Reader reader = Resources.getResourceAsReader(resource);
                    properties.load(reader);

                    String host = properties.getProperty("test.host");
                    int port = Integer.parseInt(properties.getProperty("test.port"));

                    String[] msgArr = {"수신 완료"};
                    new NettyClient(host, port, msgArr).run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
