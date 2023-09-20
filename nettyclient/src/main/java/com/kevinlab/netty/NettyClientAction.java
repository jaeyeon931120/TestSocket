package com.kevinlab.netty;

public interface NettyClientAction {
    public void close(NettyClientHandler handler);
    public void receive(NettyClientHandler handler);
}
