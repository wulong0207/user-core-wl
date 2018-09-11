package com.hhly.usercore.persistence.operate.dao;

import org.apache.ibatis.annotations.Param;

import com.hhly.usercore.persistence.operate.po.OperateMarketChannelPO;

public interface OperateMarketChannelDaoMapper {
	
	OperateMarketChannelPO findByChannelId(@Param("channelId") String channelId);
	
	String findTopChannelId(@Param("channelId") String channelId, @Param("rownum") Short rownum);
}