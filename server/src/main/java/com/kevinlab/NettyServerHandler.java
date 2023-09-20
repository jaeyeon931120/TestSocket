package com.kevinlab;

import com.kevinlab.service.ProtocalService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(NettyServerHandler.class);

    private final ProtocalService protocalService;

    public NettyServerHandler(ProtocalService protocalService){
        this.protocalService = protocalService;
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        int n = buf.readableBytes();
        if (n > 0) {
            byte[] b = new byte[n];
            buf.readBytes(b);
            //수신메시지 출력
            String receiveMsg = new String(b);
            logger.info("handler 수신된 메시지 > " + receiveMsg);
            System.out.println("handler 수신된 메시지 > " + receiveMsg);
            if (receiveMsg.indexOf("0x") > -1) {
                int findnum = receiveMsg.indexOf("0x", 1);
                String regex = receiveMsg.substring(findnum - 1, findnum);
                String[] content = receiveMsg.split(regex);

                try {
                    protocalService.protocals(content);
                    logger.info("======== 서버 종료 ========");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}