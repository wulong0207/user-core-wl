package com.hhly.usercore.base.common.service;

import com.hhly.skeleton.user.vo.UserMessageVO;

/**
 * 发送短信息接口
 * @desc
 * @author zhouyang
 * @date 2017年2月14日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface SmsService {

	/**
	 * 发送短信
	 * @param userMessageVO 验证码对象
	 * @return
	 */
	boolean doSendSms(UserMessageVO userMessageVO) throws Exception;
	
	/**
	 * 发送邮件
	 * @param userMessageVO 验证码对象
	 * @return
	 */
	boolean doSendMail(UserMessageVO userMessageVO) throws Exception;
}
