package com.alrimjang.mapper;

import com.alrimjang.model.entity.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    Users findByUsername(@Param("username") String username);

    void insertUser(Users users);
}
