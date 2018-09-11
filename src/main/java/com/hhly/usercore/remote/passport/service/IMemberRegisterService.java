package com.hhly.usercore.remote.passport.service;


import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.PassportVO;

/**
 * 
 * @desc 用户注册后端hessian接口（user-core）
 * @author zhouyang
 * @date 2017年2月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface IMemberRegisterService {

	/**
	 * 验证帐号
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> validateAccount(PassportVO passportVO);
	
	/**
	 * 封装手机注册和email注册
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> regUser(PassportVO passportVO);
	
	
	/**
	 * 注册-手机号注册(1)
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> regUserByMobile(PassportVO passportVO);
	
	
	/**
	 * 注册-邮箱注册(1)
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> regUserByEmail(PassportVO passportVO);
	
	/**
	 * 跳过，随机生成一个用户名
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> randomCreateUserName(PassportVO passportVO);
	
	/**
	 * 注册-设置帐号和密码(2)
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> perfectAccountAndPassword(PassportVO passportVO);
	
	/**
	 * 注册-实名认证(3)
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> perfectRealName(PassportVO passportVO);
	
	/**
	 * 登记手机号
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> addMobile(PassportVO passportVO);
	
	/**
	 * 登记邮箱地址
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> addEmail(PassportVO passportVO);
	
	/**
	 * 注册成功返回数据
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> registerSuccess(PassportVO passportVO);
	
}
