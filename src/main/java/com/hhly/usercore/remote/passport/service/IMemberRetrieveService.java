package com.hhly.usercore.remote.passport.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.PassportVO;

/**
 * 
 * @desc 找回信息后端hessian接口（user-core）
 * @author zhouyang
 * @date 2017年2月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface IMemberRetrieveService {
	
	/**
	 * 找回密码-验证-帐户(1)
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkUserName(PassportVO passportVO);

	/**
	 * 验证绑定的手机号
	 * @param passportVO
	 * @return
	 */
	ResultBO<?> checkFdMobileCode(PassportVO passportVO);

	/**
	 * 验证绑定的邮箱地址
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkFdEmailCode(PassportVO passportVO);

	/**
	 * 验证银行卡
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkBankCard(PassportVO passportVO);
	
	/**
	 * 找回密码-修改－密码(4)
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> updatePassword(PassportVO passportVO);
}
