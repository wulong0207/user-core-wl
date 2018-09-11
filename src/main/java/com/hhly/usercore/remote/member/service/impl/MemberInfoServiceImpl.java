package com.hhly.usercore.remote.member.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.Constants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.constants.UserConstants.UserOperationEnum;
import com.hhly.skeleton.base.mq.msg.MessageModel;
import com.hhly.skeleton.base.mq.msg.OperateNodeMsg;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.MathUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.RegularValidateUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.user.bo.BankCardDetailBO;
import com.hhly.skeleton.user.bo.SendSmsCountBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserModifyLogBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.skeleton.user.vo.UserModifyLogVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.rabbitmq.provider.MessageProvider;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.member.dao.BankcardMapper;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.pay.dao.UserWalletMapper;
import com.hhly.usercore.persistence.security.dao.UserSecurityDaoMapper;
import com.hhly.usercore.remote.member.service.IMemberInfoService;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;

/**
 * 用户个人信息接口实现类
 * @desc
 * @author zhouyang
 * @date 2017年3月4日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("iMemberInfoService")
public class MemberInfoServiceImpl implements IMemberInfoService {
	
	private static final Logger logger = Logger.getLogger(MemberInfoServiceImpl.class);
	
	@Autowired
	private UserInfoDaoMapper userInfoDaoMapper;
	
	@Autowired
	private IVerifyCodeService verifyCodeService;
	
	@Autowired
	private UserSecurityDaoMapper userSecurityDaoMapper;
	
	@Autowired
	private MessageProvider messageProvider;

	@Autowired
	private UserWalletMapper userWalletMapper;

	@Autowired
	private BankcardMapper bankcardMapper;
	
	@Resource
	RedisUtil redisUtil;
	
	@Autowired
	private UserUtil userUtil;

	@Resource
	PublicMethod publicMethod;

	@Value("${before_file_url}")
	private String before_file_url;

    /**
     * 根据用户ID获取用户设置的勿扰模式
     *
     * @param userInfoVO
     * @return
     * @throws Exception
     */
    @Override
    public ResultBO<?> getDisturb (UserInfoVO userInfoVO) {
        UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
        if(ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
        if(!StringUtil.isBlank(userInfoBO.getAppNotDisturb())) {
            String appNoteDisturb = userInfoBO.getAppNotDisturb();
            userInfoBO.setAppNotDisturbStart(appNoteDisturb.substring(0, appNoteDisturb.indexOf("-")));
            userInfoBO.setAppNotDisturbEnd(appNoteDisturb.substring(appNoteDisturb.length() - 5));
        }
        //0关1开
        userInfoBO.setSwitchStatus(userInfoBO.getMsgApp() == null ? UserConstants.IS_TRUE : userInfoBO.getMsgApp().intValue());
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("ss", userInfoBO.getSwitchStatus());
        jsonMap.put("as", userInfoBO.getAppNotDisturbStart());
        jsonMap.put("ae", userInfoBO.getAppNotDisturbEnd());

        return ResultBO.ok(jsonMap);
    }

    /**
     * 消息设置-勿扰模式，设置勿扰时间段
     *
     * @param passportVO 参数
     * @return object
     * @throws Exception 异常
     */
    @Override
    public ResultBO<?> doNotDisturb (PassportVO passportVO) {
        UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
        if(ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setId(tokenInfo.getId());
        if(passportVO.getSwitchStatus() == UserConstants.IS_TRUE.intValue()) {
            userInfoPO.setAppNotDisturb(passportVO.getTimeStr());
        }
        userInfoPO.setMsgApp(passportVO.getSwitchStatus());
        int row = userInfoDaoMapper.updateUserInfo(userInfoPO);
        if(row > 0) {
            return ResultBO.ok();
        } else {
            return ResultBO.err();
        }
    }

	@Override
	public ResultBO<?> validateNickname(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
		}
		ResultBO<?> validateNickname = ValidateUtil.validateNickname(passportVO.getNickname());
		if (validateNickname.isError()) {
			return validateNickname;
		}
		ResultBO<?> checkKeyword = publicMethod.checkKeyword(passportVO.getNickname());
		if (checkKeyword.isError()){
			return checkKeyword;
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
		int userCount = userInfoDaoMapper.findUserInfoByAccount(publicMethod.account(tokenInfo.getId(), passportVO.getNickname(), passportVO.getNickname()));
		if (userCount > 0) {
			return ResultBO.err(MessageCodeConstants.NICKNAME_IS_REGISTER);
		}
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> showUserIndex(UserInfoVO userInfoVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(userInfoVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		userInfoVO.setId(tokenInfo.getId());
		UserInfoBO resultInfoBO = userInfoDaoMapper.findUserIndexByUserId(userInfoVO);
		if (ObjectUtil.isBlank(resultInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		//按创建时间显示最近添加的那张银行卡
		List<BankCardDetailBO> bankCardList = bankcardMapper.findBankList(tokenInfo.getId());
		if (!ObjectUtil.isBlank(bankCardList)) {
			resultInfoBO.setBankCard(StringUtil.hideString(bankCardList.get(0).getCardcode(), (short)2));
		}
		//是否设置密码
		publicMethod.pwdStatus(userInfoBO, resultInfoBO);
		//是否实名认证
		publicMethod.attestationStatus(userInfoBO, resultInfoBO);
		//上次登录时间
		resultInfoBO.setLastLoginTime(tokenInfo.getLastLoginTime());
		//上次登录的ip地址
		resultInfoBO.setIp(tokenInfo.getIp());
		//设置积分
		resultInfoBO.setSafeIntegral(publicMethod.getSafeIntegration(userInfoBO));
		//计算总金额
		resultInfoBO.setUsableBalance(MathUtil.add(resultInfoBO.getRedPackBalance(),resultInfoBO.getUserWalletBalance()));
		publicMethod.setHeadUrl(userInfoBO, resultInfoBO);
		//处理代理信息
		publicMethod.setAgentInfo(resultInfoBO);
		//更新缓存
		publicMethod.modifyCache(userInfoVO.getToken(), tokenInfo, userInfoVO.getPlatform());
		//返回信息处理
		publicMethod.encryption(userInfoBO, resultInfoBO);
		return ResultBO.ok(resultInfoBO);
	}

	@Override
	public ResultBO<?> showUserPersonalData(UserInfoVO userInfoVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(userInfoVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoBO resultInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(resultInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		//
		List<BankCardDetailBO> bankCardList = bankcardMapper.findBankList(tokenInfo.getId());
		if (!ObjectUtil.isBlank(bankCardList)) {
			for (BankCardDetailBO bankCardDetailBO : bankCardList) {
				bankCardDetailBO.setBlogo(before_file_url + bankCardDetailBO.getBlogo());
				bankCardDetailBO.setSlogo(before_file_url + bankCardDetailBO.getSlogo());
				if (!ObjectUtil.isBlank(bankCardDetailBO.getCardcode())) {
					bankCardDetailBO.setCardcode(StringUtil.hideString(bankCardDetailBO.getCardcode(), (short)2));
				}
				if (!ObjectUtil.isBlank(bankCardDetailBO.getRealname())) {
					bankCardDetailBO.setRealname(StringUtil.hideString(bankCardDetailBO.getRealname(), (short)5));
				}
                // 如果信用已经过期，设置为0，表示无效
                if (bankCardDetailBO.getBanktype().equals(UserConstants.BankCardType.CREDIT_CARD.getKey())) {
                    try {
                        if (!DateUtil.validateCredCardOver(bankCardDetailBO.getOverdue())) {
                            bankCardDetailBO.setOverdue(UserConstants.BankCardStatusEnum.DELETE.getKey().toString());
                        } else {
                            bankCardDetailBO.setOverdue(UserConstants.BankCardStatusEnum.EFFECTIVE.getKey().toString());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    bankCardDetailBO.setOverdue(UserConstants.BankCardStatusEnum.EFFECTIVE.getKey().toString());
                }
            }
			resultInfoBO.setBankCardDetailBOList(bankCardList);
		}
		//是否设置了密码
		publicMethod.pwdStatus(resultInfoBO, resultInfoBO);
		//是否实名认证
		publicMethod.attestationStatus(resultInfoBO, resultInfoBO);
		//是否绑定第三方帐号
		publicMethod.tpBindStatus(tokenInfo, resultInfoBO);
		//上一次登录时间
		resultInfoBO.setLastLoginTime(tokenInfo.getLastLoginTime());
		//上一次登录的ip地址
		resultInfoBO.setIp(tokenInfo.getIp());
		//获取头像信息
		publicMethod.setHeadUrl(resultInfoBO, resultInfoBO);
		//更新缓存时间
		publicMethod.modifyCache(userInfoVO.getToken(), tokenInfo, userInfoVO.getPlatform());
		//返回信息加密处理
		publicMethod.encryption(resultInfoBO, resultInfoBO);
		resultInfoBO.setPassword(null);
		resultInfoBO.setrCode(null);
		return ResultBO.ok(resultInfoBO);
	}

	@Override
	public ResultBO<?> showUserLoginData(UserInfoVO userInfoVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(userInfoVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserId(tokenInfo.getId());
		userModifyLogVO.setUserAction(UserOperationEnum.LOGIN_SUCCESS.getKey());
		List<UserModifyLogBO> userModifyLogBO = userSecurityDaoMapper.findModifyLog(userModifyLogVO);
		publicMethod.modifyCache(userInfoVO.getToken(), tokenInfo, userInfoVO.getPlatform());
		return ResultBO.ok(userModifyLogBO);
	}

	@Override
	public ResultBO<?> checkMobile(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		ResultBO<?> resultBO = verifyCodeService.checkMobileVerifyCode(passportVO);
		if (resultBO.isError()) {
			return resultBO;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setMobileStatus(UserConstants.IS_TRUE);
		userInfoPO.setIsMobileLogin(UserConstants.IS_TRUE);

		tokenInfo.setMobileStatus(UserConstants.IS_TRUE);
		tokenInfo.setIsMobileLogin(UserConstants.IS_TRUE);
		userInfoPO.setId(tokenInfo.getId());
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		tokenInfo.setValidatePass(UserConstants.IS_TRUE);
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> checkEmail(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		ResultBO<?> resultBO = verifyCodeService.checkEmailVerifyCode(passportVO);
		if (resultBO.isError()) {
			return resultBO;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		tokenInfo.setValidatePass(UserConstants.IS_TRUE);
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setEmailStatus(UserConstants.IS_TRUE);
		userInfoPO.setIsEmailLogin(UserConstants.IS_TRUE);
		tokenInfo.setEmailStatus(UserConstants.IS_TRUE);
		tokenInfo.setIsEmailLogin(UserConstants.IS_TRUE);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok();
	}
	
	@Override
	public ResultBO<?> checkCreditCardNum(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> validateCardNum = ValidateUtil.validateCardNum(passportVO.getCardNum());
		if (validateCardNum.isError()) {
			return validateCardNum;
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
		List<BankCardDetailBO> bankCardList = bankcardMapper.findBankList(tokenInfo.getId());
		if (ObjectUtil.isBlank(bankCardList)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserId(tokenInfo.getId());
		userModifyLogVO.setUserAction(UserOperationEnum.CHECK_BANKCARD_FAIL.getKey());
		int count = userSecurityDaoMapper.findUserOprateCount(userModifyLogVO);
		if (count > UserConstants.CHECK_MAX ) {
			return ResultBO.err(MessageCodeConstants.BANKCARD_ERROR_OUT_OF_EIGHT);
		}
		int errCount = 0;
		//验证银行卡号
		for (BankCardDetailBO bankCardDetailBO : bankCardList) {
			if (passportVO.getCardNum().equals(bankCardDetailBO.getCardcode().substring(bankCardDetailBO.getCardcode().length() - UserConstants.BANKCARD_AFTER_EIGHT, bankCardDetailBO.getCardcode().length()))) {
				errCount++;
			}
		}
		if (errCount <= 0) {
			SendSmsCountBO countBO = new SendSmsCountBO(count+1);
			publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.CHECK_BANKCARD_FAIL.getKey(), UserConstants.IS_FALSE, passportVO.getIp(), null, null, UserOperationEnum.CHECK_BANKCARD_FAIL.getValue() );
			return ResultBO.err(MessageCodeConstants.BANKCARD_IS_ERROR, countBO,null);
		}
		tokenInfo.setValidatePass(UserConstants.IS_TRUE);
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> checkIDCard(PassportVO passportVO) {
		if (ObjectUtil.isBlank(passportVO)) {
			return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL_FIELD);
		}
		ResultBO<?> validateIDCard = ValidateUtil.validateIDCard(passportVO.getIdCard());
		if (validateIDCard.isError()) {
			return validateIDCard;
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
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserId(tokenInfo.getId());
		userModifyLogVO.setUserAction(UserOperationEnum.CHECK_IDCARD_FAIL.getKey());
		int count = userSecurityDaoMapper.findUserOprateCount(userModifyLogVO);
		if (count > UserConstants.CHECK_MAX ) {
			return ResultBO.err(MessageCodeConstants.IDCARD_ERROR_OUT_OF_EIGHT);
		}
		if (!passportVO.getIdCard().equals(tokenInfo.getIdCard().substring(tokenInfo.getIdCard().length() - UserConstants.BANKCARD_AFTER_EIGHT))) {
			publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.CHECK_IDCARD_FAIL.getKey(), UserConstants.IS_FALSE, passportVO.getIp(), null, null, UserOperationEnum.CHECK_IDCARD_FAIL.getValue() );
			SendSmsCountBO countBO = new SendSmsCountBO(count+1);
			return ResultBO.err(MessageCodeConstants.IDCARD_ERROR, countBO, null);
		}
		tokenInfo.setValidatePass(UserConstants.IS_TRUE);
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> checkPassword(PassportVO passportVO) {
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
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_SET_PASSWORD_SERVICE);
		}
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserId(tokenInfo.getId());
		userModifyLogVO.setUserAction(UserOperationEnum.CHECK_PASSWORD_FAIL.getKey());
		int count = userSecurityDaoMapper.findUserOprateCount(userModifyLogVO);
		if (count > UserConstants.CHECK_MAX ) {
			return ResultBO.err(MessageCodeConstants.PASSWORD_ERROR_OUT_OF_EIGHT);
		}
		try {
			if (!EncryptUtil.encrypt(passportVO.getPassword(), userInfoBO.getrCode()).equals(userInfoBO.getPassword())) {
                publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.CHECK_PASSWORD_FAIL.getKey(), UserConstants.IS_FALSE, passportVO.getIp(), null, null, UserOperationEnum.CHECK_PASSWORD_FAIL.getValue() );
                SendSmsCountBO countBO = new SendSmsCountBO(count+1);
                return ResultBO.err(MessageCodeConstants.PASSWORD_ERROR_SERVICE, countBO, null);
            }
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		tokenInfo.setValidatePass(UserConstants.IS_TRUE);
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> updateNickname(PassportVO passportVO) {
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
		if ((!ObjectUtil.isBlank(tokenInfo.getMobile()) && tokenInfo.getMobileStatus().equals(UserConstants.IS_TRUE))
				|| (ObjectUtil.isBlank(tokenInfo.getEmail()) && tokenInfo.getEmailStatus().equals(UserConstants.IS_TRUE))
				|| !ObjectUtil.isBlank(tokenInfo.getIdCard())) {
			//是否通过验证
			if (ObjectUtil.isBlank(tokenInfo.getValidatePass()) || !tokenInfo.getValidatePass().equals(UserConstants.IS_TRUE)) {
				return ResultBO.err(MessageCodeConstants.VALIDATE_FAILED_SERVICE);
			}
		}
		//验证昵称是否已经修改过
		if (tokenInfo.getAccountModify().equals(UserConstants.ModifyTypeEnum.MODIFY_ALL.getKey()) || tokenInfo.getAccountModify().equals(UserConstants.ModifyTypeEnum.MODIFY_NICKNAME.getKey())) {
            return ResultBO.err(MessageCodeConstants.NICKNAME_IS_MODIFIED);
        }
		//验证帐户名格式
		ResultBO<?> validateNickname = ValidateUtil.validateNickname(passportVO.getNickname());
		if (validateNickname.isError()) {
			return validateNickname;
		}
		//验证帐户名是否存在敏感字符
		ResultBO<?> checkKeyword = publicMethod.checkKeyword(passportVO.getNickname());
		if (checkKeyword.isError()) {
			return checkKeyword;
		}
		int userCount = userInfoDaoMapper.findUserInfoByAccount(publicMethod.account(tokenInfo.getId(), passportVO.getNickname(), passportVO.getNickname()));
		//验证账户名是否重复
		if (userCount > UserConstants.ZERO_INTEGER) {
			return ResultBO.err(MessageCodeConstants.NICKNAME_IS_REGISTER);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setNickName(passportVO.getNickname());
		if (tokenInfo.getAccountModify().equals(UserConstants.ModifyTypeEnum.NO_MODIFY.getKey())) {
			userInfoPO.setAccountModify(UserConstants.ModifyTypeEnum.MODIFY_NICKNAME.getKey());
		} else if (tokenInfo.getAccountModify().equals(UserConstants.ModifyTypeEnum.MODIFY_ACCOUNT.getKey())) {
			userInfoPO.setAccountModify(UserConstants.ModifyTypeEnum.MODIFY_ALL.getKey());
		}
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.NICKNAME_UPDATE_SUCCESS.getKey(), UserConstants.IS_TRUE , passportVO.getIp(), tokenInfo.getNickname(), passportVO.getNickname(), UserOperationEnum.NICKNAME_UPDATE_SUCCESS.getValue());
		tokenInfo.setNickname(passportVO.getNickname());
		tokenInfo.setAccountModify(userInfoPO.getAccountModify());
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		senNodeMsg(tokenInfo.getId());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> updateAccount(PassportVO passportVO) {
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
		if ((!ObjectUtil.isBlank(tokenInfo.getMobile()) && tokenInfo.getMobileStatus().equals(UserConstants.IS_TRUE))
				|| (ObjectUtil.isBlank(tokenInfo.getEmail()) && tokenInfo.getEmailStatus().equals(UserConstants.IS_TRUE))
				|| !ObjectUtil.isBlank(tokenInfo.getIdCard())) {
			//是否通过验证
			if (ObjectUtil.isBlank(tokenInfo.getValidatePass()) || !tokenInfo.getValidatePass().equals(UserConstants.IS_TRUE)) {
				return ResultBO.err(MessageCodeConstants.VALIDATE_FAILED_SERVICE);
			}
		}
		//验证昵称是否已经修改过
		if (tokenInfo.getAccountModify().equals(UserConstants.ModifyTypeEnum.MODIFY_ALL.getKey()) || tokenInfo.getAccountModify().equals(UserConstants.ModifyTypeEnum.MODIFY_ACCOUNT.getKey())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_MODIFIED_SERVICE);
		}
		//验证帐户名格式
		ResultBO<?> validateAccount = ValidateUtil.validateAccount(passportVO.getAccount());
		if (validateAccount.isError()) {
			return validateAccount;
		}
		//验证帐户名是否存在敏感字符
		ResultBO<?> checkKeyword = publicMethod.checkKeyword(passportVO.getAccount());
		if (checkKeyword.isError()) {
			return checkKeyword;
		}
		int userCount = userInfoDaoMapper.findUserInfoByAccount(publicMethod.account(tokenInfo.getId(), passportVO.getAccount(), passportVO.getAccount()));
		//验证账户名是否重复
		if (userCount > UserConstants.ZERO_INTEGER) {
			return ResultBO.err(MessageCodeConstants.USERNAME_IS_REGISTERED_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setAccount(passportVO.getAccount());
		if (tokenInfo.getAccountModify().equals(UserConstants.ModifyTypeEnum.NO_MODIFY.getKey())) {
			userInfoPO.setAccountModify(UserConstants.ModifyTypeEnum.MODIFY_ACCOUNT.getKey());
		} else if (tokenInfo.getAccountModify().equals(UserConstants.ModifyTypeEnum.MODIFY_NICKNAME.getKey())) {
			userInfoPO.setAccountModify(UserConstants.ModifyTypeEnum.MODIFY_ALL.getKey());
		}
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		/*if (tokenInfo.getLoginType().equals(LoginTypeEnum.ACCOUNT.getKey())) {
			redisUtil.delObj(CacheConstants.getMemberCacheKey(passportVO.getToken()));
		}*/
		
		//添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.ACCOUNT_UPDATE_SUCCESS.getKey(), UserConstants.IS_TRUE , passportVO.getIp(), tokenInfo.getAccount(), passportVO.getAccount(), UserOperationEnum.ACCOUNT_UPDATE_SUCCESS.getValue());
		tokenInfo.setAccount(passportVO.getAccount());
		tokenInfo.setAccountModify(userInfoPO.getAccountModify());
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		senNodeMsg(tokenInfo.getId());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> updatePassword(PassportVO passportVO) {
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
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_SET_PASSWORD_SERVICE);
		}
		if (!ObjectUtil.isBlank(passportVO.getPassword())) {
			ResultBO<?> validatePassword = ValidateUtil.validatePassword(passportVO.getPassword());
			if (validatePassword.isError()) {
				return validatePassword;
			}
			try {
				if (!EncryptUtil.encrypt(passportVO.getPassword(), userInfoBO.getrCode()).equals(userInfoBO.getPassword())) {
                    return ResultBO.err(MessageCodeConstants.PASSWORD_ERROR_SERVICE);
                }
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
		} else {
			if ((!ObjectUtil.isBlank(tokenInfo.getMobile()) && tokenInfo.getMobileStatus().equals(UserConstants.IS_TRUE))
					|| (!ObjectUtil.isBlank(tokenInfo.getEmail()) && tokenInfo.getEmailStatus().equals(UserConstants.IS_TRUE))
					|| !ObjectUtil.isBlank(tokenInfo.getIdCard())) {
				//是否通过验证
				if (ObjectUtil.isBlank(tokenInfo.getValidatePass()) || !tokenInfo.getValidatePass().equals(UserConstants.IS_TRUE)) {
					return ResultBO.err(MessageCodeConstants.VALIDATE_FAILED_SERVICE);
				}
			}
		}
		ResultBO<?> validatePassword1 = ValidateUtil.validatePassword(passportVO.getPassword1());
		if (validatePassword1.isError()) {
			return validatePassword1;
		}
		if (!passportVO.getPassword1().equals(passportVO.getPassword2())) {
			return ResultBO.err(MessageCodeConstants.ENTERED_PASSWORDS_DIFFER_FIELD);
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
			password = EncryptUtil.encrypt(passportVO.getPassword1(), rCode);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setrCode(rCode);
		userInfoPO.setPassword(password);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertUpadatePwdLog(passportVO.getIp(), userInfoBO);
		tokenInfo.setPassword(password);
		tokenInfo.setrCode(rCode);
		//更新缓存
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		senNodeMsg(tokenInfo.getId());
		return ResultBO.ok();
	}
	
	@Override
	public ResultBO<?> updateMobile(PassportVO passportVO) {
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
		boolean sendFlag = false;
		if (!ObjectUtil.isBlank(tokenInfo.getMobile()) && tokenInfo.getMobileStatus().equals(UserConstants.IS_TRUE)) {
			//是否通过验证
			if (ObjectUtil.isBlank(tokenInfo.getValidatePass()) || !tokenInfo.getValidatePass().equals(UserConstants.IS_TRUE)) {
				return ResultBO.err(MessageCodeConstants.VALIDATE_FAILED_SERVICE);
			}
			sendFlag = true;
		}
		ResultBO<?> resultCheck = verifyCodeService.checkNewMobileVerifyCode(passportVO);
		if (resultCheck.isError()) {
			return resultCheck;
		}
		String mobile = "";
		if (tokenInfo.getMobileStatus().equals(UserConstants.IS_TRUE)) {
			mobile = passportVO.getMobile();
		} else {
			if (!ObjectUtil.isBlank(tokenInfo.getMobile())) {
				mobile = tokenInfo.getMobile();
			} else {
				mobile = passportVO.getMobile();
			}
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setMobile(mobile);
		userInfoPO.setMobileStatus(UserConstants.IS_TRUE);
		userInfoPO.setIsMobileLogin(UserConstants.IS_TRUE);

		tokenInfo.setMobile(mobile);
		tokenInfo.setMobileStatus(UserConstants.IS_TRUE);
		tokenInfo.setIsMobileLogin(UserConstants.IS_TRUE);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.MOBILE_UPDATE_SUCCESS.getKey(), UserConstants.IS_TRUE , passportVO.getIp(), tokenInfo.getMobile(), mobile, UserOperationEnum.MOBILE_UPDATE_SUCCESS.getValue());
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		if(sendFlag){
			//消息推送
			senNodeMsg(tokenInfo.getId());
		}
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> updateEmail(PassportVO passportVO) {
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
		boolean sendFlag = false;
		if (!ObjectUtil.isBlank(tokenInfo.getEmail()) && tokenInfo.getEmailStatus().equals(UserConstants.IS_TRUE)) {
			//是否通过验证
			if (ObjectUtil.isBlank(tokenInfo.getValidatePass()) || !tokenInfo.getValidatePass().equals(UserConstants.IS_TRUE)) {
				return ResultBO.err(MessageCodeConstants.VALIDATE_FAILED_SERVICE);
			}
			sendFlag = true;
		}
		ResultBO<?> resultCheck = verifyCodeService.checkNewEmailVerifyCode(passportVO);
		if (resultCheck.isError()) {
			return resultCheck;
		}
		String email = "";
		if (tokenInfo.getEmailStatus().equals(UserConstants.IS_TRUE)) {
			email = passportVO.getEmail();
		} else {
			if (!ObjectUtil.isBlank(tokenInfo.getEmail())) {
				email = tokenInfo.getEmail();
			} else {
				email = passportVO.getEmail();
			}
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setEmail(email);
		userInfoPO.setEmailStatus(UserConstants.IS_TRUE);
		userInfoPO.setIsEmailLogin(UserConstants.IS_TRUE);

		tokenInfo.setEmail(email);
		tokenInfo.setEmailStatus(UserConstants.IS_TRUE);
		tokenInfo.setIsEmailLogin(UserConstants.IS_TRUE);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.EMAIL_UPDATE_SUCCESS.getKey(), UserConstants.IS_TRUE , passportVO.getIp(), tokenInfo.getEmail(), email, UserOperationEnum.EMAIL_UPDATE_SUCCESS.getValue());
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		if(sendFlag){
			//消息推送
			senNodeMsg(tokenInfo.getId());
		}
		return ResultBO.ok();
	}
	
	@Override
	public ResultBO<?> openMobileLogin(UserInfoVO userInfoVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(userInfoVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}

		//手机号码是否验证
		if (tokenInfo.getMobileStatus().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_IS_NOT_VALIDATE_SERVICE);
		}
		//验证手机登录是否开启
		if (tokenInfo.getIsMobileLogin().equals(UserConstants.IS_TRUE)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_LOGIN_IS_OPENED_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setIsMobileLogin(UserConstants.IS_TRUE);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.MOBILE_LOGIN_OPEN_SUCCESS.getKey(), UserConstants.IS_TRUE , userInfoVO.getIp(), null, null, UserOperationEnum.MOBILE_LOGIN_OPEN_SUCCESS.getValue());
		tokenInfo.setIsMobileLogin(UserConstants.IS_TRUE);
		//更新缓存
		publicMethod.modifyCache(userInfoVO.getToken(), tokenInfo, userInfoVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> closeMobileLogin(UserInfoVO userInfoVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(userInfoVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserId(tokenInfo.getId());
		userModifyLogVO.setUserAction(UserOperationEnum.MOBILE_LOGIN_CLOSE_SUCCESS.getKey());
		int closeCount = userSecurityDaoMapper.findUserOprateCount(userModifyLogVO);
		if (closeCount > UserConstants.IS_TRUE) {
			return ResultBO.err(MessageCodeConstants.YOU_CAN_OPERATION_TWO_TIMES_EVERYDAY);
		}
		//手机号码是否验证
		if (tokenInfo.getMobileStatus().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_IS_NOT_VALIDATE_SERVICE);
		}
		if (tokenInfo.getIsMobileLogin().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_LOGIN_IS_CLOSED_SERVICE);
		}
		//验证帐号是否设置密码，如果没有，则引导用户设置密码
		if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
			return ResultBO.err(MessageCodeConstants.COULD_SET_PASSWORD_WHEN_CLOSE_THE_MOBILE_LOGIN);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setIsMobileLogin(UserConstants.IS_FALSE);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.MOBILE_LOGIN_CLOSE_SUCCESS.getKey(), UserConstants.IS_TRUE , userInfoVO.getIp(), null, null, UserOperationEnum.MOBILE_LOGIN_CLOSE_SUCCESS.getValue());
		tokenInfo.setIsMobileLogin(UserConstants.IS_FALSE);
		//更新缓存
		publicMethod.modifyCache(userInfoVO.getToken(), tokenInfo, userInfoVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> openEmailLogin(UserInfoVO userInfoVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(userInfoVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		//邮箱地址是否验证
		if (tokenInfo.getEmailStatus().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_IS_NOT_VALIDATE_SERVICE);
		}
		if (tokenInfo.getIsEmailLogin().equals(UserConstants.IS_TRUE)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_LOGIN_IS_OPENED_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setIsEmailLogin(UserConstants.IS_TRUE);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.EMAIL_LOGIN_OPEN_SUCCESS.getKey(), UserConstants.IS_TRUE , userInfoVO.getIp(), null, null, UserOperationEnum.EMAIL_LOGIN_OPEN_SUCCESS.getValue());
		tokenInfo.setIsEmailLogin(UserConstants.IS_TRUE);
		//更新缓存
		publicMethod.modifyCache(userInfoVO.getToken(), tokenInfo, userInfoVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public ResultBO<?> closeEmailLogin(UserInfoVO userInfoVO) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(userInfoVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
		userModifyLogVO.setUserId(tokenInfo.getId());
		userModifyLogVO.setUserAction(UserOperationEnum.EMAIL_LOGIN_CLOSE_SUCCESS.getKey());
		int closeCount = userSecurityDaoMapper.findUserOprateCount(userModifyLogVO);
		if (closeCount > UserConstants.IS_TRUE) {
			return ResultBO.err(MessageCodeConstants.YOU_CAN_OPERATION_TWO_TIMES_EVERYDAY);
		}
		//邮箱地址是否验证
		if (tokenInfo.getEmailStatus().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_IS_NOT_VALIDATE_SERVICE);
		}
		if (tokenInfo.getIsEmailLogin().equals(UserConstants.IS_FALSE)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_LOGIN_IS_CLOSED_SERVICE);
		}
		//验证帐号是否设置密码，如果没有，则引导用户设置密码
		if (ObjectUtil.isBlank(userInfoBO.getPassword())) {
			return ResultBO.err(MessageCodeConstants.COULD_SET_PASSWORD_WHEN_CLOSE_THE_EMAIL_LOGIN);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setIsEmailLogin(UserConstants.IS_FALSE);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertOperateLog(tokenInfo.getId(), UserOperationEnum.EMAIL_LOGIN_CLOSE_SUCCESS.getKey(), UserConstants.IS_TRUE , userInfoVO.getIp(), null, null, UserOperationEnum.EMAIL_LOGIN_CLOSE_SUCCESS.getValue());
		tokenInfo.setIsEmailLogin(UserConstants.IS_FALSE);
		//更新缓存
		publicMethod.modifyCache(userInfoVO.getToken(), tokenInfo, userInfoVO.getPlatform());
		return ResultBO.ok();
	}
	
	@Override
	public ResultBO<?> findUserInfoByToken(String token) {
		UserInfoBO tokenInfo = userUtil.getUserByToken(token);
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		return ResultBO.ok(tokenInfo);
	}

	@Override
	public ResultBO<?> setPassword(PassportVO passportVO) {
		ResultBO<?> validateToken = ValidateUtil.validateToken(passportVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		//帐号已设置密码
		if (!ObjectUtil.isBlank(userInfoBO.getPassword())) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_HAVE_SET_PASSWORD);
		}
		ResultBO<?> validatePassword = ValidateUtil.validatePassword(passportVO.getPassword1());
		if (validatePassword.isError()) {
			return validatePassword;
		}
		//验证两次密码输入是否一致
		if (!passportVO.getPassword1().equals(passportVO.getPassword2())) {
			return ResultBO.err(MessageCodeConstants.ENTERED_PASSWORDS_DIFFER_FIELD);
		}
		String rCode = null;
		try {
			rCode = EncryptUtil.getSalt();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String password = null;
		try {
			password = EncryptUtil.encrypt(passportVO.getPassword1(), rCode);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(userInfoBO.getId());
		userInfoPO.setrCode(rCode);
		userInfoPO.setPassword(password);
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		//添加日志
		publicMethod.insertUpadatePwdLog(passportVO.getIp(), userInfoBO);
		//更新缓存
		publicMethod.modifyCache(passportVO.getToken(), tokenInfo, passportVO.getPlatform());
		return ResultBO.ok();
	}

	@Override
	public void expireRedisKey(String token, long second) {
		userUtil.refreshExpire(token, second);
	}
	
	@Override
	public int updateLastUsePayId(UserInfoPO userInfoPO) {
		return userInfoDaoMapper.updateUserInfo(userInfoPO);
	}

	@Override
	public ResultBO<?> uploadHeadPortrait(UserInfoVO userInfoVO) {
		ResultBO<?> validateToken = ValidateUtil.validateToken(userInfoVO.getToken());
		if (validateToken.isError()) {
			return validateToken;
		}
		if (ObjectUtil.isBlank(userInfoVO.getHeadUrl())) {
			return ResultBO.err(MessageCodeConstants.HEAD_PORTRAIT_IS_NULL);
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(userInfoVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(tokenInfo.getId());
		userInfoPO.setHeadUrl(userInfoVO.getHeadUrl());
		userInfoDaoMapper.updateUserInfo(userInfoPO);
		String url = before_file_url + userInfoVO.getHeadUrl();
		tokenInfo.setHeadUrl(url);
		UserInfoBO userInfoBO = new UserInfoBO();
		userInfoBO.setHeadUrl(url);
		publicMethod.modifyCache(userInfoVO.getToken(), tokenInfo, userInfoVO.getPlatform());
		return ResultBO.ok(userInfoBO);
	}
	
	/**
	 * 会员资料变更发送消息通知用户
	 * @param UserId
	 */
	public void senNodeMsg(Integer UserId){
        MessageModel model = new  MessageModel();
        OperateNodeMsg bodyMsg = new OperateNodeMsg();
        model.setKey(Constants.MSG_NODE_RESEND);
        model.setMessageSource("user_core");
        bodyMsg.setNodeId(1);
        bodyMsg.setNodeData(""+UserId);
        model.setMessage(bodyMsg);
        messageProvider.sendMessage(Constants.QUEUE_NAME_MSG_QUEUE, model);
	}

	@Override
	public ResultBO<?> verifyMobile(String mobile) {
		boolean truth = false;
		//手机号非空验证
		if (ObjectUtil.isBlank(mobile)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_IS_NULL_FIELD);
		}
		//手机号格式验证
		if (!mobile.matches(RegularValidateUtil.REGULAR_MOBILE)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_FORMAT_ERROR_FIELD);
		}
		List<String> numList = redisUtil.getObj(CacheConstants.C_CORE_MEMBER_MOBILE_NUM_SEGMENT, new ArrayList<String>());
		for (String numStr : numList) {
			if (mobile.startsWith(numStr, UserConstants.ZERO_INTEGER)) {
				truth = true;
			}
		}
		if (!truth) {
			return ResultBO.err(MessageCodeConstants.MOBILE_FORMAT_ERROR_FIELD);
		} else {
			return ResultBO.ok();
		}
	}

	@Override
	public ResultBO<?> getTokenStatus(String token) {
		//验证token
		ResultBO<?> validateToken = ValidateUtil.validateToken(token);
		if (validateToken.isError()) {
			return validateToken;
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(token);
		if (ObjectUtil.isBlank(tokenInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		return ResultBO.ok();
	}
}
