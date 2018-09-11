package com.hhly.usercore.persistence.member.dao;

import com.hhly.skeleton.user.bo.UserMessageBO;
import com.hhly.skeleton.user.vo.UserMessageVO;
import com.hhly.usercore.persistence.message.po.UserMessagePO;

/**
 * 验证码管理mapper接口
 * @desc
 * @author zhouyang
 * @date 2017年2月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface VerifyCodeDaoMapper {

	/**
	 * 验证码验证
	 * @param userMessageVO
	 * @return
	 */
	UserMessageBO findVerifyCode(UserMessageVO userMessageVO);
	
	/**
	 * 查询短信条数
	 * @param userMessageVO
	 * @return
	 */
	int findVerifyCodeCount(UserMessageVO userMessageVO);

	/**
	 * 查询上一条发送的验证码
	 * @param userMessageVO
	 * @return
	 */
	UserMessageBO findPreviousCode(UserMessageVO userMessageVO);
	
	/**
	 * 修改验证码状态
	 * @param messagePO
	 * @return
	 */
	int updateVerifyCodeStatus(UserMessagePO messagePO);

	/**
	 * 验证码入库
	 * @param messagePO
	 * @return
	 */
	int addVerifyCode(UserMessagePO messagePO);

}
