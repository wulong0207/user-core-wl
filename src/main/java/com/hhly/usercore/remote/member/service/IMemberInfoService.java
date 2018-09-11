package com.hhly.usercore.remote.member.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.usercore.persistence.member.po.UserInfoPO;

/**
 * 用户个人信息接口 （远程服务）
 * @desc
 * @author zhouyang
 * @date 2017年3月4日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface IMemberInfoService {


    /**
     * 根据用户ID获取用户设置的勿扰模式
     * @param userInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> getDisturb (UserInfoVO userInfoVO);

    /**
     * 消息设置-勿扰模式，设置勿扰时间段
     * @param passportVO 参数
     * @return object
     * @throws Exception 异常
     */
    ResultBO<?> doNotDisturb (PassportVO passportVO);

	/**
	 * 验证昵称
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> validateNickname(PassportVO passportVO);
	
	/**
	 * 用户首页 - 展示用户基本信息和钱包信息
	 * @param userInfoVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> showUserIndex(UserInfoVO userInfoVO);
	
	/**
	 * 个人资料 - 展示用户基本信息及银行卡信息
	 * @param userInfoVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> showUserPersonalData(UserInfoVO userInfoVO);

	/**
	 * 查询帐号登录记录
	 * @param userInfoVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> showUserLoginData(UserInfoVO userInfoVO);
	
	/**
	 * 验证密码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkPassword(PassportVO passportVO);
	
	/**
	 * 验证手机号
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkMobile(PassportVO passportVO);
	
	/**
	 * 验证邮箱
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkEmail(PassportVO passportVO);
	
	/**
	 * 验证银行卡号
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkCreditCardNum(PassportVO passportVO);

	/**
	 * 验证身份证号码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> checkIDCard(PassportVO passportVO);
	
	/**
	 * 修改昵称
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> updateNickname(PassportVO passportVO);

	/**
	 * 修改帐户名
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> updateAccount(PassportVO passportVO);

	/**
	 * 修改密码，pc
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> updatePassword(PassportVO passportVO);
	
	/**
	 * 登记手机号码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> updateMobile(PassportVO passportVO);
	
	/**
	 * 登记邮箱
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> updateEmail(PassportVO passportVO);
	
	/**
	 * 开启手机号登录
	 * @param userInfoVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> openMobileLogin(UserInfoVO userInfoVO);
	
	/**
	 * 关闭手机号登录
	 * @param userInfoVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> closeMobileLogin(UserInfoVO userInfoVO);
	
	/**
	 * 开启邮箱登录
	 * @param userInfoVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> openEmailLogin(UserInfoVO userInfoVO);
	
	/**
	 * 关闭邮箱登录
	 * @param userInfoVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> closeEmailLogin(UserInfoVO userInfoVO);

	/**
	 * 设置密码
	 * @param passportVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> setPassword(PassportVO passportVO);

	/**
	 * 上传头像
	 * @param userInfoVO
	 * @return
	 * @throws Exception
	 */
	ResultBO<?> uploadHeadPortrait(UserInfoVO userInfoVO);

	/**
	 * 
	 * @Description: 重新设置redis,key有效时间
	 * @param key
	 * @param second
	 * @throws Exception
	 * @author wuLong
	 * @date 2017年3月22日 下午2:41:50
	 */
	void expireRedisKey(String key,long second);

	/**  
	* 方法说明: 更新用户的最后一次支付ID
	* @auth: xiongJinGang
	* @param userInfoPO
	* @throws Exception
	* @time: 2017年5月15日 下午8:30:28
	* @return: int 
	*/
	int updateLastUsePayId(UserInfoPO userInfoPO);

	/**
	 * 方法说明: 验证登录token的有效性
	 * @auth: xiongjingang
	 * @param token 登录token
	 * @throws Exception
	 * @time: 2017年3月15日 下午4:55:32
	 * @return: ResultBO<?>
	 */
	ResultBO<?> findUserInfoByToken(String token);

	/**
	 * 验证手机号前三位号码段是否匹配
	 * @param mobile
	 * @return
	 */
	ResultBO<?> verifyMobile(String mobile);

	/**
	 * 验证帐号登录状态
	 * @param token
	 * @return
	 */
	ResultBO<?> getTokenStatus(String token);
}
