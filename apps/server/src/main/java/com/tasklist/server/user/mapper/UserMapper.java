package com.tasklist.server.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tasklist.server.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("SELECT * FROM users WHERE openid = #{openid} LIMIT 1")
    UserEntity selectOneByOpenid(String openid);
}
