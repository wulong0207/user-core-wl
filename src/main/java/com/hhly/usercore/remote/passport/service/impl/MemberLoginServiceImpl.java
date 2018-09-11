package com.hhly.usercore.remote.passport.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.RegularValidateUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.base.util.TokenUtil;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserResultBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.security.dao.UserSecurityDaoMapper;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;
import com.hhly.usercore.remote.passport.service.IMemberLoginService;
import com.hhly.usercore.remote.passport.service.IMemberRegisterService;


/**
* @desc 用户登录实现类
* @author zhouyang
* @date 2017年2月9日
* @company 益彩网络科技公司
* @version 1.0
*/
@Service("iMemberLoginService")
public class MemberLoginServiceImpl implements IMemberLoginService {
	
	@Autowired
	private UserInfoDaoMapper userInfoDaoMapper;

	@Resource
	private PublicMethod publicMethod;
	
	@Resource
	private RedisUtil redisUtil;
	
	@Autowired
	private IVerifyCodeService verifyCodeService;
	
	@Autowired
	private UserSecurityDaoMapper userSecurityDaoMapper;
	
	@Autowired
	private IMemberRegisterService memberRegisterService;
	
	@Autowired
	private UserUtil userUtil;

	@Override
	public ResultBO<?> loginValidate(PassportVO passportVO) {
		UserInfoVO userInfoVO = new UserInfoVO();
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		//验证帐号类型
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
			//加入手机前三位验证
			ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getUserName());
			if (validateMobile.isError()) {
				return validateMobile;
			}
			userInfoVO.setMobile(passportVO.getUserName());
			userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
			userInfoVO.setIsMobileLogin(UserConstants.IS_TRUE);
		} else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)) {
			userInfoVO.setEmail(passportVO.getUserName());
			userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
			userInfoVO.setIsEmailLogin(UserConstants.IS_TRUE);
		} else {
			userInfoVO.setAccount(passportVO.getUserName());
			UserResultBO resultBO = new UserResultBO();
			UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.account(passportVO.getUserName()));
			if (ObjectUtil.isBlank(userInfoBO)) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
			}
			if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
				if (!ObjectUtil.isBlank(userInfoBO.getMobile()) && userInfoBO.getIsMobileLogin().equals(UserConstants.IS_TRUE)) {
					resultBO.setMobile(StringUtil.hideString(userInfoBO.getMobile(),(short)3));
					return ResultBO.err(MessageCodeConstants.HAVE_SET_MOBILE, resultBO, null);
				}
				if (!ObjectUtil.isBlank(userInfoBO.getEmail()) && userInfoBO.getIsEmailLogin().equals(UserConstants.IS_TRUE)) {
					resultBO.setEmail(StringUtil.hideString(userInfoBO.getEmail(),(short)4));
					return  ResultBO.err(MessageCodeConstants.HAVE_SET_EMAIL, resultBO, null);
				}
				if (!ObjectUtil.isBlank(userInfoBO.getQqOpenID())) {
					return ResultBO.err(MessageCodeConstants.NOW_USEING_THE_QQ_LOGIN);
				}
				if (!ObjectUtil.isBlank(userInfoBO.getWechatUnionID())) {
					return ResultBO.err(MessageCodeConstants.NOW_USEING_THE_WECHAT_LOGIN);
				}
				if (!ObjectUtil.isBlank(userInfoBO.getSinaBlogOpenID())) {
					return ResultBO.err(MessageCodeConstants.NOW_USEING_THE_WEIBO_LOGIN);
				}
			}
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		UserInfoBO resultInfoBO = new UserInfoBO();
		resultInfoBO.setAccount(userInfoBO.getAccount());
		resultInfoBO.setEmail(userInfoBO.getEmail());
		resultInfoBO.setMobile(userInfoBO.getMobile());
		if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
			resultInfoBO.setIsSetPassword(UserConstants.IS_FALSE);
		} else {
			resultInfoBO.setIsSetPassword(UserConstants.IS_TRUE);
		}
		if (!ObjectUtil.isBlank(userInfoBO.getMobile())) {
			resultInfoBO.setMobile(StringUtil.hideString(userInfoBO.getMobile(), (short)3));
		}
		if (!ObjectUtil.isBlank(userInfoBO.getEmail())) {
			resultInfoBO.setEmail(StringUtil.hideString(userInfoBO.getEmail(), (short)4));
		}
		//获取头像url
		publicMethod.setHeadUrl(userInfoBO, resultInfoBO);
        return ResultBO.ok(resultInfoBO);
	}

	@Override
	public ResultBO<?> validateUser(PassportVO passportVO) {
		UserInfoVO userInfoVO = new UserInfoVO();
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		//验证帐号类型
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
			//加入手机前三位验证
			ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getUserName());
			if (validateMobile.isError()) {
				return validateMobile;
			}
			userInfoVO.setMobile(passportVO.getUserName());
			userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
			userInfoVO.setIsMobileLogin(UserConstants.IS_TRUE);
		} else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)) {
			userInfoVO.setEmail(passportVO.getUserName());
			userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
			userInfoVO.setIsEmailLogin(UserConstants.IS_TRUE);
		} else {
			userInfoVO.setAccount(passportVO.getUserName());
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		UserInfoBO resultInfoBO = new UserInfoBO();
		resultInfoBO.setAccount(userInfoBO.getAccount());
		resultInfoBO.setEmail(userInfoBO.getEmail());
		resultInfoBO.setMobile(userInfoBO.getMobile());
		if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
			resultInfoBO.setIsSetPassword(UserConstants.IS_FALSE);
		} else {
			resultInfoBO.setIsSetPassword(UserConstants.IS_TRUE);
		}
		if (!ObjectUtil.isBlank(userInfoBO.getMobile())) {
			resultInfoBO.setMobile(StringUtil.hideString(userInfoBO.getMobile(), (short)3));
		}
		if (!ObjectUtil.isBlank(userInfoBO.getEmail())) {
			resultInfoBO.setEmail(StringUtil.hideString(userInfoBO.getEmail(), (short)4));
		}
		//获取头像url
		publicMethod.setHeadUrl(userInfoBO, resultInfoBO);
		return ResultBO.ok(resultInfoBO);
	}
	

	@Override
	public ResultBO<?> loginUserByCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		UserInfoVO userInfoVO = new UserInfoVO();
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
			//加入手机前三位验证
			ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getUserName());
			if (validateMobile.isError()) {
				return validateMobile;
			}
			passportVO.setMobile(passportVO.getUserName());
			userInfoVO.setMobile(passportVO.getUserName());
			return loginUserByMobileCode(passportVO);
		} else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)) {
			passportVO.setEmail(passportVO.getUserName());
			userInfoVO.setEmail(passportVO.getUserName());
			return loginUserByEmailCode(passportVO);
		} else {
			userInfoVO.setAccount(passportVO.getUserName());
			UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
			if(ObjectUtil.isBlank(userInfoBO)){
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
			}
			//1,判断手机
			if (!ObjectUtil.isBlank(userInfoBO.getMobile()) && UserConstants.IS_TRUE.equals(userInfoBO.getIsMobileLogin())) {
				passportVO.setMobile(userInfoBO.getMobile());
				return loginUserByMobileCode(passportVO);
			}
			//2,判断邮件
			else if (!ObjectUtil.isBlank(userInfoBO.getEmail()) && UserConstants.IS_TRUE.equals(userInfoBO.getIsEmailLogin())) {
				passportVO.setEmail(userInfoBO.getEmail());
				return loginUserByEmailCode(passportVO);
			}
			return ResultBO.err(MessageCodeConstants.USER_INFO_ERROR_SYS);
		}
	}
	
	@Override
	public ResultBO<?> loginUserByMobileCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getMobile());
		if (validateMobile.isError()) {
			return validateMobile;
		}
		UserInfoBO validatePwd = userInfoDaoMapper.findUserInfo(publicMethod.mobile(passportVO.getMobile()));
		if (ObjectUtil.isBlank(validatePwd)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		if (!ObjectUtil.isBlank(validatePwd.getPassword())) {
			return ResultBO.err(MessageCodeConstants.PLEASE_CHANGE_PASSWORD_LOGIN);
		}
		if (validatePwd.getAccountStatus().equals(UserConstants.IS_FALSE) &&
				(ObjectUtil.isBlank(validatePwd.getForbitEndTime()) || new Date().after(validatePwd.getForbitEndTime()))) {
			UserInfoPO infoPO = new UserInfoPO();
			infoPO.setAccountStatus(UserConstants.IS_TRUE);
			infoPO.setId(validatePwd.getId());
			userInfoDaoMapper.updateUserInfo(infoPO);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.mobile(passportVO.getMobile()));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		//验证账户是否禁用
		if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		//验证账户手机登录是否开启
		if (userInfoBO.getIsMobileLogin().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_LOGIN_IS_NOT_OPEN_SERVICE);
		}
		ResultBO<?> resultBO = verifyCodeService.checkVerifyCode(passportVO);
		if(!ObjectUtil.isBlank(resultBO) && resultBO.isError()){
			return resultBO;
		}

		//获取token
		String token = TokenUtil.createTokenStr(passportVO.getPlatform());
		//返回信息加密处理
		publicMethod.toRedis(userInfoBO, userInfoBO, token, UserConstants.LoginTypeEnum.MOBILE.getKey());
		//登录成功，添加日志
		publicMethod.inserLoginSuccessLog(passportVO.getIp(), userInfoBO);
		//获取头像信息
		publicMethod.setHeadUrl(userInfoBO, userInfoBO);
		userInfoBO.setLoginPlatform(passportVO.getPlatform());
		//添加至缓存
		publicMethod.modifyCache(token, userInfoBO, passportVO.getPlatform());
		//返回信息加密处理
		publicMethod.encryption(userInfoBO, userInfoBO);
		return ResultBO.ok(userInfoBO);
	}

	/**
	 * 备用方法（验证码登录暂不使用邮箱去验证登录）
	 */
	@Override
	public ResultBO<?> loginUserByEmailCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> validateEmail = ValidateUtil.validateEmail(passportVO.getEmail());
		if (validateEmail.isError()) {
			return validateEmail;
		}
		UserInfoBO validatePwd = userInfoDaoMapper.findUserInfo(publicMethod.email(passportVO.getEmail()));
		if (ObjectUtil.isBlank(validatePwd)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		if (!ObjectUtil.isBlank(validatePwd.getPassword())) {
			return ResultBO.err(MessageCodeConstants.PLEASE_CHANGE_PASSWORD_LOGIN);
		}
		if (validatePwd.getAccountStatus().equals(UserConstants.IS_FALSE) &&
				(ObjectUtil.isBlank(validatePwd.getForbitEndTime()) || new Date().after(validatePwd.getForbitEndTime()))) {
			UserInfoPO infoPO = new UserInfoPO();
			infoPO.setAccountStatus(UserConstants.IS_TRUE);
			infoPO.setId(validatePwd.getId());
			userInfoDaoMapper.updateUserInfo(infoPO);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.email(passportVO.getEmail()));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		//验证账号是否禁用
		if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		//验证账户邮箱登录是否开启
		if (userInfoBO.getIsEmailLogin().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_LOGIN_IS_NOT_OPEN_SERVICE);
		}
		ResultBO<?> resultBO = verifyCodeService.checkVerifyCode(passportVO);
		if (resultBO.isError() && !ObjectUtil.isBlank(resultBO)) {
			return resultBO;
		}
		//获取token
		String token = TokenUtil.createTokenStr(passportVO.getPlatform());
		//返回信息加密处理
		publicMethod.toRedis(userInfoBO, userInfoBO, token, UserConstants.LoginTypeEnum.EMAIL.getKey());
		//添加日志，登录成功
		publicMethod.inserLoginSuccessLog(passportVO.getIp(), userInfoBO);
		//获取头像信息
		publicMethod.setHeadUrl(userInfoBO, userInfoBO);
		userInfoBO.setLoginPlatform(passportVO.getPlatform());
		//添加至缓存
		publicMethod.modifyCache(token, userInfoBO, passportVO.getPlatform());
		//返回信息加密处理
		publicMethod.encryption(userInfoBO, userInfoBO);
		return ResultBO.ok(userInfoBO);
	}

	@Override
	public ResultBO<?> loginUserByUserName(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		//验证帐号类型-手机号
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
			//加入手机前三位验证
			ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getUserName());
			if (validateMobile.isError()) {
				return validateMobile;
			}
			UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.mobile(passportVO.getUserName()));
			if (ObjectUtil.isBlank(userInfoBO)) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_OR_NOT_OPEN_MOBILE_LOGIN);
			}
			if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE) &&
					(ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) || new Date().after(userInfoBO.getForbitEndTime()))) {
				UserInfoPO infoPO = new UserInfoPO();
				infoPO.setAccountStatus(UserConstants.IS_TRUE);
				infoPO.setId(userInfoBO.getId());
				userInfoDaoMapper.updateUserInfo(infoPO);
			}
			//验证账户手机登录是否开启
			if (userInfoBO.getIsMobileLogin().equals(UserConstants.IS_FALSE)) {
				return ResultBO.err(MessageCodeConstants.MOBILE_LOGIN_IS_NOT_OPEN_SERVICE);
			}
			int logCount = userSecurityDaoMapper.findUserOprateCount(publicMethod.setLogVOValue(userInfoBO));
			if (logCount > UserConstants.SEND_MAX) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_AND_PASSWORD_IS_NOT_MATCH_SERVICE_OUT_OF_TEN_TIMES);
			}
			//验证账号是否设置密码
			if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_SET_PASSWORD_SERVICE);
			}
			//粗略的验证密码格式
			ResultBO<?> validatePassword = ValidateUtil.validatePassword(passportVO.getPassword());
			if (validatePassword.isError()) {
				return validatePassword;
			}
			//验证帐号和密码是否匹配
			try {
				if (!EncryptUtil.encrypt(passportVO.getPassword(), userInfoBO.getrCode()).equals(userInfoBO.getPassword())) {
                    //添加日志
                    publicMethod.insertOperateLog(userInfoBO.getId(), UserConstants.UserOperationEnum.LOGIN_FAIL_PASSWORD.getKey(), UserConstants.IS_FALSE,  passportVO.getIp(),null, null, UserConstants.UserOperationEnum.LOGIN_FAIL_PASSWORD.getValue());
                    return ResultBO.err(MessageCodeConstants.ACCOUNT_AND_PASSWORD_IS_NOT_MATCH_SERVICE);
                }
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			//再次查询，把用户部分信息存入缓存
			UserInfoBO resultInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.mobile(passportVO.getUserName()));
			//验证账户是否禁用
			if (resultInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
					|| (!ObjectUtil.isBlank(resultInfoBO.getForbitEndTime()) && new Date().before(resultInfoBO.getForbitEndTime()))) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
			}
			//获取token
			String token = TokenUtil.createTokenStr(passportVO.getPlatform());
			//返回信息加密处理
			publicMethod.toRedis(userInfoBO, resultInfoBO, token, UserConstants.LoginTypeEnum.MOBILE.getKey());
			//登录成功，添加日志
			publicMethod.inserLoginSuccessLog(passportVO.getIp(), userInfoBO);
			//获取头像信息
			publicMethod.setHeadUrl(userInfoBO, resultInfoBO);
			resultInfoBO.setLoginPlatform(passportVO.getPlatform());
			//添加至缓存
			publicMethod.modifyCache(token, resultInfoBO, passportVO.getPlatform());
			//返回信息加密处理
			publicMethod.encryption(userInfoBO, resultInfoBO);
			//添加登录类型
			return ResultBO.ok(resultInfoBO);
		//验证帐号类型-邮箱
		} else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)){
			UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.email(passportVO.getUserName()));
			//验证账号是否存在
			if (ObjectUtil.isBlank(userInfoBO)) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_OR_NOT_OPEN_EMAIL_LOGIN);
			}
			if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE) &&
					(ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) || new Date().after(userInfoBO.getForbitEndTime()))) {
				UserInfoPO infoPO = new UserInfoPO();
				infoPO.setAccountStatus(UserConstants.IS_TRUE);
				infoPO.setId(userInfoBO.getId());
				userInfoDaoMapper.updateUserInfo(infoPO);
			}
			//验证账户邮箱登录是否开启
			if (userInfoBO.getIsEmailLogin().equals(UserConstants.IS_FALSE)) {
				return ResultBO.err(MessageCodeConstants.EMAIL_LOGIN_IS_NOT_OPEN_SERVICE);
			}
			int logCount = userSecurityDaoMapper.findUserOprateCount(publicMethod.setLogVOValue(userInfoBO));
			if (logCount > UserConstants.SEND_MAX) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_AND_PASSWORD_IS_NOT_MATCH_SERVICE_OUT_OF_TEN_TIMES);
			}
			//验证账号是否设置密码
			if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
				return ResultBO.err(MessageCodeConstants.PASSWORD_IS_NULL_FIELD);
			}
			ResultBO<?> validatePassword = ValidateUtil.validatePassword(passportVO.getPassword());
			if (validatePassword.isError()) {
				return validatePassword;
			}
			//验证帐号和密码是否匹配
			try {
				if (!EncryptUtil.verify(passportVO.getPassword(), userInfoBO.getPassword(), userInfoBO.getrCode())) {
                    //登录失败，添加日志
                    publicMethod.insertOperateLog(userInfoBO.getId(), UserConstants.UserOperationEnum.LOGIN_FAIL_PASSWORD.getKey(), UserConstants.IS_FALSE,  passportVO.getIp(),null, null, UserConstants.UserOperationEnum.LOGIN_FAIL_PASSWORD.getValue());
                    return ResultBO.err(MessageCodeConstants.ACCOUNT_AND_PASSWORD_IS_NOT_MATCH_SERVICE);
                }
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			//再次查询，把用户部分信息存入缓存
			UserInfoBO resultInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.email(passportVO.getUserName()));
			//验证账户是否禁用
			if (resultInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
					|| (!ObjectUtil.isBlank(resultInfoBO.getForbitEndTime()) && new Date().before(resultInfoBO.getForbitEndTime()))) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
			}
			//获取token
			String token = TokenUtil.createTokenStr(passportVO.getPlatform());
			//返回信息加密处理
			publicMethod.toRedis(userInfoBO, resultInfoBO, token, UserConstants.LoginTypeEnum.EMAIL.getKey());
			//登录成功，添加日志
			publicMethod.inserLoginSuccessLog(passportVO.getIp(), userInfoBO);
			//获取头像信息
			publicMethod.setHeadUrl(userInfoBO, resultInfoBO);
			resultInfoBO.setLoginPlatform(passportVO.getPlatform());
			//添加至缓存
			publicMethod.modifyCache(token, resultInfoBO, passportVO.getPlatform());
			//返回信息加密处理
			publicMethod.encryption(userInfoBO, resultInfoBO);
			return ResultBO.ok(resultInfoBO);
		//验证帐号类型-帐户名
		} else {
			UserInfoVO userInfoVO = new UserInfoVO();
			userInfoVO.setAccount(passportVO.getUserName());
			UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
			//验证账号是否存在
			if (ObjectUtil.isBlank(userInfoBO)) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
			}
			if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE) &&
					(ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) || new Date().after(userInfoBO.getForbitEndTime()))) {
				UserInfoPO infoPO = new UserInfoPO();
				infoPO.setAccountStatus(UserConstants.IS_TRUE);
				infoPO.setId(userInfoBO.getId());
				userInfoDaoMapper.updateUserInfo(infoPO);
			}
			int logCount = userSecurityDaoMapper.findUserOprateCount(publicMethod.setLogVOValue(userInfoBO));
			if (logCount > UserConstants.SEND_MAX) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_AND_PASSWORD_IS_NOT_MATCH_SERVICE_OUT_OF_TEN_TIMES);
			}
			//验证账号是否设置密码
			if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_SET_PASSWORD_SERVICE);
			}
			ResultBO<?> validatePassword = ValidateUtil.validatePassword(passportVO.getPassword());
			if (validatePassword.isError()) {
				return validatePassword;
			}
			//验证帐号和密码是否匹配
			try {
				if (!EncryptUtil.verify(passportVO.getPassword(), userInfoBO.getPassword(), userInfoBO.getrCode())) {
                    //登录失败，添加日志
                    publicMethod.insertOperateLog(userInfoBO.getId(), UserConstants.UserOperationEnum.LOGIN_FAIL_PASSWORD.getKey(), UserConstants.IS_FALSE,  passportVO.getIp(),null, null, UserConstants.UserOperationEnum.LOGIN_FAIL_PASSWORD.getValue());
                    return ResultBO.err(MessageCodeConstants.ACCOUNT_AND_PASSWORD_IS_NOT_MATCH_SERVICE);
                }
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			//再次查询，把用户部分信息存入缓存
			UserInfoBO resultInfoBO = userInfoDaoMapper.findUserInfoToCache(userInfoVO);
			//验证账户是否禁用
			if (resultInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
					|| (!ObjectUtil.isBlank(resultInfoBO.getForbitEndTime()) && new Date().before(resultInfoBO.getForbitEndTime()))) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
			}
			//获取token
			String token = TokenUtil.createTokenStr(passportVO.getPlatform());
			publicMethod.toRedis(userInfoBO, resultInfoBO, token, UserConstants.LoginTypeEnum.ACCOUNT.getKey());
			//登录成功，添加日志
			publicMethod.inserLoginSuccessLog(passportVO.getIp(), userInfoBO);
			//获取头像信息
			publicMethod.setHeadUrl(userInfoBO, resultInfoBO);
			resultInfoBO.setLoginPlatform(passportVO.getPlatform());
			//添加至缓存
			publicMethod.modifyCache(token, resultInfoBO, passportVO.getPlatform());
			//返回信息加密处理
			publicMethod.encryption(userInfoBO, resultInfoBO);
			return ResultBO.ok(resultInfoBO);
		}
	}
	
	@Override
	public ResultBO<?> loginUserAutomation(UserInfoBO userInfoBO) {
		//获取token
		String token = TokenUtil.createTokenStr(userInfoBO.getPlatform());
		//添加token至对象中
		userInfoBO.setToken(token);
		publicMethod.getUserIndexBO(userInfoBO, userInfoBO.getId());
		userInfoBO.setSafeIntegral(publicMethod.getSafeIntegration(userInfoBO));
		if (!ObjectUtil.isBlank(userInfoBO.getRealName()) && !ObjectUtil.isBlank(userInfoBO.getIdCard())) {
			userInfoBO.setAttestationRealName(UserConstants.IS_TRUE);
		} else {
			userInfoBO.setAttestationRealName(UserConstants.IS_FALSE);
		}
		//添加日志，登录成功
		publicMethod.inserLoginSuccessLog(userInfoBO.getIp(), userInfoBO);
		if (!ObjectUtil.isBlank(userInfoBO.getPassword())) {
			userInfoBO.setIsSetPassword(UserConstants.IS_TRUE);
		} else {
			userInfoBO.setIsSetPassword(UserConstants.IS_FALSE);
		}
		//获取头像信息
		publicMethod.setHeadUrl(userInfoBO, userInfoBO);
		userInfoBO.setLoginPlatform(userInfoBO.getPlatform());
		//添加至缓存
		publicMethod.modifyCache(token, userInfoBO, userInfoBO.getPlatform());
		//返回信息加密处理
		publicMethod.encryption(userInfoBO, userInfoBO);
		return ResultBO.ok(userInfoBO);
	}

	@Override
	public ResultBO<?> loginFastByMobile(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		passportVO.setMobile(passportVO.getUserName());
		ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getMobile());
		if (validateMobile.isError()) {
			return validateMobile;
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.mobile(passportVO.getMobile()));
		if (ObjectUtil.isBlank(userInfoBO)) {//该账号未注册或未开启手机号码登录
			return memberRegisterService.regUser(passportVO);
		}else {//登录
			//验证账户是否禁用
			if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
			}
			ResultBO<?> resultBO = verifyCodeService.checkVerifyCode(passportVO);
			if(!ObjectUtil.isBlank(resultBO) && resultBO.isError()){
				return resultBO;
			}
			//获取token
			String token = TokenUtil.createTokenStr(passportVO.getPlatform());
			//返回信息加密处理
			publicMethod.toRedis(userInfoBO, userInfoBO, token, UserConstants.LoginTypeEnum.MOBILE.getKey());
			//登录成功，添加日志
			publicMethod.inserLoginSuccessLog(passportVO.getIp(), userInfoBO);
			//获取头像信息
			publicMethod.setHeadUrl(userInfoBO, userInfoBO);
			userInfoBO.setLoginPlatform(passportVO.getPlatform());
			//添加至缓存
			publicMethod.modifyCache(token, userInfoBO, passportVO.getPlatform());
			//返回信息加密处理
			publicMethod.encryption(userInfoBO, userInfoBO);
			return ResultBO.ok(userInfoBO);
		}
	}


	@Override
	public ResultBO<?> exitUser(PassportVO passportVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		//清除缓存
		userUtil.clearUserToken(passportVO.getToken());
		return ResultBO.ok();
	}



}
