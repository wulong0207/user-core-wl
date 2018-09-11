package com.hhly.usercore.persistence.member.dao;

import org.apache.ibatis.annotations.Param;

import com.hhly.usercore.persistence.member.po.UserInfoLastLogPO;

public interface UserInfoLastLogDaoMapper {
	
	UserInfoLastLogPO findLastLogByUserId(@Param("userId") Integer userId);
}