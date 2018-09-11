package com.hhly.usercore.remote.member.service;


import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.PassportVO;

/**
 * @desc 验证码管理后端hessian接口（user-core）
 * @author zhouyang
 * @date 2017年2月13日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface IVerifyCodeService {
	
	/**
	 * 获取手机/邮箱验证码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> sendVerifyCode(PassportVO passportVO);
	
	/**
	 * 获取手机验证码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> sendMobileVerifyCode(PassportVO passportVO);
	
	/**
	 * 获取邮箱验证码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> sendEmailVerifyCode(PassportVO passportVO);

	/**
	 * 找回密码 - 获取手机验证码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> sendFdMobileVerifyCode(PassportVO passportVO);

	/**
	 * 找回密码 - 获取邮箱验证码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> sendFdEmailVerifyCode(PassportVO passportVO);

	/**
	 * 获取新手机号验证码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> sendNewMobileVerifyCode(PassportVO passportVO);
	
	/**
	 * 获取新邮箱地址验证码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> sendNewEmailVerifyCode(PassportVO passportVO);
	
	/**
	 * 获取普通验证码
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> sendCommonVerifyCode();
	
	/**
	 * 验证验证码有效性
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkVerifyCode(PassportVO passportVO);

	/**
	 * 找回密码 - 验证手机号码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkFdMobileVerifyCode(PassportVO passportVO);

	/**
	 * 找回密码 - 验证邮箱地址
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkFdEmailVerifyCode(PassportVO passportVO);
	
	/**
	 * 验证,手机验证码(除注册业务以外的其它)
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> checkMobileVerifyCode(PassportVO passportVO);
	
	/**
	 * 验证,邮箱验证码(除注册业务以外的其它)
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> checkEmailVerifyCode(PassportVO passportVO);
	
	/**
	 * 验证帐号以及验证码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkCommonVerifyCode(PassportVO passportVO);
	
	/**
	 * 验证新手机号码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkNewMobileVerifyCode(PassportVO passportVO);
	
	/**
	 * 验证新邮箱地址
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkNewEmailVerifyCode(PassportVO passportVO);

	/**
	 * 验证token
	 * @param token 缓存key
	 * @returnN
	 */
	ResultBO<?> validateToken(String token);
}
