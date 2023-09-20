package com.kevinlab.netty.service;

import java.util.Map;

public interface NettyService {

    // Netty로 온 데이터 분석
    Map<String, Object> protocol(String[] Content, byte[] b) throws Exception;
}
