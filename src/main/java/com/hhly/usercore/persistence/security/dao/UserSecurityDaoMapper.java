package com.hhly.usercore.persistence.security.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.hhly.skeleton.user.bo.MUserIpBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserInfoLastLogBO;
import com.hhly.skeleton.user.bo.UserModifyLogBO;
import com.hhly.skeleton.user.vo.UserModifyLogVO;
import com.hhly.usercore.persistence.security.po.UserModifyLogPO;

/**
 * 用户操作日志
 * @desc
 * @author zhouyang
 * @date 2017年2月15日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface UserSecurityDaoMapper {
	
	/**
	 * 用户添加日志
	 * @param userModifyLogPO
	 * @return
	 */
	int addModifyLog(UserModifyLogPO userModifyLogPO);

	/**
	 * 添加用户登录异步日志
	 * @param userModifyLogPO
	 * @return
	 */
	int addLoginLog(UserModifyLogPO userModifyLogPO);
    
    
    List<MUserIpBO> queryProvinceLike (@Param("ip") String ip);
    
    /**
     * 展示安全中心个人资料
	 * @param userId
	 * @return
	 */
	UserInfoBO findUserInfoByUserId(Integer userId);
	
	/**
	 * 查询用户日志
	 * @param userModifyLogVO
	 * @return
	 */
	List<UserModifyLogBO> findModifyLog(UserModifyLogVO userModifyLogVO);

	/**
	 *
	 * @param userModifyLogVO
	 * @return
	 */
	int deleteUserLoginRecord(UserModifyLogVO userModifyLogVO);

	/**
     * 查询用户操作记录条数
     * @param userModifyLogVO
     * @return
     */
    int findUserOprateCount(UserModifyLogVO userModifyLogVO);

	/**
	 * 查询帐号最后登录的那条记录
	 * @param userId
	 * @return
	 */
	UserInfoLastLogBO findLastLoginDetail(Integer userId);

	int selectCount(UserModifyLogVO userModifyLogVO);

    List<UserModifyLogBO> selectPage(UserModifyLogVO userModifyLogVO);
    
}
