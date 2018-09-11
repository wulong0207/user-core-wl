package com.hhly.usercore.persistence.member.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.hhly.usercore.persistence.member.po.UserIpPO;

public interface UserIpDaoMapper {

	/**
	 * ip模糊查询ip端全部对象
	 * @param ip
	 * @return
	 * @date 2018年4月26日上午10:34:46
	 * @author cheng.chen
	 */
    List<UserIpPO> findAreaByIpLike (@Param("ip") String ip);
    
    /**
     * 通过存储过程查询地区信息
     * @param params
     * @return
     * @date 2018年4月26日上午10:34:33
     * @author cheng.chen
     */
    String findAreaByIp(Map<String, String> params);
}