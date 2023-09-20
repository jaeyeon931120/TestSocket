package com.kevinlab.mapper;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository("dataMapper")
public class DataMapper {

    @Autowired
    private SqlSession session;

    /**
     * 받아온 raw데이터를 DB에 insert(전기)
     * @param *map
     * @exception Exception Exception
     */
    public void insert(Map<String, Object> param) throws Exception {
        System.out.println("params: " + param);
        this.session.insert("dataMapper.insertData", param);
    }
}
