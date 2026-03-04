package com.alrimjang.mapper;

import com.alrimjang.model.entity.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    Users findByUsername(@Param("username") String username);

    List<Users> findAllUsers();

    int updateNoticeWritePermission(@Param("userId") String userId,
                                    @Param("canPostNotice") boolean canPostNotice);

    void insertUser(Users users);
}
