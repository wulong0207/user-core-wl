package com.hhly.usercore.remote.passport.service.impl;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.constants.UserConstants.UserOperationEnum;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.RegularValidateUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.user.bo.BankCardDetailBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserResultBO;
import com.hhly.skeleton.user.bo.UserValidateBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.skeleton.user.vo.UserModifyLogVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.member.dao.BankcardMapper;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.security.dao.UserSecurityDaoMapper;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;
import com.hhly.usercore.remote.passport.service.IMemberRetrieveService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;


/**
 * 
 * @desc 找回密码实现类
 * @author zhouyang
 * @date 2017年2月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("iMemberRetrieveService")
public class MemberRetrieveServiceImpl implements IMemberRetrieveService {
	
	public static final Logger logger = Logger.getLogger(MemberRetrieveServiceImpl.class);
	
	@Autowired
	private IVerifyCodeService verifyCodeService;
	
	@Autowired
	private UserSecurityDaoMapper userSecurityDaoMapper;
	
	@Autowired
	private UserInfoDaoMapper userInfoDaoMapper;

	@Autowired
	private BankcardMapper bankcardMapper;
	
	@Resource
	private RedisUtil redisUtil;

	@Resource
	PublicMethod publicMethod;
	
	@Override
	public ResultBO<?> checkUserName(PassportVO passportVO) {
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
		/*UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserIp(ip);
		userModifyLogVO.setOperationStatus(UserConstants.IS_FALSE);
		userModifyLogVO.setUserAction(UserOperationEnum.USERNAME_ERROR_FOUND_PASS.getKey());
		int count = userModifyLogMapper.findUserOprateCount(userModifyLogVO);
		if (count > UserConstants.SEND_MAX) {
			return ResultBO.err(MessageCodeConstants.USERNAME_ERROR_OUT_OF_TEN_TIMES);
		}*/
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
		if (ObjectUtil.isBlank(userInfoBO)) {
			/*UserModifyLogPO userModifyLogPO = new UserModifyLogPO(null, UserOperationEnum.USERNAME_ERROR_FOUND_PASS.getKey(), UserConstants.IS_FALSE, ip, null, null, UserOperationEnum.USERNAME_ERROR_FOUND_PASS.getValue());
			userModifyLogMapper.addModifyLog(userModifyLogPO);*/
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		UserResultBO userResultBO = new UserResultBO();
		userResultBO.setAccount(userInfoBO.getAccount());
		userResultBO.setMobile(userInfoBO.getMobile());
		userResultBO.setMobileStatus(userInfoBO.getMobileStatus());
		userResultBO.setIsMobileLogin(userInfoBO.getIsMobileLogin());
		userResultBO.setEmail(userInfoBO.getEmail());
		userResultBO.setEmailStatus(userInfoBO.getEmailStatus());
		userResultBO.setIsEmailLogin(userInfoBO.getIsEmailLogin());
		//按创建时间显示最近添加的那张银行卡
		List<BankCardDetailBO> bankCardList = bankcardMapper.findBankList(userInfoBO.getId());
		if (!ObjectUtil.isBlank(bankCardList)) {
			userResultBO.setDefualtCard(StringUtil.hideString(bankCardList.get(0).getCardcode() , (short)2));
			userResultBO.setBankName(bankCardList.get(0).getBankname());
		}
		return ResultBO.ok(userResultBO);
	}

	@Override
	public ResultBO<?> checkFdMobileCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> resultBO = verifyCodeService.checkFdMobileVerifyCode(passportVO);
		if (resultBO.isError()) {
			return resultBO;
		}
		UserValidateBO userValidateBO = new UserValidateBO();
		userValidateBO.setValidatePass(UserConstants.IS_TRUE);
		userValidateBO.setMobile(passportVO.getMobile());
		redisUtil.addObj(CacheConstants.getMemberRetrieveKey(passportVO.getUserName(), UserConstants.IS_TRUE), userValidateBO, CacheConstants.FIFTEEN_MINUTES);
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> checkFdEmailCode(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> resultBO = verifyCodeService.checkFdEmailVerifyCode(passportVO);
		if (resultBO.isError()) {
			return resultBO;
		}
		UserValidateBO userValidateBO = new UserValidateBO();
		userValidateBO.setValidatePass(UserConstants.IS_TRUE);
		userValidateBO.setEmail(passportVO.getEmail());
		redisUtil.addObj(CacheConstants.getMemberRetrieveKey(passportVO.getUserName(), UserConstants.IS_TRUE), userValidateBO, CacheConstants.FIFTEEN_MINUTES);
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> checkBankCard(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> validateCardNum = ValidateUtil.validateCardNum(passportVO.getCardNum());
		if (validateCardNum.isError()) {
			return validateCardNum;
		}
		UserInfoVO userInfoVO = new UserInfoVO();
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
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
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserId(userInfoBO.getId());
		userModifyLogVO.setOperationStatus(UserConstants.IS_FALSE);
		userModifyLogVO.setUserAction(UserOperationEnum.BANKCARD_VALIDATE_FAIL.getKey());
		int logCount = userSecurityDaoMapper.findUserOprateCount(userModifyLogVO);
		if (logCount > UserConstants.SEND_MAX) {
			return ResultBO.err(MessageCodeConstants.BANKCARD_VALIDATE_FAIL_TIME_OUT_OF_TEN);
		}
		List<BankCardDetailBO> bankCardList = bankcardMapper.findBankList(userInfoBO.getId());
		if (ObjectUtil.isBlank(bankCardList)) {
			return ResultBO.err(MessageCodeConstants.NOT_BIND_BANK_CARD);
		}
		int errCount = 0;
		//验证银行卡号
		for (BankCardDetailBO bankCardDetailBO : bankCardList) {
				if (passportVO.getCardNum().equals(bankCardDetailBO.getCardcode().substring(bankCardDetailBO.getCardcode().length() - UserConstants.BANKCARD_AFTER_EIGHT, bankCardDetailBO.getCardcode().length()))) {
					errCount++;
				}
		}
		if (errCount <= 0) {
			//添加日志
			publicMethod.insertOperateLog(userInfoBO.getId(), UserOperationEnum.BANKCARD_VALIDATE_FAIL.getKey(), UserConstants.IS_FALSE , passportVO.getIp(), null, null, UserOperationEnum.BANKCARD_VALIDATE_FAIL.getValue());
			return ResultBO.err(MessageCodeConstants.BANKCARD_IS_ERROR);
		}
		UserValidateBO userValidateBO = new UserValidateBO();
		userValidateBO.setValidatePass(UserConstants.IS_TRUE);
		redisUtil.addObj(CacheConstants.getMemberRetrieveKey(passportVO.getUserName(), UserConstants.IS_TRUE), userValidateBO, CacheConstants.FIFTEEN_MINUTES);
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> updatePassword(PassportVO passportVO) {
		ResultBO<?> validateUserName = ValidateUtil.validateUserName(passportVO.getUserName());
		if (validateUserName.isError()) {
			return validateUserName;
		}
		ResultBO<?> validatePassword = ValidateUtil.validatePassword(passportVO.getPassword());
		if (validatePassword.isError()) {
			return validatePassword;
		}
		//验证两次密码输入是否一致
		if (!passportVO.getPassword().equals(passportVO.getPassword1())) {
			return ResultBO.err(MessageCodeConstants.ENTERED_PASSWORDS_DIFFER_FIELD);
		}
		UserInfoVO userInfoVO = new UserInfoVO();
		if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
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
		String key = CacheConstants.getMemberRetrieveKey(passportVO.getUserName(), UserConstants.IS_TRUE);
		UserValidateBO userValidateBO = redisUtil.getObj(key, new UserValidateBO());
		if (ObjectUtil.isBlank(userValidateBO) || ObjectUtil.isBlank(userValidateBO.getValidatePass()) || !userValidateBO.getValidatePass().equals(UserConstants.IS_TRUE)) {
			return ResultBO.err(MessageCodeConstants.VALIDATE_FAILED_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		String rCode = null;
		try {
			rCode = EncryptUtil.getSalt();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		userInfoPO.setId(userInfoBO.getId());
		userInfoPO.setrCode(rCode);
		try {
			userInfoPO.setPassword(EncryptUtil.encrypt(passportVO.getPassword(), rCode));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserId(userInfoBO.getId());
		userModifyLogVO.setUserAction(UserOperationEnum.LOGIN_FAIL_PASSWORD.getKey());
		//重置密码成功，删除登录失败记录
		userSecurityDaoMapper.deleteUserLoginRecord(userModifyLogVO);
		//添加日志
		publicMethod.insertOperateLog(userInfoBO.getId(), UserOperationEnum.SET_PASSWORD_SUCCESS.getKey(), UserConstants.IS_TRUE , passportVO.getIp(), null, passportVO.getPassword(), UserOperationEnum.SET_PASSWORD_SUCCESS.getValue());
		//验证后,清除验证状态
		//redisUtil.delObj(key);
		return ResultBO.ok();
	}

}
