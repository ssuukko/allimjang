package com.alrimjang.mapper;

import com.alrimjang.model.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMapper {
    List<Group> findAll();
    List<Group> findByUserId(@Param("userId") String userId);
    int countMemberByGroupIdAndUserId(@Param("groupId") String groupId, @Param("userId") String userId);
    List<Group> findChatByUserId(@Param("userId") String userId);
    int countChatMemberByGroupIdAndUserId(@Param("groupId") String groupId, @Param("userId") String userId);
    int insertGroup(Group group);
    int insertGroupMember(@Param("groupId") String groupId, @Param("userId") String userId);
}
