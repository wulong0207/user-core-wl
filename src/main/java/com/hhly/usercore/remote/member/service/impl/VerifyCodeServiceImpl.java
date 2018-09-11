package com.hhly.usercore.remote.member.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.constants.UserConstants.MessageTypeEnum;
import com.hhly.skeleton.base.constants.UserConstants.UserOperationEnum;
import com.hhly.skeleton.base.constants.UserConstants.VerifyCodeTypeEnum;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.RegularValidateUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.base.util.TokenUtil;
import com.hhly.skeleton.user.bo.BankCardDetailBO;
import com.hhly.skeleton.user.bo.CommonCodeBO;
import com.hhly.skeleton.user.bo.SendSmsCountBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserMessageBO;
import com.hhly.skeleton.user.bo.UserResultBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.skeleton.user.vo.UserMessageVO;
import com.hhly.skeleton.user.vo.UserModifyLogVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.common.service.SmsService;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.member.dao.BankcardMapper;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.dao.VerifyCodeDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.message.po.UserMessagePO;
import com.hhly.usercore.persistence.security.dao.UserSecurityDaoMapper;
import com.hhly.usercore.persistence.security.po.UserModifyLogPO;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;


/**
 * 验证码处理（发送、接收验证）实现
 * @desc
 * @author zhouyang
 * @date 2017年2月8日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("iVerifyCodeService")
public class VerifyCodeServiceImpl implements IVerifyCodeService {

	public static final Logger logger = Logger.getLogger(VerifyCodeServiceImpl.class);

	@Autowired
	private VerifyCodeDaoMapper verifyCodeDaoMapper;

	@Autowired
	private SmsService smsService;

	@Autowired
	private UserSecurityDaoMapper userSecurityDaoMapper;

	@Autowired
	private UserInfoDaoMapper userInfoDaoMapper;

	@Autowired
	private BankcardMapper bankcardMapper;

	@Autowired
	private RedisUtil redisUtil;
	
	@Autowired
	private UserUtil userUtil;

	@Resource
	private PublicMethod publicMethod;

	@Value("${before_file_url}")
	private String before_file_url;



	@Override
	public ResultBO<?> sendVerifyCode(PassportVO passportVO) {
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		if (!passportVO.getSendType().equals(MessageTypeEnum.LOG_MSG.getKey()) && !passportVO.getSendType().equals(MessageTypeEnum.REG_MSG.getKey())
				&& !passportVO.getSendType().equals(MessageTypeEnum.FAST_LOGIN_MSG.getKey())) {
			return ResultBO.err(MessageCodeConstants.SEND_TYPE_ERROR);
		}
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
			if(passportVO.getSendType().equals(MessageTypeEnum.FAST_LOGIN_MSG.getKey())) {//快速登录
				ResultBO<?> validateOneMinute = validateCodeSecound(CacheConstants.getMinuteKey(passportVO.getUserName(), passportVO.getSendType()), CacheConstants.THIRTY_SECONDS);
				if (validateOneMinute.isError()) {
					return validateOneMinute;
				}
			}else{
				ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(passportVO.getUserName(), passportVO.getSendType()));
				if (validateOneMinute.isError()) {
					return validateOneMinute;
				}
			}
			ResultBO<?> smsSendType = publicMethod.smsSendType(passportVO.getUserName(), passportVO.getSendType());
			if (smsSendType.isError()) {
				return smsSendType;
			}
			UserMessageVO messageVO = new UserMessageVO(passportVO.getUserName(), passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.SMS.getKey());
			UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
			String code = code(userMessageBO);
			String content  = "";
			Integer userId = null;
			if (passportVO.getSendType().equals(MessageTypeEnum.LOG_MSG.getKey())) {
				content = appendStr(code, MessageTypeEnum.LOG_MSG.getValue());
				UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.mobile(passportVO.getUserName()));
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
				UserInfoBO bo = userInfoDaoMapper.findUserInfoToCache(publicMethod.mobile(passportVO.getUserName()));
				if (ObjectUtil.isBlank(bo)) {
					return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
				}
				if (bo.getAccountStatus().equals(UserConstants.IS_FALSE)
						|| (!ObjectUtil.isBlank(bo.getForbitEndTime()) && new Date().before(bo.getForbitEndTime()))) {
					return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
				}
				userId = bo.getId();
			} if(passportVO.getSendType().equals(MessageTypeEnum.FAST_LOGIN_MSG.getKey())) {
				content = appendStr(code, MessageTypeEnum.FAST_LOGIN_MSG.getValue());
			}else {
				content = appendStr(code, MessageTypeEnum.REG_MSG.getValue());
			}
			return sendSms(userId, passportVO.getUserName(), code, content, passportVO.getSendType());
		} else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)) {
			ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(passportVO.getUserName(), passportVO.getSendType()));
			if (validateOneMinute.isError()) {
				return validateOneMinute;
			}
			ResultBO<?> mailSendType = publicMethod.mailSendType(passportVO.getUserName(), passportVO.getSendType());
			if (mailSendType.isError()) {
				return mailSendType;
			}
			UserMessageVO messageVO = new UserMessageVO(passportVO.getUserName(), passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.MAIL.getKey());
			UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
			String code = code(userMessageBO);
			String content = "";
			Integer userId = null;
			if (passportVO.getSendType().equals(MessageTypeEnum.LOG_MSG.getKey())) {
				content = this.appendHtml(code, MessageTypeEnum.LOG_MSG.getValue(), "");
				UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.email(passportVO.getUserName()));
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
				UserInfoBO bo = userInfoDaoMapper.findUserInfoToCache(publicMethod.email(passportVO.getUserName()));
				if (ObjectUtil.isBlank(bo)) {
					return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
				}
				if (bo.getAccountStatus().equals(UserConstants.IS_FALSE)
						|| (!ObjectUtil.isBlank(bo.getForbitEndTime()) && new Date().before(bo.getForbitEndTime()))) {
					return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
				}
				userId = bo.getId();
			} else {
				content = this.appendHtml(code, MessageTypeEnum.REG_MSG.getValue(), "");
			}
			return sendMail(userId, passportVO.getUserName(), code, content, passportVO.getSendType());
		} else {
			ResultBO<?> validateAccount = ValidateUtil.validateAccount(passportVO.getUserName());
			if (validateAccount.isError()) {
				return validateAccount;
			}
			UserInfoBO bo = userInfoDaoMapper.findUserInfoToCache(publicMethod.account(passportVO.getUserName()));
			if (ObjectUtil.isBlank(bo)) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
			}
			if (passportVO.getSendType().equals(MessageTypeEnum.LOG_MSG.getKey())) {
				if (!ObjectUtil.isBlank(bo.getMobile()) && bo.getIsMobileLogin().equals(UserConstants.IS_TRUE)) {
					ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(passportVO.getUserName(), passportVO.getSendType()));
					if (validateOneMinute.isError()) {
						return validateOneMinute;
					}
					ResultBO<?> smsSendType = publicMethod.smsSendType(bo.getMobile(), passportVO.getSendType());
					if (smsSendType.isError()) {
						return smsSendType;
					}
					UserMessageVO messageVO = new UserMessageVO(bo.getMobile(), passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.SMS.getKey());
					UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
					String code = code(userMessageBO);
					String content = appendStr(code,MessageTypeEnum.LOG_MSG.getValue());
					return sendSms(bo.getId(), bo.getMobile(), code, content, passportVO.getSendType());
				}
				if (!ObjectUtil.isBlank(bo.getEmail()) && bo.getIsEmailLogin().equals(UserConstants.IS_TRUE)) {
					ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(passportVO.getUserName(), passportVO.getSendType()));
					if (validateOneMinute.isError()) {
						return validateOneMinute;
					}
					ResultBO<?> mailSendType = publicMethod.mailSendType(bo.getEmail(), passportVO.getSendType());
					if (mailSendType.isError()) {
						return mailSendType;
					}
					UserMessageVO messageVO = new UserMessageVO(bo.getEmail(), passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.MAIL.getKey());
					UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
					String code = code(userMessageBO);
					String content = "";
					if (passportVO.getSendType().equals(MessageTypeEnum.LOG_MSG.getKey())) {
						content = this.appendHtml(code, MessageTypeEnum.LOG_MSG.getValue(), "");
					} else {
						content = this.appendHtml(code, MessageTypeEnum.REG_MSG.getValue(), "");
					}
					return sendMail(bo.getId(), bo.getEmail(), code, content, passportVO.getSendType());
				}
			} else {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_FORMAT_ERROR_FILED);
			}
			return ResultBO.ok();
		}
	}

	@Override
	public ResultBO<?> sendNewMobileVerifyCode(PassportVO passportVO) {
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (tokenInfo.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(tokenInfo.getForbitEndTime()) && new Date().before(tokenInfo.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		String mobile = "";
		
		MessageTypeEnum messageTypeEnum = MessageTypeEnum.getMessageTypeEnum(passportVO.getSendType());
		switch (messageTypeEnum) {
		case ADD_MSG:
			if (!ObjectUtil.isBlank(tokenInfo.getMobile()) && tokenInfo.getMobileStatus().equals(UserConstants.IS_FALSE)) {
				mobile = tokenInfo.getMobile();
				break;
			}
			mobile = passportVO.getMobile();
			ResultBO<?> validateMobile = ValidateUtil.validateMobile(mobile);
			if (validateMobile.isError()) {
				return validateMobile;
			}
			UserInfoVO userInfoVO = new UserInfoVO();
			userInfoVO.setMobile(mobile);
			userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
			UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
			if (!ObjectUtil.isBlank(bo)) {
				return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
			}
			break;
		case OTHER_MSG:
			mobile = tokenInfo.getMobile();
			break;
		}
	
		ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(mobile, passportVO.getSendType()));
		if (validateOneMinute.isError()) {
			return validateOneMinute;
		}
		UserMessageVO messageVO = new UserMessageVO(mobile, passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.SMS.getKey());
		UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
		String code = code(userMessageBO);
		String content = this.appendStr(code, messageTypeEnum.getValue());
		return sendSms(tokenInfo.getId(), mobile, code, content, passportVO.getSendType());
	}

	@Override
	public ResultBO<?> sendNewEmailVerifyCode(PassportVO passportVO) {
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}

		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (tokenInfo.getAccountStatus().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		String email = "";
		if (tokenInfo.getEmailStatus().equals(UserConstants.IS_TRUE)) {
			email = passportVO.getEmail();
			ResultBO<?> validateEmail = ValidateUtil.validateEmail(email);
			if (validateEmail.isError()) {
				return validateEmail;
			}
			UserInfoVO userInfoVO = new UserInfoVO();
			userInfoVO.setEmail(passportVO.getEmail());
			userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
			UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
			if (!ObjectUtil.isBlank(bo)) {
				return ResultBO.err(MessageCodeConstants.EMAIL_IS_REGISTERED_SERVICE);
			}
		} else {
			if (!ObjectUtil.isBlank(tokenInfo.getEmail())) {
				email = tokenInfo.getEmail();
			} else {
				email = passportVO.getEmail();
				ResultBO<?> validateEmail = ValidateUtil.validateEmail(email);
				if (validateEmail.isError()) {
					return validateEmail;
				}
				UserInfoVO userInfoVO = new UserInfoVO();
				userInfoVO.setEmail(passportVO.getEmail());
				UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
				if (!ObjectUtil.isBlank(bo)) {
					return ResultBO.err(MessageCodeConstants.EMAIL_IS_REGISTERED_SERVICE);
				}
			}
		}

		ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(email, passportVO.getSendType()));
		if (validateOneMinute.isError()) {
			return validateOneMinute;
		}
		UserMessageVO messageVO = new UserMessageVO(email, passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.MAIL.getKey());
		UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
		String code = code(userMessageBO);
		String content = this.appendHtml(code, MessageTypeEnum.ADD_MSG.getValue(), tokenInfo.getNickname());
		if (!ObjectUtil.isBlank(tokenInfo.getEmail()) && tokenInfo.getEmailStatus().equals(UserConstants.IS_FALSE)) {
			return sendMail(tokenInfo.getId(), tokenInfo.getEmail(), code, content, passportVO.getSendType());
		} else {
			return sendMail(tokenInfo.getId(), email, code, content, passportVO.getSendType());
		}
	}

	@Override
	public ResultBO<?> sendMobileVerifyCode(PassportVO passportVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (tokenInfo.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(tokenInfo.getForbitEndTime()) && new Date().before(tokenInfo.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(tokenInfo.getMobile(), passportVO.getSendType()));
		if (validateOneMinute.isError()) {
			return validateOneMinute;
		}
		//验证帐号是否登记手机号码
		if (ObjectUtil.isBlank(tokenInfo.getMobile())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_ADD_THIS_MOBILE_SERVICE);
		}
		UserMessageVO messageVO = new UserMessageVO(tokenInfo.getMobile(), passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.SMS.getKey());
		UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
		String code = code(userMessageBO);
		String content = this.appendStr(code, MessageTypeEnum.BIND_MSG.getValue());
		return sendSms(tokenInfo.getId(), tokenInfo.getMobile(), code, content, passportVO.getSendType());
	}

	@Override
	public ResultBO<?> sendEmailVerifyCode(PassportVO passportVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (tokenInfo.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(tokenInfo.getForbitEndTime()) && new Date().before(tokenInfo.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(tokenInfo.getEmail(), passportVO.getSendType()));
		if (validateOneMinute.isError()) {
			return validateOneMinute;
		}
		//验证帐号是否登记邮箱地址
		if (ObjectUtil.isBlank(tokenInfo.getEmail())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_ADD_THIS_EMAIL_SERVICE);
		}
		UserMessageVO messageVO = new UserMessageVO(tokenInfo.getEmail(), passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.MAIL.getKey());
		UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
		String code = code(userMessageBO);
		String content = this.appendHtml(code, MessageTypeEnum.ADD_MSG.getValue(), tokenInfo.getNickname());
		return sendMail(tokenInfo.getId(), tokenInfo.getEmail(), code, content, passportVO.getSendType());
	}

	@Override
	public ResultBO<?> sendFdMobileVerifyCode(PassportVO passportVO) {
		ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(passportVO.getMobile(), passportVO.getSendType()));
		if (validateOneMinute.isError()) {
			return validateOneMinute;
		}
		ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getMobile());
		if (validateMobile.isError()) {
			return validateMobile;
		}
		if (!passportVO.getSendType().equals(MessageTypeEnum.FOUND_MSG.getKey())) {
			return ResultBO.err(MessageCodeConstants.SEND_TYPE_ERROR);
		}
		PassportVO vo = new PassportVO();
		vo.setUserName(passportVO.getUserName());
		vo.setMobile(passportVO.getMobile());
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.fdMobile(vo));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_IS_NOT_FOUND_SERVICE);
		}
		if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		UserMessageVO messageVO = new UserMessageVO(passportVO.getMobile(), passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.SMS.getKey());
		UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
		String code = code(userMessageBO);
		String content = this.appendStr(code, MessageTypeEnum.FOUND_MSG.getValue());
		return sendSms(userInfoBO.getId(), passportVO.getMobile(), code, content, passportVO.getSendType());
	}

	@Override
	public ResultBO<?> sendFdEmailVerifyCode(PassportVO passportVO) {
		ResultBO<?> validateOneMinute = validateOneMinute(CacheConstants.getMinuteKey(passportVO.getEmail(), passportVO.getSendType()));
		if (validateOneMinute.isError()) {
			return validateOneMinute;
		}
		ResultBO<?> validateEmail = ValidateUtil.validateEmail(passportVO.getEmail());
		if (validateEmail.isError()) {
			return validateEmail;
		}
		if (!passportVO.getSendType().equals(MessageTypeEnum.FOUND_MSG.getKey())) {
			return ResultBO.err(MessageCodeConstants.SEND_TYPE_ERROR);
		}
		PassportVO vo = new PassportVO();
		vo.setUserName(passportVO.getUserName());
		vo.setEmail(passportVO.getEmail());
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.fdEmail(vo));
		//验证邮箱号码是否存在
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_IS_NOT_FOUND_SERVICE);
		}
		if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		UserMessageVO messageVO = new UserMessageVO(passportVO.getEmail(), passportVO.getSendType(), UserConstants.IS_FALSE, VerifyCodeTypeEnum.MAIL.getKey());
		UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
		String code = code(userMessageBO);
		String content = this.appendHtml(code, MessageTypeEnum.FOUND_MSG.getValue(), userInfoBO.getNickname());
		return sendMail(userInfoBO.getId(), passportVO.getEmail(), code, content, passportVO.getSendType());
	}

	@Override
	public ResultBO<?> sendCommonVerifyCode() {
		String code = EncryptUtil.getRandomCode4();
		//生成token
		String token = TokenUtil.createTokenStr() + code;
		CommonCodeBO commonCodeBO = new CommonCodeBO();
		commonCodeBO.setCode(code);
		commonCodeBO.setToken(token);
		//将验证码对象存入缓存，设置缓存时间为5分钟   sms_token
		redisUtil.addObj(CacheConstants.getCommonCodeCacheKey(token), commonCodeBO, CacheConstants.FIVE_MINUTES);
		return ResultBO.ok(commonCodeBO);
	}

	@Override
	public ResultBO<?> checkVerifyCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
			//验证发送类型
			ResultBO<?> smsSendType = publicMethod.smsSendType(passportVO);
			if (smsSendType.isError()) {
				return smsSendType;
			}
			if (passportVO.getSendType().equals(MessageTypeEnum.LOG_MSG.getKey())) {
				UserInfoBO infoBO = userInfoDaoMapper.findUserInfo(publicMethod.mobile(passportVO.getUserName()));
				if (infoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
						|| (!ObjectUtil.isBlank(infoBO.getForbitEndTime()) && new Date().before(infoBO.getForbitEndTime()))) {
					return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
				}
				return checkSms(null,passportVO, passportVO.getUserName(), infoBO.getId());
			} else {
				return checkSms(null,passportVO, passportVO.getUserName(),null);
			}
		} else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)) {
			ResultBO<?> mailSendType = publicMethod.mailSendType(passportVO);
			if (mailSendType.isError()) {
				return mailSendType;
			}
			if (passportVO.getSendType().equals(MessageTypeEnum.LOG_MSG.getKey())) {
				UserInfoBO infoBO = userInfoDaoMapper.findUserInfo(publicMethod.email(passportVO.getUserName()));
				if (infoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
						|| (!ObjectUtil.isBlank(infoBO.getForbitEndTime()) && new Date().before(infoBO.getForbitEndTime()))) {
					return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
				}
				return checkMail(null,passportVO, passportVO.getUserName(), infoBO.getId());
			} else {
				return checkMail(null,passportVO, passportVO.getUserName(), null);
			}

		} else {
			UserInfoBO userInfo = userInfoDaoMapper.findUserInfoToCache(publicMethod.account(passportVO.getUserName()));
			if (ObjectUtil.isBlank(userInfo)) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
			}
			if (passportVO.getSendType().equals(MessageTypeEnum.LOG_MSG.getKey())) {
				if (!ObjectUtil.isBlank(userInfo.getMobile()) && userInfo.getIsMobileLogin().equals(UserConstants.IS_TRUE)) {
					//验证发送类型
					ResultBO<?> smsSendType = publicMethod.smsSendType(userInfo.getMobile(), MessageTypeEnum.LOG_MSG.getKey());
					if (smsSendType.isError()) {
						return smsSendType;
					}
					return checkSms(null,passportVO, userInfo.getMobile(),userInfo.getId());
				}
				if (!ObjectUtil.isBlank(userInfo.getEmail()) && userInfo.getIsEmailLogin().equals(UserConstants.IS_TRUE)) {
					ResultBO<?> mailSendType = publicMethod.mailSendType(userInfo.getEmail(), MessageTypeEnum.LOG_MSG.getKey());
					if (mailSendType.isError()) {
						return mailSendType;
					}
					return checkMail(null,passportVO, userInfo.getEmail(), userInfo.getId());
				}
			} else {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_FORMAT_ERROR_FILED);
			}
			return ResultBO.ok();
		}
	}

	@Override
	public ResultBO<?> checkFdMobileVerifyCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
		}
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getMobile());
		if (validateMobile.isError()) {
			return validateMobile;
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.fdMobile(passportVO));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_IS_NOT_FOUND_SERVICE);
		}
		if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		return checkSms(null,passportVO, passportVO.getMobile(),userInfoBO.getId());
	}

	@Override
	public ResultBO<?> checkFdEmailVerifyCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
		}
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		ResultBO<?> validateEmail = ValidateUtil.validateEmail(passportVO.getEmail());
		if (validateEmail.isError()) {
			return validateEmail;
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.fdEmail(passportVO));
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_IS_NOT_FOUND_SERVICE);
		}
		if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		return checkMail(null,passportVO, passportVO.getEmail(), userInfoBO.getId());
	}

	@Override
	public ResultBO<?> checkNewMobileVerifyCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (tokenInfo.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(tokenInfo.getForbitEndTime()) && new Date().before(tokenInfo.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		String mobile = "";
		if (tokenInfo.getMobileStatus().equals(UserConstants.IS_TRUE)) {
			mobile = passportVO.getMobile();
			ResultBO<?> validateMobile = ValidateUtil.validateMobile(mobile);
			if (validateMobile.isError()) {
				return validateMobile;
			}
			UserInfoVO userInfoVO = new UserInfoVO();
			userInfoVO.setMobile(mobile);
			userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
			UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
			if (!ObjectUtil.isBlank(bo)) {
				return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
			}
		} else {
			if (!ObjectUtil.isBlank(tokenInfo.getMobile())) {
				mobile = tokenInfo.getMobile();
			} else {
				mobile = passportVO.getMobile();
				ResultBO<?> validateMobile = ValidateUtil.validateMobile(mobile);
				if (validateMobile.isError()) {
					return validateMobile;
				}
				UserInfoVO userInfoVO = new UserInfoVO();
				userInfoVO.setMobile(mobile);
				UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
				if (!ObjectUtil.isBlank(bo)) {
					return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
				}
			}
		}
		return checkSms(tokenInfo, passportVO, mobile, tokenInfo.getId());
	}

	@Override
	public ResultBO<?> checkNewEmailVerifyCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (tokenInfo.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(tokenInfo.getForbitEndTime()) && new Date().before(tokenInfo.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		String email = "";
		if (tokenInfo.getEmailStatus().equals(UserConstants.IS_TRUE)) {
			email = passportVO.getEmail();
			ResultBO<?> validateEmail = ValidateUtil.validateEmail(email);
			if (validateEmail.isError()) {
				return validateEmail;
			}
			UserInfoVO userInfoVO = new UserInfoVO();
			userInfoVO.setEmail(passportVO.getEmail());
			userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
			UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
			if (!ObjectUtil.isBlank(bo)) {
				return ResultBO.err(MessageCodeConstants.EMAIL_IS_REGISTERED_SERVICE);
			}
		} else {
			if (!ObjectUtil.isBlank(tokenInfo.getEmail())) {
				email = tokenInfo.getEmail();
			} else {
				email = passportVO.getEmail();
				ResultBO<?> validateEmail = ValidateUtil.validateEmail(email);
				if (validateEmail.isError()) {
					return validateEmail;
				}
				UserInfoVO userInfoVO = new UserInfoVO();
				userInfoVO.setEmail(passportVO.getEmail());
				UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
				if (!ObjectUtil.isBlank(bo)) {
					return ResultBO.err(MessageCodeConstants.EMAIL_IS_REGISTERED_SERVICE);
				}
			}
		}
		return checkMail(tokenInfo, passportVO, email, tokenInfo.getId());
	}

	@Override
	public ResultBO<?> checkMobileVerifyCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (tokenInfo.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(tokenInfo.getForbitEndTime()) && new Date().before(tokenInfo.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		return checkSms(tokenInfo, passportVO, userInfoBO.getMobile(),tokenInfo.getId());
	}

	@Override
	public ResultBO<?> checkEmailVerifyCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (tokenInfo.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(tokenInfo.getForbitEndTime()) && new Date().before(tokenInfo.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		return checkMail(tokenInfo, passportVO, userInfoBO.getEmail(), tokenInfo.getId());
	}

	@Override
	public ResultBO<?> checkCommonVerifyCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserIp(passportVO.getIp());
		userModifyLogVO.setOperationStatus(UserConstants.IS_FALSE);
		userModifyLogVO.setUserAction(UserOperationEnum.USERNAME_ERROR_FOUND_PASS.getKey());
		int count = userSecurityDaoMapper.findUserOprateCount(userModifyLogVO);
		if (count > UserConstants.SEND_MAX) {
			return ResultBO.err(MessageCodeConstants.USERNAME_ERROR_OUT_OF_TEN_TIMES);
		}
		UserInfoVO userInfoVO = new UserInfoVO();
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
		//验证帐号是否存在
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
		if (ObjectUtil.isBlank(userInfoBO)) {
			UserModifyLogPO userModifyLogPO = new UserModifyLogPO(null, UserOperationEnum.USERNAME_ERROR_FOUND_PASS.getKey(), UserConstants.IS_FALSE, passportVO.getIp(), null, null, UserOperationEnum.USERNAME_ERROR_FOUND_PASS.getValue());
			userSecurityDaoMapper.addModifyLog(userModifyLogPO);
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		CommonCodeBO tokenInfo = redisUtil.getObj(CacheConstants.getCommonCodeCacheKey(passportVO.getToken()), new CommonCodeBO());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
		}
		//判断验证码是否正确
		if (!passportVO.getCode().equals(tokenInfo.getCode())) {
			return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
		}
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		redisUtil.delObj(CacheConstants.getCommonCodeCacheKey(passportVO.getToken()));
		UserResultBO userResultBO = new UserResultBO();
		userResultBO.setAccount(userInfoBO.getAccount());
		userResultBO.setMobile(userInfoBO.getMobile());
		userResultBO.setMobileStatus(userInfoBO.getMobileStatus());
		userResultBO.setIsMobileLogin(userInfoBO.getIsMobileLogin());
		userResultBO.setEmail(userInfoBO.getEmail());
		userResultBO.setEmailStatus(userInfoBO.getEmailStatus());
		userResultBO.setIsEmailLogin(userInfoBO.getIsEmailLogin());
		List<BankCardDetailBO> bankCardList = bankcardMapper.findBankList(userInfoBO.getId());
		if (!ObjectUtil.isBlank(bankCardList)) {
			if (!ObjectUtil.isBlank(bankCardList)) {
				userResultBO.setDefualtCard(StringUtil.hideString(bankCardList.get(0).getCardcode() , (short)2));
			}
		}
		return ResultBO.ok(userResultBO);
	}

	/**
	 * 封装短信拼接
	 * @param code	验证码
	 * @return
	 * @throws Exception
	 */
	private String appendStr(String code, String operateType) {
		StringBuffer str = new  StringBuffer();
		str.append("您的验证码：");
		str.append(code);
		str.append("，您正在进行"+ operateType+"操作");
		str.append("，请勿向他人泄露");
		String msg = str.toString();
		return msg;
	}

	/**
	 * 拼接邮件内容，html格式
	 * @return
	 * @throws Exception
	 */
	private String appendHtml(String code, String operateType, String nickname) {
		StringBuffer str = new StringBuffer();
		str.append("<div style=\"width:60%;padding:20px 100px;border:1px solid #ddd;font-size:14px;color:#333;\">");
		str.append("<div style=\"overflow: hidden;padding:20px 0;border-bottom:1px solid #eee;\">");
		str.append("<span style=\"float: left;\"><img style=\"height:35px;\" src=\""+before_file_url+"_upload_images/base/2ncai.png\" alt=\"\"></span>");
		str.append("<span style=\"float: right;line-height:35px;\">2N彩票伴您中奖</span></div>");
		str.append("<div style=\"padding:30px 0\">");
		str.append("<p style=\"padding:15px 0;font-size:16px;\">亲爱的会员： <strong style=\"color:#FF7F00\">"+nickname+"</strong>您好！</p>");
		str.append("<p style=\"padding:15px 0;font-size:18px;\">您正在进行"+operateType+"操作，请在验证码输入框中输入： <strong style=\"color:#FF7F00\">"+code+"</strong>，以完成操作。</p>");
		str.append("<p style=\"padding:15px 0;color:#FF7F00;\">此验证码<strong>15</strong>分钟内有效！</p>");
		str.append("<p style=\"padding:15px 0;color:#999;font-size:12px;\">注意：此操作可能会修改您的密码、登录邮箱或绑定手机。如非本人操作，请及时登录并修改密码以保证帐户安全<br/>");
		str.append("（工作人员不会向你索取此验证码，请勿泄漏！)</p>");
		str.append("<div style=\"border-top:1px solid #ddd;padding:20px 0;margin-top:20px;\">此为系统邮件，请勿回复<br />请保管好您的邮箱，避免账号被他人盗用<br /><br />2N彩票版权所有@2017</div></div></div>");
		return str.toString();
	}

	@Override
	public ResultBO<?> validateToken(String token) {
		ResultBO<?> validateToken = ValidateUtil.validateToken(token);
		if (validateToken.isError()) {
			return validateToken;
		}
		return ResultBO.ok();
	}

	private String code(UserMessageBO userMessageBO) {
		String code = "";
		if (!ObjectUtil.isBlank(userMessageBO)) {
			if (DateUtil.getDifferenceTime(new Date(), userMessageBO.getCreateTime()) < CacheConstants.TEN_MINUTES_TO_MILLISECOND) {
				code = userMessageBO.getCode();
			} else {
				code = EncryptUtil.getRandomCode6();
			}
		} else {
			code = EncryptUtil.getRandomCode6();
		}
		return code;
	}

	private ResultBO<?> validateOneMinute (String oneMinutekey) {
		String oneMinuteValue = redisUtil.getString(oneMinutekey);
		if(!ObjectUtil.isBlank(oneMinuteValue)) {
			//一分钟之类，同一手机同一类型只能发送一条短信
			return ResultBO.err(MessageCodeConstants.ONE_MINUTE_TIPS);
		}
		return ResultBO.ok();
	}
	
	private ResultBO<?> validateCodeSecound (String key, Long secound) {
		String oneMinuteValue = redisUtil.getString(key);
		if(!ObjectUtil.isBlank(oneMinuteValue)) {
			//一分钟之类，同一手机同一类型只能发送一条短信
			return ResultBO.err(MessageCodeConstants.CODE_SECOUND_TIPS, secound);
		}
		return ResultBO.ok();
	}
	
	private ResultBO<?> sendSms(Integer userId, String userName, String code, String content, Short sendType) {
		//查询手机发送短信验证码条数
		UserMessageVO userMessageVO = new UserMessageVO(userName, code, content,
				sendType, VerifyCodeTypeEnum.SMS.getKey());
		int count = verifyCodeDaoMapper.findVerifyCodeCount(userMessageVO);
		//验证短信发送次数是否超出
		if (count > UserConstants.SEND_MAX) {
			return ResultBO.err(MessageCodeConstants.THE_VERIFYCODE_SEND_MAX_IS_TEN_FIELD);
		}
		boolean result = false;	//发送短信息请求
		try {
			result = smsService.doSendSms(userMessageVO);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!result) {
			return ResultBO.err(MessageCodeConstants.SMS_SEND_DEFEAT_SYS);
		}
		//设置接口请求时间间隔，快速登录30s，其它时候60s
		long cacheTime = sendType.equals(MessageTypeEnum.FAST_LOGIN_MSG.getKey()) ? CacheConstants.THIRTY_SECONDS
				: CacheConstants.ONE_MINUTES;
		redisUtil.addString(CacheConstants.getMinuteKey(userName, sendType), code, cacheTime);
		UserMessagePO userMessagePO = null;
		try {
			userMessagePO = new UserMessagePO(userId, userName, code, URLDecoder.decode(content, UserConstants.UTF_8),
                    sendType, UserConstants.IS_FALSE, VerifyCodeTypeEnum.SMS.getKey());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		verifyCodeDaoMapper.addVerifyCode(userMessagePO);
		redisUtil.addString(userName+sendType, code,CacheConstants.FIFTEEN_MINUTES);
		SendSmsCountBO sendSmsCountBO = new SendSmsCountBO(count+1);
		return ResultBO.ok(sendSmsCountBO);
	}

	private ResultBO<?> sendMail(Integer userId, String userName, String code, String content, Short sendType) {
		UserMessageVO userMessageVO = new UserMessageVO(userName, code, content,
				sendType, VerifyCodeTypeEnum.MAIL.getKey());
		//查询邮件发送条数
		int count = verifyCodeDaoMapper.findVerifyCodeCount(userMessageVO);
		//验证邮件发送数量是否超出
		if (count > UserConstants.SEND_MAX) {
			return ResultBO.err(MessageCodeConstants.THE_VERIFYCODE_SEND_MAX_IS_TEN_FIELD);
		}
		boolean result = false;	//发送邮件
		try {
			result = smsService.doSendMail(userMessageVO);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!result) {
			return ResultBO.err(MessageCodeConstants.MAIL_SEND_DEFEAT_SYS);
		}
		//设置接口请求时间间隔
		redisUtil.addString(CacheConstants.getMinuteKey(userName, sendType), code, CacheConstants.ONE_MINUTES);
		UserMessagePO userMessagePO = new UserMessagePO(userId, userName, code, content,
				sendType, UserConstants.IS_FALSE, VerifyCodeTypeEnum.MAIL.getKey());
		verifyCodeDaoMapper.addVerifyCode(userMessagePO);
		redisUtil.addString(userName+sendType, code, CacheConstants.FIFTEEN_MINUTES);
		SendSmsCountBO sendSmsCountBO = new SendSmsCountBO(count+1);
		return ResultBO.ok(sendSmsCountBO);
	}

	private ResultBO<?> checkSms(UserInfoBO tokenInfo,PassportVO passportVO,String mobile, Integer userId) {
		Integer errCount = 0;
		//redisUtil.delString(userId+mobile+CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT);
		String countStr = redisUtil.getString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+mobile);
		if (ObjectUtil.isBlank(countStr)) {
			countStr = "0";
		}
		errCount = Integer.valueOf(countStr);
		//验证码是否为空
		if (ObjectUtil.isBlank(passportVO.getCode())) {
			return ResultBO.err(MessageCodeConstants.VERIFYCODE_IS_NULL_FIELD);
		}
		String redisCode = redisUtil.getString(mobile+passportVO.getSendType());
		//验证验证码是否正确
		if (!passportVO.getCode().equals(redisCode)) {
			if (!passportVO.getSendType().equals((short) 2)) {
				errCount++;
				UserInfoPO infoPO = new UserInfoPO();
				infoPO.setId(userId);
				if (errCount == 3) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(DateUtil.addHour(new Date(),1));
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+mobile, errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(tokenInfo)) {
						tokenInfo.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(passportVO.getIp(),userId);
					userUtil.clearUserToken(passportVO.getToken());
					userUtil.clearUserById(userId);
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_THREE);
				} else if (errCount == 8) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(DateUtil.addHour(new Date(),3));
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+mobile, errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(tokenInfo)) {
						tokenInfo.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(passportVO.getIp(),userId);
					userUtil.clearUserToken(passportVO.getToken());
					userUtil.clearUserById(userId);
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_EIGHT);
				} else if (errCount > 10) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(getTime());
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+mobile, errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(tokenInfo)) {
						tokenInfo.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(passportVO.getIp(),userId);
					userUtil.clearUserToken(passportVO.getToken());
					userUtil.clearUserById(userId);
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_TEN);
				} else {
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+mobile, errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
				}
			}
			return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
		}
		UserMessagePO userMessagePO = new UserMessagePO(mobile, passportVO.getCode(), UserConstants.IS_TRUE);
		verifyCodeDaoMapper.updateVerifyCodeStatus(userMessagePO);
		//验证完成，清除缓存
		redisUtil.delString(mobile+passportVO.getSendType());
		return ResultBO.ok();
	}

	private ResultBO<?> checkMail(UserInfoBO tokenInfo, PassportVO passportVO, String email, Integer userId) {
		Integer errCount = 0;
		String countStr = redisUtil.getString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+email);
		if (ObjectUtil.isBlank(countStr)) {
			countStr = "0";
		}
		errCount = Integer.valueOf(countStr);
		//验证码是否为空
		if (ObjectUtil.isBlank(passportVO.getCode())) {
			return ResultBO.err(MessageCodeConstants.VERIFYCODE_IS_NULL_FIELD);
		}
		String redisCode = redisUtil.getString(email+passportVO.getSendType());
		//验证验证码是否正确
		if (!passportVO.getCode().equals(redisCode)) {
			errCount++;
			if (!passportVO.getSendType().equals((short) 2)) {
				UserInfoPO infoPO = new UserInfoPO();
				infoPO.setId(userId);
				if (errCount == 3) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(DateUtil.addHour(new Date(),1));
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+email, errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(tokenInfo)) {
						tokenInfo.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(passportVO.getIp(),userId);
					userUtil.clearUserToken(passportVO.getToken());
					userUtil.clearUserById(userId);
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_THREE);
				} else if (errCount == 8) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(DateUtil.addHour(new Date(),3));
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+email, errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(tokenInfo)) {
						tokenInfo.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(passportVO.getIp(),userId);
					userUtil.clearUserToken(passportVO.getToken());
					userUtil.clearUserById(userId);
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_EIGHT);
				} else if (errCount > 10) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(getTime());
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+email, errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(tokenInfo)) {
						tokenInfo.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(passportVO.getIp(),userId);
					userUtil.clearUserToken(passportVO.getToken());
					userUtil.clearUserById(userId);
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_TEN);
				} else {
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userId+email, errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
				}
			}
			return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
		}
		UserMessagePO userMessagePO = new UserMessagePO(email, passportVO.getCode(), UserConstants.IS_TRUE);
		verifyCodeDaoMapper.updateVerifyCodeStatus(userMessagePO);
		//验证完成，清除缓存
		redisUtil.delString(email+passportVO.getSendType());
		return ResultBO.ok();
	}

	private static Date getTime(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 24);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}


}
