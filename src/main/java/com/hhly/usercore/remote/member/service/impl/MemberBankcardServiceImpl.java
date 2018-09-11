package com.hhly.usercore.remote.member.service.impl;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.common.PayEnum;
import com.hhly.skeleton.base.common.PayEnum.EntranceEnum;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.Constants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.PayConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.pay.bo.AddBankCardFirsBO;
import com.hhly.skeleton.pay.bo.PayBankBO;
import com.hhly.skeleton.pay.bo.PayBankCardH5BO;
import com.hhly.skeleton.pay.bo.PayBankLimitBO;
import com.hhly.skeleton.pay.bo.PayBankSegmentBO;
import com.hhly.skeleton.pay.bo.PayBankcardBO;
import com.hhly.skeleton.pay.bo.UserPayTypeBO;
import com.hhly.skeleton.pay.channel.bo.PayChannelBO;
import com.hhly.skeleton.pay.channel.vo.PayChannelVO;
import com.hhly.skeleton.pay.vo.PayBankcardVO;
import com.hhly.skeleton.pay.vo.TakenReqParamVO;
import com.hhly.skeleton.user.bo.BankCardDetailBO;
import com.hhly.skeleton.user.bo.SendSmsCountBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserMessageBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserMessageVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.common.service.BankcardSegmentService;
import com.hhly.usercore.base.common.service.SmsService;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.SendCodeUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.local.bank.service.PayBankService;
import com.hhly.usercore.local.pay.service.PayChannelService;
import com.hhly.usercore.persistence.member.dao.BankcardMapper;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.dao.VerifyCodeDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.message.po.UserMessagePO;
import com.hhly.usercore.persistence.pay.dao.PayBankLimitDaoMapper;
import com.hhly.usercore.persistence.pay.po.PayBankcardPO;
import com.hhly.usercore.persistence.security.po.UserModifyLogPO;
import com.hhly.usercore.remote.member.service.IMemberBankcardService;
import com.hhly.usercore.remote.member.service.IMemberSecurityService;
import com.hhly.usercore.remote.passport.service.IMemberRegisterService;

/**
 * @version 1.0
 * @auth chenkangning
 * @date 2017/3/2.
 * @desc 用户银行卡管理接口实现类
 * @compay 益彩网络科技有限公司
 */
@Service("iMemberBankcardService")
public class MemberBankcardServiceImpl implements IMemberBankcardService {
	private static final Logger logger = Logger.getLogger(MemberBankcardServiceImpl.class);

	@Autowired
	private BankcardMapper bankcardMapper;

	@Autowired
	private IMemberSecurityService memberSecurityService;
	@Autowired
	private PayChannelService payChannelService;
	@Resource
	private RedisUtil redisUtil;
	@Resource
	private PayBankService payBankService;
	@Resource
	private BankcardSegmentService bankcardSegmentService;
	@Resource
	private SmsService smsService;
	@Autowired
	private VerifyCodeDaoMapper verifyCodeDaoMapper;
	@Resource
	private PayBankLimitDaoMapper bankLimitDaoMapper;

	@Autowired
	private IMemberRegisterService memberRegisterService;
	@Resource
	SendCodeUtil sendCodeUtil;

	@Resource
	UserInfoDaoMapper userInfoDaoMapper;

	@Resource
	private PublicMethod publicMethod;

	@Autowired
	private UserUtil userUtil;


    @Value("${before_file_url}")
    private String before_file_url;

    /**
     * 打开或关闭快捷支付-PC
     *
     * @param token  用户唯一标识
     * @param cardId 银行卡id
     * @return ResultBO
     * @throws Exception 异常
     */
    @Override
    public ResultBO<?> openOrCloseQuickPayment (String token, Integer cardId, Integer status) throws Exception {
        logger.debug("BankcardServiceImpl.openOrCloseQuickPayment input params >>> " + token + ">>>" + cardId + ">>>" + status);
        UserInfoBO userInfoBO = userUtil.getUserByToken(token);
        //是否通过验证
        if(ObjectUtil.isBlank(userInfoBO.getValidatePass()) || !userInfoBO.getValidatePass().equals(UserConstants.IS_TRUE)) {
            return ResultBO.err(MessageCodeConstants.VALIDATE_FAILED_SERVICE);
        }
        return closenQuickPayment(token, cardId, status);
    }


    /**
     * 关闭快捷支付
	 *
	 * @param token  用户唯一标识
	 * @param cardId 银行卡id
	 * @return
	 * @throws Exception
	 */
	@Override
	public ResultBO<?> closenQuickPayment(String token, Integer cardId, Integer status) throws Exception {
		logger.debug("BankcardServiceImpl.closenQuickPayment input params >>> " + token + ">>>" + cardId);
		UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (status.shortValue() != UserConstants.IS_TRUE && status.shortValue() != UserConstants.IS_FALSE) {
			return ResultBO.err(MessageCodeConstants.OPEN_BANK_PARAM_ERROR);
		}
        int row = openOrCloseQuickPayment(cardId, status, userInfoBO);
        clearUserBankCache(userInfoBO);// 清除用户的支付方式缓存
        if (row > 0) {
			return ResultBO.ok(status);
		} else {
			return ResultBO.err(MessageCodeConstants.NOT_FIND_USER_CARD_SERVICE);
		}
	}

    private int openOrCloseQuickPayment (Integer cardId, Integer status, UserInfoBO userInfoBO) {
        PayBankcardPO bankcardPO = new PayBankcardPO();
        bankcardPO.setId(cardId);
        bankcardPO.setUserId(userInfoBO.getId());
        bankcardPO.setOpenbank(status.shortValue());
        clearUserBankCache(userInfoBO);// 清除用户的支付方式缓存
        return bankcardMapper.closeOrOpenQuickPayment(bankcardPO);
    }

    /**
     * 添加OR 修改 银行卡
	 *
	 * @param token         用户唯一标识
	 * @param payBankCardVO PayBankcardPO对象
	 * @return ResultBO
	 * @throws Exception 异常
	 */
	@Override
	public ResultBO<?> addBankCard(String token, PayBankcardVO payBankCardVO) throws Exception {
		logger.debug("BankcardServiceImpl.addBankCard input params >>> " + payBankCardVO.toString());

		PayBankcardPO bankcardPO = new PayBankcardPO();
		ResultBO<?> resultBO = validate(token, payBankCardVO);
		if (resultBO != null) {
			return resultBO;
		}
		int row;


		UserInfoBO userInfoBO = userUtil.getUserByToken(token);

		ResultBO<?> resultBO1 = realNameAuth(token, payBankCardVO, userInfoBO);
		if (resultBO1 != null) {
			return resultBO1;
		}
        //把其它银行卡设置为非默认
        updateDisableDefault(userInfoBO);
        // 保存银行卡信息
        //UserInfoBO dataUserInfo = userInfoDaoMapper.findUserIndexByUserId(userInfoBO.getId());
        BeanUtils.copyProperties(payBankCardVO, bankcardPO);
        bankcardPO.setRealname("此字段暂无用");
        bankcardPO.setUserId(userInfoBO.getId());
		bankcardPO.setStatus(UserConstants.IS_TRUE.intValue());
		bankcardPO.setOpenbank(!Objects.equals(payBankCardVO.getOpenbank(), UserConstants.IS_FALSE) ? UserConstants.IS_TRUE : payBankCardVO.getOpenbank());
        bankcardPO.setIsdefault(UserConstants.IS_TRUE);
        row = bankcardMapper.addBankCard(bankcardPO);

		// 添加操作日志
		UserModifyLogPO userModifyLogPO = new UserModifyLogPO(bankcardPO.getUserId(), UserConstants.UserOperationEnum.ADD_BANKCARD.getKey(), UserConstants.IS_TRUE, bankcardPO.getIp(), null,
				bankcardPO.getBankname() + "-" + bankcardPO.getCardcode(), "移动端添加银行卡");
		memberSecurityService.addModifyLog(userModifyLogPO);
		if (row > 0) {
			updateUserBankInfo(bankcardPO);
            resetRedisUserInfo(payBankCardVO, userInfoBO);
            clearUserBankCache(userInfoBO);// 清除用户的支付方式缓存
			return ResultBO.ok();
		}

		return ResultBO.err(MessageCodeConstants.HESSIAN_ERROR_SYS);
	}

	private void updateUserBankInfo(PayBankcardPO bankcardPO){
		UserInfoPO po = new UserInfoPO();
		po.setId(bankcardPO.getUserId());
		po.setUserPayId(bankcardPO.getBankid());
		po.setUserPayCardcode(bankcardPO.getCardcode());
		int num = userInfoDaoMapper.updateUserInfo(po);
		logger.info("用户 : " + po.getId() + "执行添加银行卡 - 修改用户银行卡信息操作 : " + num);
	}

    private void updateDisableDefault (UserInfoBO userInfoBO) {
        //把其它银行卡设置为非默认
        PayBankcardPO payBankcardPO = new PayBankcardPO();
        payBankcardPO.setUserId(userInfoBO.getId());
        bankcardMapper.updateDisableDefault(payBankcardPO);
    }

	/**
	 * 实名认证
	 *
	 * @param token
	 * @param payBankCardVO
	 * @param userInfoBO
	 * @return
	 * @throws Exception
	 */
	private ResultBO<?> realNameAuth(String token, PayBankcardVO payBankCardVO, UserInfoBO userInfoBO) throws Exception {
		ResultBO<?> resultBO;
		if (StringUtil.isBlank(userInfoBO.getIdCard()) && !payBankCardVO.getIdCard().contains("*")) {
			resultBO = ValidateUtil.validateIdCard(payBankCardVO.getIdCard());
			if (resultBO.isError()) {
				return resultBO;
			}
		}
		// 判断身份证是不是包含*号，包含的话表示已经做过实名认证，此处不处理
		if (StringUtil.isBlank(userInfoBO.getIdCard()) && !payBankCardVO.getIdCard().contains("*")) {
			resultBO = realNameAuthentication(payBankCardVO, userInfoBO);
			userInfoBO.setIdCard(payBankCardVO.getIdCard());
			userInfoBO.setRealName(payBankCardVO.getRealname());
			if (resultBO.isError()) {
				return resultBO;
			}
			publicMethod.modifyCache(token, userInfoBO, userInfoBO.getLoginPlatform());
		}
		return null;
	}

	/**
	 * 保存实名认证信息
	 *
	 * @param payBankCardVO
	 * @param userInfoBO
	 * @return
	 * @throws Exception
	 */
	private ResultBO<?> realNameAuthentication(PayBankcardVO payBankCardVO, UserInfoBO userInfoBO) throws Exception {
		// 实名认证
		PassportVO passportVO = new PassportVO();
		passportVO.setIdCard(payBankCardVO.getIdCard());
        passportVO.setRealName(payBankCardVO.getRealname());
        passportVO.setUserId(userInfoBO.getId());
		passportVO.setToken(payBankCardVO.getToken());
		passportVO.setIp(payBankCardVO.getIp());
		return memberRegisterService.perfectRealName(passportVO);
	}

	/**
	 * PC添加银行卡
	 *
	 * @param payBankcardVO 数据对象
	 * @return
	 * @throws Exception
	 */
	@Override
	public ResultBO<?> addBandCardPc(PayBankcardVO payBankcardVO) throws Exception {

		logger.debug("BankcardServiceImpl.addBandCardPc input params >>> " + payBankcardVO.toString());

		UserInfoBO userInfoBO = userUtil.getUserByToken(payBankcardVO.getToken());
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
        if (ObjectUtil.isBlank(payBankcardVO.getValidateInfo())) {
            //兼容以前接口，判断此字段为空的时候需要验证身份
            if (!StringUtil.isBlank(userInfoBO.getMobile())
                    || !StringUtil.isBlank(userInfoBO.getEmail())
                    || !StringUtil.isBlank(userInfoBO.getIdCard())) {
                //如果手机号或邮箱或身份证某一项不为空时才校验身份，否则不需要校验用户身份
                if (ObjectUtil.isBlank(userInfoBO.getValidatePass()) || !userInfoBO.getValidatePass().equals(UserConstants.IS_TRUE)) {
                    //是否通过验证
                    return ResultBO.err(MessageCodeConstants.VALIDATE_FAILED_SERVICE);
                }
            }
        }


        PayBankcardPO bankcardPO = new PayBankcardPO();
		bankcardPO.setUserId(userInfoBO.getId());
        bankcardPO.setRealname(null);
        ResultBO<?> bo1 = validateForPcAdd(payBankcardVO, userInfoBO);
        if (bo1 != null) {
			return bo1;
		}
		ResultBO<?> resultBO1 = realNameAuth(payBankcardVO.getToken(), payBankcardVO, userInfoBO);
		if (resultBO1 != null) {
			return resultBO1;
		}
        updateDisableDefault(userInfoBO);
        BeanUtils.copyProperties(payBankcardVO, bankcardPO);
        bankcardPO.setIsdefault(UserConstants.IS_TRUE);
        int row = bankcardMapper.addBankCard(bankcardPO);
        // 添加操作日志
		UserModifyLogPO userModifyLogPO = new UserModifyLogPO(bankcardPO.getUserId(), UserConstants.UserOperationEnum.ADD_BANKCARD.getKey(), UserConstants.IS_TRUE, bankcardPO.getIp(), null,
				bankcardPO.getBankname() + "-" + bankcardPO.getCardcode(), "添加银行卡PC");
		memberSecurityService.addModifyLog(userModifyLogPO);
		if (row > 0) {
			updateUserBankInfo(bankcardPO);
            resetRedisUserInfo(payBankcardVO, userInfoBO);
            clearUserBankCache(userInfoBO);// 清除用户的支付方式缓存
			PayBankcardBO bo = new PayBankcardBO();
			bo.setId(bankcardPO.getId());
			return ResultBO.ok(bo);
		}
		return ResultBO.err(MessageCodeConstants.HESSIAN_ERROR_SYS);
	}

    /**
     * 更新redis信息
     */
    private void resetRedisUserInfo (PayBankcardVO payBankcardVO, UserInfoBO userInfoBO) {
        userInfoBO.setValidatePass(UserConstants.IS_TRUE);
        publicMethod.modifyCache(payBankcardVO.getToken(), userInfoBO, userInfoBO.getLoginPlatform());
    }


    /**
     * PC添加银行卡校验数据
	 *
	 * @param payBankcardVO
	 * @param userInfoBO
	 * @return
	 */
    private ResultBO<?> validateForPcAdd(PayBankcardVO payBankcardVO, UserInfoBO userInfoBO) {

        // 领奖人银行卡号
        if (StringUtil.isBlank(payBankcardVO.getCardcode())) {
            return ResultBO.err(MessageCodeConstants.CARD_CODE_IS_NULL_FIELD);
        }
        // 获取具体的银行信息
        ResultBO<?> bo = findPayBankByCard(payBankcardVO.getCardcode());
        if (bo.isError()) {
            return bo;
        }
        // 判断此用户是否已经添加过此张银行卡
        List<?> list = bankcardMapper.selectByUserIdAndCardCodeIsExist(userInfoBO.getId().toString(), payBankcardVO.getCardcode());
        if (!ObjectUtil.isBlank(list)) {
            return ResultBO.err(MessageCodeConstants.IS_USER_BANKCARD_EXIST_SERVICE);
        }

        // 快捷支付
        if (!Objects.equals(payBankcardVO.getOpenbank(), UserConstants.IS_FALSE) && !Objects.equals(payBankcardVO.getOpenbank(), UserConstants.IS_TRUE)) {
            return ResultBO.err(MessageCodeConstants.OPEN_BANK_PARAM_ERROR);
        }

        // 隐藏参数：银行卡类型错误
        if (payBankcardVO.getBanktype() != UserConstants.BankCardType.DEPOSIT_CARD.getKey() && payBankcardVO.getBanktype() != UserConstants.BankCardType.CREDIT_CARD.getKey()) {
            return ResultBO.err(MessageCodeConstants.BANK_TYPE_IS_NULL_OR_ERROR);
        }
        ResultBO<?> resultBO = validateCreditCard(payBankcardVO);
        if (resultBO != null) {
			return resultBO;
		}


        // 设置默认卡片
        if (!Objects.equals(payBankcardVO.getIsdefault(), UserConstants.IS_FALSE) && !Objects.equals(payBankcardVO.getIsdefault(), UserConstants.IS_TRUE)) {
            payBankcardVO.setIsdefault(UserConstants.IS_FALSE);
        }
        return null;
    }

    /**
     * 信用卡校验
     *
     * @param payBankcardVO
     * @return
     */
    private ResultBO<?> validateCreditCard(PayBankcardVO payBankcardVO) {
        // 如果是信用卡的话，
        if (!ObjectUtil.isBlank(payBankcardVO.getBanktype()) && Objects.equals(payBankcardVO.getBanktype(), Short.parseShort(UserConstants.BankCardType.CREDIT_CARD.getKey() + ""))) {
            // 1.校验银行卡号是否符合算法
            if (!validateCreditCard(payBankcardVO.getCardcode())) {
                return ResultBO.err(MessageCodeConstants.INVALID_CREDIT_CARD_ERROR);
            }
            // 2.信用卡有效期
            if (StringUtil.isBlank(payBankcardVO.getOverdue())) {
                return ResultBO.err(MessageCodeConstants.BANK_CARD_OVERDUE_IS_NULL_FIELD);
            } else {
                try {
                    if (!DateUtil.validateCredCardOver(payBankcardVO.getOverdue())) {
                        return ResultBO.err(MessageCodeConstants.CREDIT_CARD_EXPIRED);
                    }
                } catch (ParseException e) {
                    return ResultBO.err(MessageCodeConstants.INVALID_CREDIT_CARD_FORMAT);
                }
            }
            payBankcardVO.setBanktype(UserConstants.BankCardType.CREDIT_CARD.getKey());
        } else {
            payBankcardVO.setBanktype(UserConstants.BankCardType.DEPOSIT_CARD.getKey());
        }
        return null;
    }

	/**
	 * 激活信用卡
	 *
	 * @param payBankCardVO
	 * @return
	 * @throws Exception
	 */
	@Override
	public ResultBO<?> activateCard(PayBankcardVO payBankCardVO) throws Exception {
		int row;
		UserInfoBO userInfoBO = userUtil.getUserByToken(payBankCardVO.getToken());
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (ObjectUtil.isBlank(payBankCardVO.getId())) {
			// 如果银行卡ID字段为空，直接返回
			return ResultBO.err(MessageCodeConstants.BANKCARD_IS_VALIDATION_SERVICE);
		}
		PayBankcardPO bankcardPO = new PayBankcardPO();
		bankcardPO.setId(payBankCardVO.getId());
		try {
			if (DateUtil.validateCredCardOver(payBankCardVO.getOverdue())) {
				bankcardPO.setOverdue(payBankCardVO.getOverdue());
			}
		} catch (ParseException e) {
			logger.error("BankcardServiceImpl addBankCard 信用卡有效期错误:" + e);
			return ResultBO.err(MessageCodeConstants.INVALID_CREDIT_CARD_FORMAT);
		}

		bankcardPO.setUserId(userInfoBO.getId());
		row = bankcardMapper.updateByBankCardId(bankcardPO);

		// 添加操作日志
		UserModifyLogPO userModifyLogPO = new UserModifyLogPO(payBankCardVO.getUserid(), UserConstants.UserOperationEnum.UPDATE_BANKCARD.getKey(), UserConstants.IS_TRUE, payBankCardVO.getIp(), null,
				payBankCardVO.getBankname() + "-" + payBankCardVO.getCardcode(), "激活信用卡");
		memberSecurityService.addModifyLog(userModifyLogPO);
		if (row > 0) {
			return ResultBO.ok();
		}
		return ResultBO.err(MessageCodeConstants.NOT_FIND_USER_CARD_SERVICE);
	}

	/**
	 * 验证信息
	 *
	 * @param token         用户唯一标识
	 * @param payBankCardVO 数据对象
	 * @return ResultBO or null
     */
    private ResultBO<?> validate(String token, PayBankcardVO payBankCardVO) throws Exception{
        UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (ObjectUtil.isBlank(userInfoBO)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
        UserInfoBO userInfoBO1 = userInfoDaoMapper.findUserInfo(publicMethod.id(userInfoBO.getId()));
		if (ObjectUtil.isBlank(userInfoBO1)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
		}
		payBankCardVO.setUserid(userInfoBO.getId());
		// 判断银行id是否符合要求
		if (payBankCardVO.getBankid() != UserConstants.IS_FALSE.intValue()) {
			PayBankBO payBankBO = payBankService.findBankById(payBankCardVO.getBankid());
			if (ObjectUtil.isBlank(payBankBO)) {
				return ResultBO.err(MessageCodeConstants.INVALID_BANK_ID_ERROR);
			}
		} else {
			return ResultBO.err(MessageCodeConstants.INVALID_BANK_ID_ERROR);
		}

		// 银行卡名称为空
		if (StringUtil.isBlank(payBankCardVO.getBankname())) {
			return ResultBO.err(MessageCodeConstants.BANK_NAME_IS_NULL);
		}
		// 判断银行卡号是否为空
		if (StringUtil.isBlank(payBankCardVO.getCardcode())) {
			return ResultBO.err(MessageCodeConstants.CARD_CODE_IS_NULL_FIELD);
		}

		// 银行卡类型错误
        if (payBankCardVO.getBanktype() != UserConstants.BankCardType.DEPOSIT_CARD.getKey() && payBankCardVO.getBanktype() != UserConstants.BankCardType.CREDIT_CARD.getKey()) {
            return ResultBO.err(MessageCodeConstants.BANK_TYPE_IS_NULL_OR_ERROR);
        }

		if (StringUtil.isBlank(payBankCardVO.getRealname())) {
			return ResultBO.err(MessageCodeConstants.REALNAME_IS_NULL_FIELD);
		}
        //真实姓名为空
        if (!payBankCardVO.getRealname().contains("*")){
			//验证真实姓名
			ResultBO<?> validateRealName = ValidateUtil.validateRealName(payBankCardVO.getRealname());
			if (validateRealName.isError()) {
				return validateRealName;
			}
			//验证身份证号
			ResultBO<?> validateIdCard = ValidateUtil.validateIdCard(payBankCardVO.getIdCard());
			if (validateIdCard.isError()) {
				return validateIdCard;
			}
		}


        ResultBO<?> resultBO = validateCreditCard(payBankCardVO);
        if (resultBO != null) {
			return resultBO;
		}

		// 验证码校验
		if (StringUtil.isBlank(payBankCardVO.getCode())) {
			return ResultBO.err(MessageCodeConstants.VERIFYCODE_IS_NULL_FIELD);
		} else {
             /*if(!"123456".equals(payBankCardVO.getCode())) {
			    return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
			}*/
			Integer errCount = 0;
			//redisUtil.delString(userId+mobile+CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT);
			String countStr = redisUtil.getString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile());
			if (ObjectUtil.isBlank(countStr)) {
				countStr = "0";
			}
			errCount = Integer.valueOf(countStr);
			String redisCode = redisUtil.getString(payBankCardVO.getMobile() + UserConstants.MessageTypeEnum.OTHER_MSG.getKey());
			// 根据缓存判断验证码有效性
			if (ObjectUtil.isBlank(redisCode)) {
				return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
			}
            if (!payBankCardVO.getCode().equals(redisCode)) {
				errCount++;
				UserInfoPO infoPO = new UserInfoPO();
				infoPO.setId(userInfoBO.getId());
				if (errCount == 3) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(DateUtil.addHour(new Date(),1));
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile(), errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(userInfoBO)) {
						userInfoBO.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(payBankCardVO.getToken(), userInfoBO, payBankCardVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(payBankCardVO.getIp(),userInfoBO.getId());
					userUtil.clearUserToken(payBankCardVO.getToken());
					userUtil.clearUserById(userInfoBO.getId());
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_THREE);
				} else if (errCount == 8) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(DateUtil.addHour(new Date(),3));
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile(), errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(userInfoBO)) {
						userInfoBO.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(payBankCardVO.getToken(), userInfoBO, payBankCardVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(payBankCardVO.getIp(),userInfoBO.getId());
					userUtil.clearUserToken(payBankCardVO.getToken());
					userUtil.clearUserById(userInfoBO.getId());
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_EIGHT);
				} else if (errCount > 10) {
					infoPO.setAccountStatus(UserConstants.IS_FALSE);
					infoPO.setForbitEndTime(getTime());
					userInfoDaoMapper.updateUserInfo(infoPO);
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile(), errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
					if (!ObjectUtil.isBlank(userInfoBO)) {
						userInfoBO.setAccountStatus(UserConstants.IS_FALSE);
						publicMethod.modifyCache(payBankCardVO.getToken(), userInfoBO, payBankCardVO.getPlatform());
					}
					publicMethod.insertUserForbitLog(payBankCardVO.getIp(),userInfoBO.getId());
					userUtil.clearUserToken(payBankCardVO.getToken());
					userUtil.clearUserById(userInfoBO.getId());
					return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_TEN);
				} else {
					redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile(), errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
				}
                return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
            }
            UserMessagePO userMessagePO = new UserMessagePO();
			userMessagePO.setAccount(payBankCardVO.getMobile());
			userMessagePO.setCode(payBankCardVO.getCode());
			userMessagePO.setStatus(UserConstants.IS_TRUE);
			verifyCodeDaoMapper.updateVerifyCodeStatus(userMessagePO);
			// 验证完成，清除缓存
			redisUtil.delString(payBankCardVO.getMobile() + UserConstants.MessageTypeEnum.OTHER_MSG.getKey());

		}

		// 获取具体的银行信息
		ResultBO<?> bo = findPayBankByCard(payBankCardVO.getCardcode());
		if (bo.isError()) {
			return bo;
		}

		if (!ObjectUtil.isBlank(payBankCardVO.getIsdefault()) && payBankCardVO.getIsdefault().equals(UserConstants.IS_TRUE)) {
			// 如果有设置默认银行卡，校验是不是已经设置过，如果有，就返回提示
			List<PayBankcardBO> list = bankcardMapper.selectBankCard(payBankCardVO);
			if (!ObjectUtil.isBlank(list)) {
				return ResultBO.err(MessageCodeConstants.DEFAULT_BANK_CARD_EXIST_SERVICE);
			}
		}
		// 判断此用户是否已经添加过此张银行卡
		List<?> list = bankcardMapper.selectByUserIdAndCardCodeIsExist(userInfoBO.getId().toString(), payBankCardVO.getCardcode());
		if (!ObjectUtil.isBlank(list)) {
			return ResultBO.err(MessageCodeConstants.IS_USER_BANKCARD_EXIST_SERVICE);
		}
		return null;
	}

	/**
	 * 查询用户银行卡信息
	 *
	 * @param token 用户唯一标识
	 * @return ResultBO
	 * @throws Exception 异常
	 */
	@Override
	public ResultBO<?> selectBankCard(String token) throws Exception {
		return selectBankCardByType(token,null);
	}

	@Override
	public ResultBO<?> selectBankCardByType(String token, Short bankType) throws Exception {
		logger.debug("BankcardServiceImpl.selectBankCard input params >>> " + token);
		UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		List<BankCardDetailBO> list = findUserBankList(userInfoBO.getId(), bankType);
		return ResultBO.ok(list);
	}

	/**
	 * @param token    用户唯一标识
	 * @param bankId   银行id
	 * @param bankType 卡类型
	 * @return ResultBO
	 * @throws Exception
	 */
	@Override
	public ResultBO<?> getCardLimitDetail(String token, Short bankId, Short bankType) throws Exception {
		UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		PayBankLimitBO bankLimitBO = bankLimitDaoMapper.getPayBankLimitByBankId(bankId.intValue(), bankType);
		return ResultBO.ok(bankLimitBO);
	}

	/**
	 * 根据id删除银行卡,逻辑删除，修改状态为0
	 *
	 * @param token 用户唯一标识
	 * @param id    ID
	 * @return ResultBO
	 * @throws Exception 异常
	 */
	@Override
	public ResultBO<?> deleteByBankCardId(String token, Integer id) throws Exception {
		logger.debug("BankcardServiceImpl.deleteByBankCardId input params >>> " + id);
		UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		int row = bankcardMapper.deleteByBankCardId(id, userInfoBO.getId());
		if (row > 0) {
			 clearUserBankCache(userInfoBO);// 清除用户的支付方式缓存
			return ResultBO.ok();
		} else {
			return ResultBO.err(MessageCodeConstants.NOT_FIND_USER_CARD_SERVICE);
		}
	}

	private ResultBO<?> findPayBankByCard(String bankCard) {
		// 比对银行卡信息
		// 1从缓存里面获取PayBankSegmentBO集合
		List<PayBankSegmentBO> payBankSegmentBOList = redisUtil.getObj(CacheConstants.PAY_BANK_SEGMENTBO_LIST_KEY, new ArrayList<PayBankSegmentBO>());
		PayBankSegmentBO bo = null;
		if(ObjectUtil.isBlank(payBankSegmentBOList)){
	    	 payBankSegmentBOList = bankcardSegmentService.selectGroup();
		}
		for (PayBankSegmentBO payBankSegmentBO : payBankSegmentBOList) {
			if (!ObjectUtil.isBlank(payBankSegmentBO.getTopCut())) {
				// 先检索是不是符合xxxxx开头的
				if (bankCard.startsWith(String.valueOf(payBankSegmentBO.getTopCut()))) {
					bo = payBankSegmentBO;
					break;
				}
			}
		}
		// 如果对象为空，表示卡号不符合要求
		if (ObjectUtil.isBlank(bo)) {
			return ResultBO.err(MessageCodeConstants.BANKCARD_ERROR_SERVICE);
		} else if (bankCard.length() != bo.getCardLength()) {
			// 如果是以xxxx开头的，再检查卡号长度是不是一样
			return ResultBO.err(MessageCodeConstants.BANKCARD_ERROR_SERVICE);
		}
		return ResultBO.ok(bo);
	}

	@Override
	public List<PayBankcardBO> findUserBankList(Integer userId) {
		PayBankcardVO payBankcardVO = new PayBankcardVO(userId);
		List<PayBankcardBO> list =  bankcardMapper.selectBankCard(payBankcardVO);
		if (!ObjectUtil.isBlank(list)) {
			for (PayBankcardBO bankcardBO : list) {
				bankcardBO.setCardcode(StringUtil.hideString(bankcardBO.getCardcode(), (short) 2));
			}
		}
		return list;
	}

	private List<BankCardDetailBO> findUserBankList(Integer userId, Short bankType) {
		List<BankCardDetailBO> list = bankcardMapper.findBankList(userId);
		if (!ObjectUtil.isBlank(list)) {
			for (BankCardDetailBO bankCardDetailBO : list) {
				bankCardDetailBO.setBlogo(before_file_url + bankCardDetailBO.getBlogo());
				bankCardDetailBO.setSlogo(before_file_url + bankCardDetailBO.getSlogo());
				bankCardDetailBO.setCardcode(StringUtil.hideString(bankCardDetailBO.getCardcode(), (short) 6));
			}
		}
		return list;
	}

	@Override
	public List<PayBankcardBO> findUserBankListFromCache(Integer userId) {
		String key = CacheConstants.P_CORE_USER_BANK_CARD_LIST + userId;
		List<PayBankcardBO> list = redisUtil.getObj(key, new ArrayList<PayBankcardBO>());
		if (ObjectUtil.isBlank(list)) {
			list = findUserBankList(userId);
			if (!ObjectUtil.isBlank(list)) {
				redisUtil.addObj(key, list, CacheConstants.ONE_HOURS);
			}
		}
		return list;
	}

	@Override
	public void findBankByIdAndCheckName(Integer userId, TakenReqParamVO takenReqParamVO) {
		PayBankcardBO payBankcardBO = bankcardMapper.getUserBankById(userId, takenReqParamVO.getBankCardId());
		if (!ObjectUtil.isBlank(payBankcardBO)) {
			// 银行名称不相等，表示用户修改过，需要保存用户输入
			if (!payBankcardBO.getBankname().equals(takenReqParamVO.getBankName())) {
				PayBankcardPO payBankcardPO = new PayBankcardPO();
				payBankcardPO.setId(payBankcardBO.getId());
				payBankcardPO.setBankname(takenReqParamVO.getBankName());
				bankcardMapper.updateBankName(payBankcardPO);
			}
		}
	}

	@Override
	public PayBankcardBO getSingleBankCard(Integer userId) {
		List<PayBankcardBO> list = findUserBankListFromCache(userId);
		PayBankcardBO payBankcard = null;
		if (!ObjectUtil.isBlank(list)) {
			int num = 0;
			for (PayBankcardBO payBankcardBO : list) {
				if (num == 0) {
					payBankcard = payBankcardBO;
				}
				// 是否有默认不为空并且是默认的，取默认的
				if (!ObjectUtil.isBlank(payBankcardBO.getIsdefault()) && Integer.valueOf(payBankcardBO.getIsdefault()).equals(PayConstants.IsDefaultEnum.TRUE.getKey())) {
					payBankcard = payBankcardBO;
					break;
				}
				num++;
			}
		}
		return payBankcard;
	}

	@Override
	public PayBankcardBO getBankCardByCardCodeFromCache(String cardCode, Integer userId) {
		List<PayBankcardBO> list = findUserBankListFromCache(userId);
		PayBankcardBO payBankcard = null;
		if (!ObjectUtil.isBlank(list)) {
			for (PayBankcardBO payBankcardBO : list) {
				if (payBankcardBO.getCardcode().equals(cardCode)) {
					return payBankcardBO;
				}
			}
		}
		return payBankcard;
	}

	/**
	 * 获取用户支付方式列表
	 *
	 * @param token
	 * @return
	 */
	@Override
	public List<UserPayTypeBO> findUserPayTypes(String token, String orderBy) {
		UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (ObjectUtil.isBlank(userInfoBO)) {
			return null;
		}
		String key = CacheConstants.P_CORE_USER_PAY_CHANNEL + userInfoBO.getId() + orderBy;
		List<UserPayTypeBO> all = redisUtil.getObj(key, new ArrayList<UserPayTypeBO>());
		if (ObjectUtil.isBlank(all)) {
			all = new ArrayList<UserPayTypeBO>();
			List<UserPayTypeBO> userPayTypeList = new ArrayList<UserPayTypeBO>();
			// 用户排序后的银行卡信息
			List<PayBankcardBO> payBankCardBOs = bankcardMapper.findPayBankCardByUserId(userInfoBO.getId(), orderBy);
			UserPayTypeBO returnBO = null;

			// 以下操作是第三方支付
			List<PayBankBO> bankList = payBankService.findBankByType(PayConstants.PayBankPayTypeEnum.THIRD.getKey());// 获取第三方的支付方式
			Short cardType = PayConstants.PayChannelCardTypeEnum.THIRD.getKey();
			if (!ObjectUtil.isBlank(bankList)) {
				for (PayBankBO payBankBO : bankList) {
					// 获取可用的支付渠道
					List<PayChannelBO> channelList = getPayChannel(payBankBO.getId(), cardType, orderBy);
					// 没有可用的渠道，执行下面一条
					if (ObjectUtil.isBlank(channelList)) {
						continue;
					}
					returnBO = new UserPayTypeBO();
					returnBO.setBankId(payBankBO.getId());
					returnBO.setBankName(payBankBO.getcName());
					returnBO.setBankType(PayConstants.PayTypeEnum.THIRD_PAYMENT.getKey());// 默认第三方支付 银行卡类型:1储蓄卡;2信用卡;3第三方支付
					returnBO.setFlag(payBankBO.getStatus());// 0停用 1可用
					returnBO.setsLogo(payBankBO.getsLogo());// 小图标
					returnBO.setbLogo(payBankBO.getbLogo());// 大图标
					// 判断支付渠道是否可用
					checkChannel(channelList, returnBO);
					// 用户最近支付id 等于存的id 默认显示它
					if (!ObjectUtil.isBlank(userInfoBO.getUserPayId())) {
						if (userInfoBO.getUserPayId().equals(payBankBO.getId())) {
							returnBO.setIsRecentlyPay(payBankBO.getId());// 最近使用支付方式id
							all.add(returnBO);
							continue;
						}
					}
					userPayTypeList.add(returnBO);
				}
			}
			List<PayBankBO> bankList2 = payBankService.findBankByType(PayConstants.PayBankPayTypeEnum.BANK_CARD.getKey());
			for (PayBankcardBO payBankCardBO : payBankCardBOs) {
				PayBankBO payBank = null;
				for (PayBankBO payBankBO : bankList2) {// 验证银行状态是否是开启，银行关闭的话，执行下一个
					if (payBankCardBO.getBankid().equals(payBankBO.getId())) {
						payBank = payBankBO;
						break;
					}
				}
				if (ObjectUtil.isBlank(payBank)) {
					continue;
				}
				// 获取可用的支付渠道
				List<PayChannelBO> channelList = getPayChannel(payBankCardBO.getBankid(), payBankCardBO.getBanktype(), orderBy);
				// 没有可用的渠道，执行下面一条
				if (ObjectUtil.isBlank(channelList)) {
					logger.info("银行【"+payBank.getcName()+"】没有可用的支付渠道！");
					continue;
				}
				returnBO = new UserPayTypeBO();
				returnBO.setBankId(payBankCardBO.getBankid());
				returnBO.setBankName(payBank.getcName());
				returnBO.setBankType(payBankCardBO.getBanktype());
				// 格式化银行卡号变为 **1234
				returnBO.setCardCode(StringUtil.hideHeadString(payBankCardBO.getCardcode()));
				// 是否开启快捷支付为空，默认0（未开通）
				returnBO.setOpenBank(ObjectUtil.isBlank(payBankCardBO.getOpenbank()) ? 0 : payBankCardBO.getOpenbank());
				returnBO.setBankCardId(payBankCardBO.getId());// 银行卡ID
				returnBO.setOverdue(payBankCardBO.getOverdue());
				returnBO.setFlag(payBankCardBO.getStatus());// 银行状态 0停用 1可用
				PayBankBO payBankBO = payBankService.findBankFromCache(payBankCardBO.getBankid());
				returnBO.setsLogo(payBankBO.getsLogo());// 小图标
				returnBO.setbLogo(payBankBO.getbLogo());// 大图标
				// 银行卡类型:1储蓄卡;2信用卡;3第三方支付。判断是否为信用卡，信用卡判断有效期
				if (payBankCardBO.getBanktype().equals(PayConstants.BankCardTypeEnum.CREDIT.getKey()) && !ObjectUtil.isBlank(payBankCardBO.getOverdue())) {
					try {
						boolean overdueValidate = DateUtil.validateCredCardOver(payBankCardBO.getOverdue());
						if (!overdueValidate) {
							returnBO.setReason(Constants.CREDIT_EXPIRED_TIPS);
							returnBO.setFlag(PayConstants.BankStatusEnum.EXPIRED.getKey());
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				// 判断支付渠道是否可用
				checkChannel(channelList, returnBO);
				// 用户最近支付id 等于存的id 默认显示它
				if (!ObjectUtil.isBlank(userInfoBO.getUserPayId())) {
					if (userInfoBO.getUserPayId().equals(payBankCardBO.getBankid())) {
						returnBO.setIsRecentlyPay(payBankCardBO.getBankid());// 最近使用支付方式id
						all.add(returnBO);
						continue;
					}
				}
				userPayTypeList.add(returnBO);
			}
			all.addAll(userPayTypeList);
			if (!ObjectUtil.isBlank(all)) {
				redisUtil.addObj(key, all, CacheConstants.ONE_DAY);// 存一天
			}
		}
		return all;
	}

	private void checkChannel(List<PayChannelBO> channelList, UserPayTypeBO userPayTypeBO) {
		// 前面状态是可使用的，再判断所有支付渠道是否都不满足
		if (userPayTypeBO.getFlag().equals(PayConstants.BankStatusEnum.OPEN.getKey())) {
			boolean flag = false;// 是否有暂停时间
			boolean canUse = false;// 有可使用的渠道
			Date minEndTime = null;// 设置一个最晚结束时间
			for (PayChannelBO payChannelBO : channelList) {
				// 支付渠道是可用的，才进行下一步验证
				if (payChannelBO.getAvailable().equals(PayConstants.BankStatusEnum.OPEN.getKey())) {
					canUse = true;// 支付渠道有可用的，就将能使用设置成true
					// 是否暂停是打开的，要验证暂停的开始时间及结束时间
					if (payChannelBO.getPause().equals(PayConstants.BankStatusEnum.OPEN.getKey())) {
						Date stopBeginTime = payChannelBO.getBeginTime();
						Date stopEndTime = payChannelBO.getEndTime();
						if (ObjectUtil.isBlank(minEndTime)) {
							minEndTime = stopEndTime;// 赋值最晚维护时间
						} else {
							int num = DateUtil.compare(minEndTime, stopEndTime);
							if (num > 0) {
								minEndTime = stopEndTime;// 赋值最晚维护时间
							}
						}
						if (!ObjectUtil.isBlank(stopBeginTime) && !ObjectUtil.isBlank(stopEndTime)) {
							userPayTypeBO.setStartTime(stopBeginTime);
							userPayTypeBO.setEndTime(stopEndTime);
							Date nowDate = DateUtil.getNowDate(null);
							int bigBigin = DateUtil.compare(nowDate, stopBeginTime);
							int bigEnd = DateUtil.compare(nowDate, stopEndTime);
							// 当前时间在暂停使用时间内
							if (bigBigin >= 0 && bigEnd <= 0) {
								userPayTypeBO.setFlag(PayConstants.BankStatusEnum.DISABLE.getKey());// 不可用
								// userPayTypeBO.setReason(MessageFormat.format(Constants.PAY_CHANNEL_STOP_USE, DateUtil.convertDateToStr(stopEndTime, DateUtil.DEFAULT_FORMAT)));
							} else {
								flag = true;// 有暂停，但不在开始结束时间内，就跳出
								break;
							}
						}
					} else {
						flag = true;// 有一个没有暂停的，就跳出
						break;
					}
				}
			}
			if (!canUse) {// 所有渠道都不可用，设置成不可用
				userPayTypeBO.setFlag(PayConstants.BankStatusEnum.DISABLE.getKey());// 设置成不可用
				userPayTypeBO.setReason(Constants.PAY_CHANNEL_STOP_USE_TIP);
			} else {
				// 如果多个渠道有一个不为空，返回可用
				if (flag) {
					userPayTypeBO.setFlag(PayConstants.BankStatusEnum.OPEN.getKey());// 可用
					userPayTypeBO.setReason("");
				} else {
					// 设置最早可以使用的时间
					userPayTypeBO.setReason(MessageFormat.format(Constants.PAY_CHANNEL_STOP_USE, DateUtil.convertDateToStr(minEndTime, DateUtil.DEFAULT_FORMAT)));
				}
			}
		}
	}

	/**
	 * 获取该银行卡是否能够使用，
	 * @param orderBy       入口端
	 * @return
	 */
	private List<PayChannelBO> getPayChannel(Integer bankId, Short bankType, String orderBy) {
		// 该银行所有可选的支付渠道
		List<PayChannelBO> payChannelBOs = payChannelService.findChannelByBankIdUseCache(bankId);
		List<PayChannelBO> list = new ArrayList<PayChannelBO>();
		for (PayChannelBO payChannelBO : payChannelBOs) {
			// 银行卡类型相同，添加到list中
			if (payChannelBO.getCardType().equals(bankType)) {
				// 验证支付渠道在当前平台是否可以使用，不能使用不添加
				boolean flag = checkChannelStatusForPlatform(orderBy, payChannelBO);
				if (flag) {
					list.add(payChannelBO);
				}
			}
		}
		return list;
	}

	/**
	 * 方法说明: 验证各个平台的支付状态是否可用
	 *
	 * @param orderBy
	 * @param payChannelBO
	 * @auth: xiongJinGang
	 * @time: 2017年4月12日 上午10:23:28
	 * @return: boolean
	 */
	private boolean checkChannelStatusForPlatform(String orderBy, PayChannelBO payChannelBO) {
		EntranceEnum entranceEnum = PayEnum.EntranceEnum.getEnum(orderBy);
		if (!ObjectUtil.isBlank(entranceEnum)) {
			switch (entranceEnum) {
			case PC:
				return payChannelBO.getPc().equals(Short.parseShort(PayEnum.IsStartEnum.START.getValue() + ""));
			case H5:
				return payChannelBO.getH5().equals(Short.parseShort(PayEnum.IsStartEnum.START.getValue() + ""));
			case IOS:
				return payChannelBO.getIos().equals(Short.parseShort(PayEnum.IsStartEnum.START.getValue() + ""));
			case ANDROID:
				return payChannelBO.getAndroid().equals(Short.parseShort(PayEnum.IsStartEnum.START.getValue() + ""));
			default:
				return false;
			}
		}
		return false;
	}

	/**
	 * 移动端查询用户银行卡信息
	 *
	 * @param token 用户唯一标识
	 * @return ResultBO
	 */
	@Override
	public ResultBO<?> selectCardForModile(String token) {
		UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}

		List<PayChannelBO> channelBOList;
		PayChannelBO channelBO;
		// 获取账户、红包余额及，红包过期数
		List<PayBankCardH5BO> h5BOList = this.bankcardMapper.selectForMobileBlance(userInfoBO.getId());
		// 获取到银行卡列表
		List<PayBankcardBO> list = bankcardMapper.selectBankCardForMobile(new PayBankcardVO(userInfoBO.getId()));
		// 最终返回对象
		PayBankCardH5BO payBankCardH5BO = new PayBankCardH5BO();
		PayChannelVO channelVO;
		for (PayBankcardBO bankcardBO : list) {
			String cardCode = bankcardBO.getCardcode();
			bankcardBO.setCardcode(StringUtil.hideString(cardCode, (short) 6));
			bankcardBO.setBlogo(before_file_url+bankcardBO.getBlogo());
			bankcardBO.setSlogo(before_file_url+bankcardBO.getSlogo());
			channelVO = new PayChannelVO(String.valueOf(bankcardBO.getBankid()), UserConstants.BankCardStatusEnum.EFFECTIVE.getKey().toString(), UserConstants.BankCardStatusEnum.EFFECTIVE.getKey().toString());
			// 根据银行id查询启用状态的渠道
			channelBOList = payChannelService.selectByCondition(channelVO);
			// 如果启用渠道为空,查询维护时间最小的一条
			if (ObjectUtil.isBlank(channelBOList)) {
				channelVO = new PayChannelVO(String.valueOf(bankcardBO.getBankid()), UserConstants.BankCardStatusEnum.DELETE.getKey().toString(), UserConstants.BankCardStatusEnum.DELETE.getKey().toString(), true);
				channelBOList = payChannelService.selectByCondition(channelVO);
				if (!ObjectUtil.isBlank(channelBOList)) {
					channelBO = channelBOList.get(0);
					// 存进bankCardBO对象
					bankcardBO.setEndTime(channelBO.getEndTime());
				}
			}
			// 如果信用已经过期，设置为0，表示无效
			if (bankcardBO.getBanktype().equals(UserConstants.BankCardType.CREDIT_CARD.getKey())) {
				try {
					if (!DateUtil.validateCredCardOver(bankcardBO.getOverdue())) {
						bankcardBO.setOverdue(UserConstants.BankCardStatusEnum.DELETE.getKey().toString());
					} else {
						bankcardBO.setOverdue(UserConstants.BankCardStatusEnum.EFFECTIVE.getKey().toString());
					}
				} catch (ParseException e) {
					logger.error("BankcardServiceImpl addBankCard 信用卡有效期错误:" + e);
					e.printStackTrace();
				}
            } else {
                bankcardBO.setOverdue(UserConstants.BankCardStatusEnum.EFFECTIVE.getKey().toString());
            }

		}
		if (!ObjectUtil.isBlank(h5BOList)) {
			payBankCardH5BO = h5BOList.get(0);
		}
		if (!ObjectUtil.isBlank(list)) {
			payBankCardH5BO.setBankCardListDatas(list);
		}

		return ResultBO.ok(payBankCardH5BO);
	}

	@Override
	public PayBankcardBO findUserBankById(Integer userId, Integer bankCardId) {
		return bankcardMapper.getUserBankById(userId, bankCardId);
	}

	/**
	 * 根据银行卡号取到银行卡名称，类型
	 *
	 * @param token    用户唯一标识
	 * @param cardCode 银行卡号
	 * @return resultBO
	 */
	@Override
	public ResultBO<?> getBankName(String token, String cardCode) {
		UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
        // 判断此用户是否已经添加过此张银行卡
        List<?> list = bankcardMapper.selectByUserIdAndCardCodeIsExist(userInfoBO.getId().toString(), cardCode);

        if(!ObjectUtil.isBlank(list)) {
            return ResultBO.err(MessageCodeConstants.IS_USER_BANKCARD_EXIST_SERVICE);
        }
		AddBankCardFirsBO addBankCardFirsBO = new AddBankCardFirsBO();
		addBankCardFirsBO.setCardCode(cardCode);
        addBankCardFirsBO.setUserName(StringUtil.hideString(userInfoBO.getRealName(), (short) 5));
		addBankCardFirsBO.setMobile(userInfoBO.getMobile());
		String idCard = userInfoBO.getIdCard();
		if (!StringUtil.isBlank(idCard)) {
			addBankCardFirsBO.setIdCard(StringUtil.hideString(idCard, (short) 1));
		}

		ResultBO<?> resultBO = bankcardSegmentService.findPayBankSegmentByCard(cardCode);
		PayBankSegmentBO payBankSegmentBO;
		if (!ObjectUtil.isBlank(resultBO.getData())) {
			payBankSegmentBO = (PayBankSegmentBO) resultBO.getData();
			addBankCardFirsBO.setBankId(payBankSegmentBO.getBankId().toString());
			if (payBankSegmentBO.getBankId() != null) {
				PayBankBO payBankBO = payBankService.findBankById(payBankSegmentBO.getBankId());
				if (!ObjectUtil.isBlank(payBankBO)) {
					addBankCardFirsBO.setBankName(payBankBO.getName());
				} else {
					addBankCardFirsBO.setBankName(payBankSegmentBO.getBankName());
				}
				addBankCardFirsBO.setCardType(payBankSegmentBO.getCardType().intValue());
			}
            String slog = bankcardMapper.selectPaybank(payBankSegmentBO.getBankId());
            addBankCardFirsBO.setSlog(before_file_url + slog);
        }

        return resultBO.getSuccess() == ResultBO.getErr() ? resultBO : ResultBO.ok(addBankCardFirsBO);
	}

	@Override
	public ResultBO<?> findUserBankCardByCardId(Integer userId, Integer bankCardId) {
		// 用户的钱包
		PayBankcardBO payBankcard = null;
		try {
			payBankcard = bankcardMapper.getUserBankById(userId, bankCardId);
		} catch (Exception e1) {
			logger.error("获取用户" + userId + " 的银行卡" + bankCardId + "信息异常" + e1.getMessage());
			return ResultBO.err(MessageCodeConstants.SYS_ERROR_SYS);
		}
		if (!ObjectUtil.isBlank(payBankcard)) {
			Short bankType = payBankcard.getBanktype();
			// 信用卡，要判断有效期
			if (PayConstants.BankCardTypeEnum.CREDIT.getKey().equals(bankType)) {
				String overdue = payBankcard.getOverdue();
				if (ObjectUtil.isBlank(overdue)) {
					return ResultBO.err(MessageCodeConstants.BANK_CARD_OVERDUE_IS_NULL_FIELD);
				}
				try {
					boolean isValidate = DateUtil.validateCreditCard(overdue);
					if (!isValidate) {
						return ResultBO.err(MessageCodeConstants.PAY_CREDIT_INVALID_ERROR_SERVICE);
					}
					// 返回银行卡信息
					return ResultBO.ok(payBankcard);
				} catch (Exception e) {
					// 比较出异常，说明信用卡存储格式有问题
					return ResultBO.err(MessageCodeConstants.PAY_CREDIT_FORMAT_ERROR_SERVICE);
				}
			}
			return ResultBO.ok(payBankcard);
		} else {
			return ResultBO.err(MessageCodeConstants.PAY_BANKCARD_NOT_FOUND_SERVICE);
		}
	}

	/**
	 * 1. 对卡号上的每位数字乘以权重。其规则是，如果卡号数字个数是偶数，则第一位乘以2，否则就乘以1，然后以后分别是,1,2,1,2,1,2;
	 * <p>
	 * 2. 如果每位数字乘以权重后超过9 ,则需要减去 9;
	 * <p>
	 * 3. 将所有的处理过的加权数字求和，用 数字 10 求模运算;
	 * <p>
	 * 4. 余数应该是0，否则可能是输入错误。也可能是一个假号。
	 *
	 * @param cardNo
	 * @return
	 */
	public static boolean validateCreditCard(String cardNo) {
		if (StringUtils.isBlank(cardNo) || !StringUtils.isNumeric(cardNo)) {
			return false;
		}
		int len = cardNo.length();
		boolean isOdd = len % 2 == 1;
		int total = 0;
		int tem = 0;
		for (int i = 0; i < len; i++) {
			tem = Integer.valueOf(String.valueOf(cardNo.charAt(i)));
			if (i == 0 && !isOdd) {

				tem = tem << 1;

			} else if (i > 0 && i % 2 == 0) {

				tem = tem << 1;

			}

			if (tem > 9) {
				tem = tem - 9;
			}
			total = total + tem;
		}
		return total % 10 == 0;
	}

	/**
	 * 获取校验码
	 *
	 * @param token  用户唯一标识
	 * @param mobile 手机号
	 * @return
	 */
	@Override
	public ResultBO<?> getValidateCode(String token, String mobile) throws Exception {
		UserInfoBO userInfoBO = userUtil.getUserByToken(token);
		if (userInfoBO == null) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
				|| (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
		}
		ResultBO<?> resultBo = ValidateUtil.validateMobile(mobile);
		if (resultBo.isError()) {
			return resultBo;
		}


        UserMessageVO messageVO = new UserMessageVO(mobile, UserConstants.MessageTypeEnum.OTHER_MSG.getKey(), UserConstants.IS_FALSE, UserConstants.VerifyCodeTypeEnum.SMS.getKey());
        UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);

        ResultBO<?> resultBO = sendCodeUtil.sendCodeEntrance(mobile, UserConstants.MessageTypeEnum.OTHER_MSG.getKey(), userMessageBO, userInfoBO.getId());
        if (!ObjectUtil.isBlank(resultBO)) {
			return resultBO;
		}
		String count = redisUtil.getString(sendCodeUtil.getNowDate() + mobile + UserConstants.MessageTypeEnum.OTHER_MSG.getKey().toString());
        logger.debug("添加银行卡时当天发送条数：" + count);
        SendSmsCountBO sendSmsCountBO = new SendSmsCountBO(Integer.parseInt(count));
        return ResultBO.ok(sendSmsCountBO);
	}

	/**
	 * 设置默认银行卡
	 *
	 * @param payBankcardVO 数据对象
	 * @return ResultBO
	 * @throws Exception
	 */
	@Override
	public ResultBO<?> updateDefault(PayBankcardVO payBankcardVO) throws Exception {
		int row, row2;
		PayBankcardPO payBankcardPO = new PayBankcardPO();
		BeanUtils.copyProperties(payBankcardVO, payBankcardPO);
		payBankcardPO.setUserId(payBankcardVO.getUserid());
		row = bankcardMapper.updateDefault(payBankcardPO);
		row2 = bankcardMapper.updateDisableDefault(payBankcardPO);
		if (row > 0 && row2 > 0) {
			return ResultBO.ok();
		} else {
			return ResultBO.err("updateDefault is error");
		}
	}
    
    
    /**
     * 修改银行预留手机号
     *
     * @param payBankcardVO 参数及筛选条件
     * @return ResultBO
     * @throws Exception 异常
     */
    @Override
    public ResultBO<?> updateOpenMobile (PayBankcardVO payBankcardVO) throws Exception {
        int row;
        UserInfoBO userInfoBO = userUtil.getUserByToken(payBankcardVO.getToken());
        if(userInfoBO == null) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        ResultBO<?> resultBo = ValidateUtil.validateMobile(payBankcardVO.getMobile());
        if(resultBo.isError()) {
            return resultBo;
        }
        PayBankcardPO payBankcardPO = new PayBankcardPO();
        BeanUtils.copyProperties(payBankcardVO, payBankcardPO);
        payBankcardPO.setUserId(userInfoBO.getId());
        row = bankcardMapper.updateOpenMobile(payBankcardPO);
        if(row > 0) {
            PayBankcardBO payBankcardBO = bankcardMapper.getUserBankById(userInfoBO.getId(), payBankcardPO.getId());
            return ResultBO.ok(payBankcardBO);
        } else {
            return ResultBO.err("updateDefault is error");
        }
    }
    
    /**
     * 方法说明: 清除缓存
	* @auth: xiongJinGang
	* @param userInfoBO
	* @time: 2017年5月26日 下午3:53:00
	* @return: void 
	*/
	private void clearUserBankCache(UserInfoBO userInfoBO) {
		redisUtil.delAllString(CacheConstants.P_CORE_USER_PAY_CHANNEL + userInfoBO.getId());
		redisUtil.delAllString(CacheConstants.P_CORE_USER_BANK_CARD_LIST+ userInfoBO.getId());
		redisUtil.delAllString(CacheConstants.P_CORE_PAY_BANK_CHANNEL_SINGLE);
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
