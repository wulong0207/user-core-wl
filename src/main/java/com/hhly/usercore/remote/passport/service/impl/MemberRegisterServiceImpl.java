package com.hhly.usercore.remote.passport.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.constants.UserConstants.LoginTypeEnum;
import com.hhly.skeleton.base.constants.UserConstants.MessageTypeEnum;
import com.hhly.skeleton.base.constants.UserConstants.UserOperationEnum;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.RegularValidateUtil;
import com.hhly.skeleton.base.util.TokenUtil;
import com.hhly.skeleton.user.bo.RealNameBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.pay.dao.UserWalletMapper;
import com.hhly.usercore.persistence.pay.po.UserWalletPO;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;
import com.hhly.usercore.remote.passport.service.IMemberLoginService;
import com.hhly.usercore.remote.passport.service.IMemberRegisterService;

/**
 * 
 * @desc 用户注册实现类
 * @author zhouyang
 * @date 2017年2月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */

@Service("iMemberRegisterService")
public class MemberRegisterServiceImpl implements IMemberRegisterService {

	private static final Logger logger = Logger.getLogger(MemberRegisterServiceImpl.class);

	@Autowired
	private UserInfoDaoMapper userInfoDaoMapper;

	@Autowired
	private UserWalletMapper userWalletMapper;

	@Autowired
	private IVerifyCodeService verifyCodeService;

	@Autowired
	private IMemberLoginService memberLoginService;

	@Resource
	PublicMethod publicMethod;

	@Autowired
	private UserUtil userUtil;

	@Override
	public ResultBO<?> validateAccount(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
		}
		ResultBO<?> validateAccount = ValidateUtil.validateAccount(passportVO.getAccount());
		if (validateAccount.isError()) {
			return validateAccount;
		}
		ResultBO<?> checkKeyword = publicMethod.checkKeyword(passportVO.getAccount());
		if (checkKeyword.isError()) {
			return checkKeyword;
		}
		Integer id = null;
		if (!ObjectUtil.isBlank(passportVO.getToken())) {
			// 验证token
			ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
			if (validateToken.isError()) {
				return validateToken;
			}
			UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
			if (ObjectUtil.isBlank(tokenInfo)) {
				return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
			}
			id = tokenInfo.getId();
		}
		int userCount = userInfoDaoMapper.findUserInfoByAccount(publicMethod.account(id, passportVO.getAccount(), passportVO.getAccount()));
		if (userCount > UserConstants.ZERO_INTEGER) {
			return ResultBO.err(MessageCodeConstants.USERNAME_IS_REGISTERED_SERVICE);
		}
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> regUser(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
			// 加入手机前三位验证
			ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getUserName());
			if (validateMobile.isError()) {
				return validateMobile;
			}
			passportVO.setMobile(passportVO.getUserName());
			return regUserByMobile(passportVO);
		} else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)) {
			passportVO.setEmail(passportVO.getUserName());
			return regUserByEmail(passportVO);
		} else {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_FORMAT_ERROR_FILED);
		}
	}

	@Override
	public ResultBO<?> regUserByMobile(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> resultBO = verifyCodeService.checkVerifyCode(passportVO); // 验证验证码，绑定手机号
		if (resultBO.isError()) {
			return resultBO;
		}
		UserInfoVO userInfoVO = new UserInfoVO();
		userInfoVO.setMobile(passportVO.getMobile());
		userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
		UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
		if (!ObjectUtil.isBlank(bo)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO(UserConstants.IS_TRUE, UserConstants.IS_TRUE, UserConstants.IS_FALSE, UserConstants.IS_FALSE,
				UserConstants.IS_TRUE, UserConstants.IS_FALSE);
		if (ObjectUtil.isBlank(passportVO.getChannelId())) {
			userInfoPO.setChannelId(UserConstants.OTHER_CHANNEL);
		} else {
			userInfoPO.setChannelId(passportVO.getChannelId());
		}
		String account = null;
		try {
			account = TokenUtil.getUserNameByMobile(passportVO.getMobile());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		userInfoPO.setAccount(account);
		userInfoPO.setNickName(account);
		userInfoPO.setMobile(passportVO.getMobile());
		userInfoPO.setAccountModify(UserConstants.ModifyTypeEnum.NO_MODIFY.getKey());
		userInfoPO.setPlatform(passportVO.getPlatform());
		// 新增插入 代理系统编码
		if (!ObjectUtil.isBlank(passportVO.getAgentCode())) {
			userInfoPO.setAgentCode(passportVO.getAgentCode());
		}
		userInfoPO.setUserType(UserConstants.UserTypeEnum.USER.getValue());
		userInfoDaoMapper.addUser(userInfoPO);
		logger.info("验证通过，入库");
		// 添加日志
		publicMethod.insertOperateLog(userInfoPO.getId(), UserOperationEnum.REGISTER_SUCCESS.getKey(), UserConstants.IS_TRUE,
				passportVO.getIp(), null, passportVO.getMobile(), UserOperationEnum.REGISTER_SUCCESS.getValue());
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.mobile(passportVO.getMobile()));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		// 添加登录类型
		userInfoBO.setLoginType(LoginTypeEnum.MOBILE.getKey());
		UserWalletPO userWalletPO = new UserWalletPO(userInfoBO.getId(), UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO,
				UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.IS_TRUE, UserConstants.ZERO, 1);
		userWalletMapper.addUserWallet(userWalletPO);
		userInfoBO.setPlatform(passportVO.getPlatform());
		// 注册成功后，自动登录
		userInfoBO.setIp(passportVO.getIp());
		return memberLoginService.loginUserAutomation(userInfoBO);
	}

	@Override
	public ResultBO<?> regUserByEmail(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> resultBO = verifyCodeService.checkVerifyCode(passportVO); // 验证验证码，绑定手机号
		if (resultBO.isError()) {
			return resultBO;
		}
		UserInfoVO userInfoVO = new UserInfoVO();
		userInfoVO.setEmail(passportVO.getEmail());
		userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
		UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
		if (!ObjectUtil.isBlank(bo)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_IS_REGISTERED_SERVICE);
		}
		String account = null;
		try {
			account = TokenUtil.getUserNameByEmail(passportVO.getEmail());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		UserInfoVO infoVO = new UserInfoVO();
		infoVO.setAccount(account);
		UserInfoBO infoBO = userInfoDaoMapper.findUserInfo(infoVO);
		UserInfoPO userInfoPO = new UserInfoPO(UserConstants.IS_FALSE, UserConstants.IS_FALSE, UserConstants.IS_TRUE, UserConstants.IS_TRUE,
				UserConstants.IS_TRUE, UserConstants.IS_FALSE);
		if (ObjectUtil.isBlank(passportVO.getChannelId())) {
			userInfoPO.setChannelId(UserConstants.OTHER_CHANNEL);
		} else {
			userInfoPO.setChannelId(passportVO.getChannelId());
		}
		userInfoPO.setEmail(passportVO.getEmail());
		userInfoPO.setPlatform(passportVO.getPlatform());
		userInfoPO.setAccountModify(UserConstants.ModifyTypeEnum.NO_MODIFY.getKey());
		if (ObjectUtil.isBlank(infoBO)) {
			userInfoPO.setAccount(account);
		} else {
			userInfoPO.setAccount(account + EncryptUtil.randomCode());
		}
		userInfoPO.setNickName(account);
		// 新增插入 代理系统编码
		if (!ObjectUtil.isBlank(passportVO.getAgentCode())) {
			userInfoPO.setAgentCode(passportVO.getAgentCode());
		}
		userInfoPO.setUserType(UserConstants.UserTypeEnum.USER.getValue());
		userInfoDaoMapper.addUser(userInfoPO);
		logger.info("======================验证通过，注册成功" + userInfoPO.getId());
		// 添加日志
		publicMethod.insertOperateLog(userInfoPO.getId(), UserOperationEnum.REGISTER_SUCCESS.getKey(), UserConstants.IS_TRUE,
				passportVO.getIp(), null, passportVO.getEmail(), UserOperationEnum.REGISTER_SUCCESS.getValue());
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.email(passportVO.getEmail()));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		// 添加登录类型
		userInfoBO.setLoginType(LoginTypeEnum.EMAIL.getKey());
		UserWalletPO userWalletPO = new UserWalletPO(userInfoBO.getId(), UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO,
				UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.IS_TRUE, UserConstants.ZERO, 1);
		userWalletMapper.addUserWallet(userWalletPO);
		userInfoBO.setPlatform(passportVO.getPlatform());
		// 注册成功，自动登录
		userInfoBO.setIp(passportVO.getIp());
		return memberLoginService.loginUserAutomation(userInfoBO);
	}

	@Override
	public ResultBO<?> randomCreateUserName(PassportVO passportVO) {
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		String account = "";
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		try {
			// 生成随机帐户名
			if (!ObjectUtil.isBlank(tokenInfo.getEmail())) {
				account = TokenUtil.getUserNameByMobile(tokenInfo.getMobile());
			} else if (!ObjectUtil.isBlank(tokenInfo.getMobile())) {
				account = TokenUtil.getUserNameByEmail(tokenInfo.getEmail());
			} else {
				return ResultBO.err(MessageCodeConstants.SYS_ERROR_SYS);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setAccount(account);
		userInfoPO.setNickName(account);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		tokenInfo.setAccount(account);
		tokenInfo.setNickname(account);
		// 更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> perfectAccountAndPassword(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		// 验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.id(tokenInfo.getId()));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		// 从缓存中获取用户id，根据用户Id查库判断该用户是否设置了帐号密码
		if (!ObjectUtil.isBlank(userInfoBO.getPassword())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_ALREADY_SET_USERNAME_AND_PASSWORD_SERVICE);
		}
		ResultBO<?> validateAccount = ValidateUtil.validateAccount(passportVO.getAccount());
		if (validateAccount.isError()) {
			return validateAccount;
		}
		ResultBO<?> checkKeyword = publicMethod.checkKeyword(passportVO.getAccount());
		if (checkKeyword.isError()) {
			return checkKeyword;
		}
		ResultBO<?> validatePassword = ValidateUtil.validatePassword(passportVO.getPassword());
		if (validatePassword.isError()) {
			return validatePassword;
		}
		int userCount = userInfoDaoMapper
				.findUserInfoByAccount(publicMethod.account(tokenInfo.getId(), passportVO.getAccount(), passportVO.getAccount()));
		// 验证用户名是否已存在
		if (userCount > UserConstants.ZERO_INTEGER) {
			return ResultBO.err(MessageCodeConstants.USERNAME_IS_REGISTERED_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		String rCode = null;
		try {
			rCode = EncryptUtil.getSalt();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String password = null;
		try {
			password = EncryptUtil.encrypt(passportVO.getPassword(), rCode);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		userInfoPO.setAccount(passportVO.getAccount());
		userInfoPO.setNickName(passportVO.getAccount());
		
		userInfoPO.setrCode(rCode);
		userInfoPO.setPassword(password);
		userInfoPO.setId(tokenInfo.getId());
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		// 添加日志
		publicMethod.insertUpadatePwdLog(passportVO.getIp(), userInfoBO);
		tokenInfo.setAccount(passportVO.getAccount());
		tokenInfo.setNickname(passportVO.getAccount());
		// 更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> perfectRealName(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		// 验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.id(tokenInfo.getId()));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		// 验证帐号是否已实名认证
		if (!ObjectUtil.isBlank(tokenInfo.getRealName()) || !ObjectUtil.isBlank(tokenInfo.getIdCard())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_ALREADY_REALNAME_AUTHENTICTION_SERVICE);
		}
		ResultBO<?> validateRealName = ValidateUtil.validateRealName(passportVO.getRealName());
		if (validateRealName.isError()) {
			return validateRealName;
		}
		ResultBO<?> validateIdCard = null;
		try {
			validateIdCard = ValidateUtil.validateIdCard(passportVO.getIdCard());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (validateIdCard.isError()) {
			return validateIdCard;
		}
		if (!ObjectUtil.isBlank(tokenInfo.getIdCard()) || !ObjectUtil.isBlank(tokenInfo.getRealName())) {
			return ResultBO.err(MessageCodeConstants.REAL_IDENTITY_IS_DONE);
		}
		int count = userInfoDaoMapper.findUserInfoByIdCard(passportVO.getIdCard());
		// 验证身份证绑定账户个数
		if (count > UserConstants.IDCARD_BIND_MAX) {
			return ResultBO.err(MessageCodeConstants.THE_IDCARD_BIND_MAX_IS_SEX_FIELD);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setRealName(passportVO.getRealName());
		userInfoPO.setIdCard(passportVO.getIdCard().toLowerCase());
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		// 添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.REAL_NAME_AUTHENTICATION_SUCCESS.getKey(), UserConstants.IS_TRUE,
				passportVO.getIp(), null, passportVO.getRealName(), UserOperationEnum.REAL_NAME_AUTHENTICATION_SUCCESS.getValue());
		RealNameBO realNameBO = new RealNameBO();
		realNameBO.setAccount(userInfoBO.getAccount());
		UserInfoBO preRealName = userInfoDaoMapper.findUserInfo(publicMethod.id(tokenInfo.getId()));
		if (ObjectUtil.isBlank(preRealName)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (!ObjectUtil.isBlank(preRealName.getRealName()) && !ObjectUtil.isBlank(preRealName.getIdCard())) {
			realNameBO.setIsPreRealName(UserConstants.IS_TRUE);
		} else {
			realNameBO.setIsPreRealName(UserConstants.IS_FALSE);
		}
		tokenInfo.setRealName(passportVO.getRealName());
		tokenInfo.setIdCard(passportVO.getIdCard());
		// 更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok(realNameBO);
	}

	@Override
	public ResultBO<?> addMobile(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		// 验证手机号格式
		ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getMobile());
		if (validateMobile.isError()) {
			return validateMobile;
		}
		// 验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (!ObjectUtil.isBlank(tokenInfo.getMobile())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_REGISTERED_MOBILE);
		}
		UserInfoVO vo = new UserInfoVO();
		vo.setMobile(passportVO.getMobile());
		vo.setMobileStatus(UserConstants.IS_TRUE);
		UserInfoBO bo = userInfoDaoMapper.findUserInfo(vo);
		if (!ObjectUtil.isBlank(bo)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setMobile(passportVO.getMobile());
		userInfoPO.setId(tokenInfo.getId());
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		tokenInfo.setMobile(passportVO.getMobile());
		// 更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return publicMethod.resultBO(UserConstants.ADD_SUCCESS);
	}

	@Override
	public ResultBO<?> addEmail(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		// 验证邮箱格式
		ResultBO<?> validateEmail = ValidateUtil.validateEmail(passportVO.getEmail());
		if (validateEmail.isError()) {
			return validateEmail;
		}
		// 验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (!ObjectUtil.isBlank(tokenInfo.getEmail())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_REGISTERED_EMAIL);
		}
		UserInfoVO vo = new UserInfoVO();
		vo.setEmail(passportVO.getEmail());
		vo.setEmailStatus(UserConstants.IS_TRUE);
		UserInfoBO bo = userInfoDaoMapper.findUserInfo(vo);
		if (!ObjectUtil.isBlank(bo)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_IS_REGISTERED_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setEmail(passportVO.getEmail());
		userInfoPO.setId(tokenInfo.getId());
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		tokenInfo.setEmail(passportVO.getEmail());
		// 更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return publicMethod.resultBO(UserConstants.ADD_SUCCESS);
	}

	@Override
	public ResultBO<?> registerSuccess(PassportVO passportVO) {
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.id(tokenInfo.getId()));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		RealNameBO realNameBO = new RealNameBO();
		realNameBO.setAccount(userInfoBO.getAccount());
		if (!ObjectUtil.isBlank(userInfoBO.getRealName()) && !ObjectUtil.isBlank(userInfoBO.getIdCard())) {
			realNameBO.setIsPreRealName(UserConstants.IS_TRUE);
		} else {
			realNameBO.setIsPreRealName(UserConstants.IS_FALSE);
		}
		return ResultBO.ok(realNameBO);
	}

}
