package com.kevinlab.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper
public class DataMapper {

    @Autowired
    private SqlSession sql;

    /**
     * 받아온 raw데이터를 DB에 insert(전기)
     * @param *map
     * @exception Exception Exception
     */
    public int insert(Map<String, Object> param) throws Exception {
        return this.sql.insert("DataMapper.insertData", param);
    }
}
