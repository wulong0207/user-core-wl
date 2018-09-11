package com.hhly.usercore.remote.passport.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.vo.PassportVO;

/**
 * 用户登录后端hessian接口 (user-core)
 * @desc 
 * @author zhouyang
 * @date 2017年2月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface IMemberLoginService {
	
	/**
	 * 登录-验证帐号
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> loginValidate(PassportVO passportVO);

	/**
	 * 验证帐号
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> validateUser(PassportVO passportVO);
	
	/**
	 * 登录
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> loginUserByCode(PassportVO passportVO);
	
	/**
	 * 登录-手机验证码
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> loginUserByMobileCode(PassportVO passportVO);
	
	/**
	 * 登录-邮箱验证码
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> loginUserByEmailCode(PassportVO passportVO);
	
	/**
	 * 登录-帐号(手机号/邮箱/帐户名)密码登录
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> loginUserByUserName(PassportVO passportVO);
	
	/**
	 * 注册后自动登录
	 * @param userInfoBO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> loginUserAutomation(UserInfoBO userInfoBO);
	
	/**
	 * 手机号快速登录，手机号不存在或没有开启手机号登录就注册
	 * @return
	 */
	ResultBO<?> loginFastByMobile(PassportVO passportVO);
	
	/**
	 * 用户退出(清缓存,清cookie)
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> exitUser(PassportVO passportVO);
	
}
