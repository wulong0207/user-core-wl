package com.hhly.usercore.remote.member.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.UserModifyLogVO;
import com.hhly.usercore.persistence.security.po.UserModifyLogPO;

/**
 * 用户操作日志接口
 * @desc
 * @author zhouyang
 * @date 2017年2月15日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface IMemberSecurityService {

	/**
	 * 添加日志
	 * @param userModifyLogPO 日志对象
	 * @return
	 */
	int addModifyLog(UserModifyLogPO userModifyLogPO);

    /**
     * 根据用户id查询操作日志
	 * @param userModifyLogVO
     * @return
     */
	ResultBO<?> selectByUserId(UserModifyLogVO userModifyLogVO);
	
	/**
	 * 展示用户安全中心信息
	 * @param userModifyLogVO
	 * @return
	 */
	ResultBO<?> findUserModifyLogByUserId(UserModifyLogVO userModifyLogVO);

	/**
	 * 查询帐号登录记录
	 * @param userModifyLogVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> showUserLoginData(UserModifyLogVO userModifyLogVO);
}
