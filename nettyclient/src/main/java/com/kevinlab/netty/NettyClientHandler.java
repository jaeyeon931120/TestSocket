package com.kevinlab.netty;

import com.kevinlab.netty.service.NettyService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Configurable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(NettyClientHandler.class);

    private NettyService nettyService;

    private String sendMsg;
    private int lostCnt_ = 0;
    private final int LIMIT_COUNT = 1;

    public NettyClientHandler(String msg){
        this.sendMsg = msg;
    }

    private NettyClientAction action_;
    public void setCallBackClientHandler(NettyClientAction action){
        this.action_ = action;
    }

    private String receiveMsg;
    public String getReceiveMsg(){
        return this.receiveMsg;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("채널 등록");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("채널 연결이 종료됨.");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        logger.info("채널이 메시지 발송할 준비가 됨.");
        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMsg.getBytes());
        ctx.writeAndFlush(messageBuffer); // 메시지를 발송하고 flush처리
        logger.info("발송 메시지 > " + sendMsg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        logger.info("메시지를 받는 메소드.");
        ByteBuf buf = (ByteBuf) msg;
        int n = buf.readableBytes();
        if( n > 0 ){
            byte[] b = new byte[n];
            buf.readBytes(b);
            //수신메시지 출력
            this.receiveMsg = new String(b,"UTF-8");
            logger.info("handler 수신된 메시지 > " + this.receiveMsg);
            String[] content = this.receiveMsg.split("\\s");
            Map<String, Object> data;

            if(this.action_ != null){
                action_.receive(NettyClientHandler.this);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        logger.info("메시지를 받는 동작이 끝나면 동작하는 메소드.");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent e = (IdleStateEvent) evt;
            if(e.state() == IdleState.READER_IDLE){
                //..처리할 동작
                if(LIMIT_COUNT > ++lostCnt_) { //n번을 초과하지 않았기때문에 메시지 재발송
                    msgSend(ctx);
                } else {
                    if(this.action_ != null){
                        action_.close(NettyClientHandler.this);
                    }
                }
            }
        }
    }
    
    private void msgSend(ChannelHandlerContext ctx){
        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMsg.getBytes());
        ctx.writeAndFlush(messageBuffer); //메시지를 발송하고 flush처리
        logger.info("발송 메시지 > " + sendMsg);
    }
}
