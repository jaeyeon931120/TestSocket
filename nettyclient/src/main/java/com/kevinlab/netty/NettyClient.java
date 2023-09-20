package com.kevinlab.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NettyClient {
    private static final Logger logger = LogManager.getLogger(NettyClient.class);

    private Bootstrap bs = new Bootstrap();
    private SocketAddress addr_;
    private Channel channel_;
    private String[] msgArr;
    private int idx;
    private NioEventLoopGroup group;
    private int fail_cnt = 0;
    private final int FAIL_COUNT_LIMIT = 3;

    public NettyClient(SocketAddress addr, String[] msgArr){
        this.addr_ = addr;
        this.msgArr = msgArr;
    }
    public NettyClient(String host, int port, String[] msgArr){
        this(new InetSocketAddress(host, port), msgArr);
    }

    //실제로 동작시킬 메소드 Bootstrap 연결 옵션 설정 및 연결 처리
    public void run(){
        if(this.addr_ == null){
            logger.error("주소 정보가 없습니다.");
        }else if(this.msgArr == null || this.msgArr.length == 0){
            logger.error("보낼 메시지가 없습니다.");
        }

        group = new NioEventLoopGroup(3);
        bs.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);

        doConnect();
    }

    private void doConnect(){
        handlerSet();

        //옵션 처리가 끝난 후 연결처리
        bs.connect(addr_).addListener(new ChannelFutureListener(){
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    logger.info("연결 성공");
                    logger.info(addr_ + "connect()");
                    channel_ = future.channel();
                } else {
                    future.channel().close();
                    if(FAIL_COUNT_LIMIT > ++fail_cnt){
                        logger.info("연결 실패 " + fail_cnt + "/" + FAIL_COUNT_LIMIT);
                        bs.connect(addr_).addListener(this);
                    } else {
                        logger.info(FAIL_COUNT_LIMIT + "회 연결 초과");
                        bs.group().shutdownGracefully(); // eventLoop에 등록된 Thread를 종료 처리한다.
                    }
                }
            }
        });
    }

    private void handlerSet(){
        if(bs != null){
            bs.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //idle 등록
                    ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(5, 0, 0));

                    NettyClientHandler handler = new NettyClientHandler(msgArr[idx]);
                    handler.setCallBackClientHandler(new NettyClientAction(){
                        public void close(NettyClientHandler handler){
                            closeAndContinue();
                        }

                        public void receive(NettyClientHandler handler){
                            String receiveMsg = handler.getReceiveMsg();
                            closeAndContinue();
                        }
                    });

                    ch.pipeline().addLast("clientHandler", handler);
                }
            });
        }
    }

    private void closeAndContinue(){
        try {
            channel_.close().sync(); // 현재의 채널을 일단 닫는다.
            if(msgArr.length > ++idx){ // 보낼 메시지가 남았으면 재연결 처리
                doConnect();
            } else { // 보낼 메시지가 없다면 종료
               bs.group().shutdownGracefully(); // eventLoop에 등록된 Thread를 종료 처리한다.
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
