package com.hhly.usercore.remote.member.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.PayConstants;
import com.hhly.skeleton.base.constants.PayConstants.TakenValidateTypeEnum;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.base.util.TokenUtil;
import com.hhly.skeleton.pay.bo.PayBankBO;
import com.hhly.skeleton.pay.bo.PayBankcardBO;
import com.hhly.skeleton.pay.vo.TakenBankCardVO;
import com.hhly.skeleton.pay.vo.TakenUserInfoVO;
import com.hhly.skeleton.pay.vo.TakenUserWalletVO;
import com.hhly.skeleton.pay.vo.TakenValidateTypeVO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.TakenUtil;
import com.hhly.usercore.base.utils.TransValidateUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.local.bank.service.PayBankService;
import com.hhly.usercore.local.member.service.MemberWalletService;
import com.hhly.usercore.persistence.pay.dao.TransTakenMapper;
import com.hhly.usercore.remote.member.service.IMemberBankcardService;
import com.hhly.usercore.remote.member.service.ITransTakenService;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;

/**
 * @desc 提款交易实现类
 * @author xiongjingang
 * @date 2017年3月2日 上午10:51:55
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("transTakenService")
public class TransTakenServiceImpl extends TransValidateUtil implements ITransTakenService {

	private static final Logger logger = Logger.getLogger(TransTakenServiceImpl.class);

	@Resource
	private TransTakenMapper transTakenMapper;
	@Resource
	private IMemberBankcardService memberBankcardService;
	@Resource
	private PayBankService payBankService;
	@Resource
	private IVerifyCodeService verifyCodeService;
	@Resource
	private MemberWalletService memberWalletService;
	@Resource
	private RedisUtil redisUtil;
    
	@Autowired
	private UserUtil userUtil;
	// PC端 提款请求第一步
	@Override
	public ResultBO<?> takenReq(String token) throws Exception {
		UserInfoBO userInfo = userUtil.getUserByToken(token);
		if (ObjectUtil.isBlank(userInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		Integer userId = userInfo.getId();
		// 验证用户提款次数
		ResultBO<?> resultBO = findUserTakenTimesAndValidate(userId);
		if (resultBO.isError()) {
			return resultBO;
		}
		List<TakenValidateTypeVO> list = new ArrayList<TakenValidateTypeVO>();
		TakenValidateTypeVO takenValidateType = null;
		TakenValidateTypeEnum takenValidateTypeEnum = null;
		String idCard = userInfo.getIdCard();
		if (!ObjectUtil.isBlank(idCard)) {
			takenValidateTypeEnum = PayConstants.TakenValidateTypeEnum.IDCARD;
			takenValidateType = new TakenValidateTypeVO(takenValidateTypeEnum, idCard);
			list.add(takenValidateType);
		}
		PayBankcardBO bankCard = memberBankcardService.getSingleBankCard(userId);
		if (!ObjectUtil.isBlank(bankCard)) {
			takenValidateTypeEnum = PayConstants.TakenValidateTypeEnum.BANKCARD;
			takenValidateType = new TakenValidateTypeVO(takenValidateTypeEnum, bankCard.getCardcode());
			takenValidateType.setBankName(bankCard.getBankname());
			takenValidateType.setRealName(userInfo.getRealName());// 实名认证
			list.add(takenValidateType);
		} else {
			// 未绑定卡
			return ResultBO.err(MessageCodeConstants.NOT_BIND_BANK_CARD);
		}

		String mobile = userInfo.getMobile();
		if (!ObjectUtil.isBlank(mobile)) {
			takenValidateTypeEnum = PayConstants.TakenValidateTypeEnum.MOBILE;
			takenValidateType = new TakenValidateTypeVO(takenValidateTypeEnum, mobile);
			list.add(takenValidateType);
		}

		String email = userInfo.getEmail();
		if (!ObjectUtil.isBlank(email)) {
			takenValidateTypeEnum = PayConstants.TakenValidateTypeEnum.EMAIL;
			takenValidateType = new TakenValidateTypeVO(takenValidateTypeEnum, email);
			list.add(takenValidateType);
		}

		if (!ObjectUtil.isBlank(list)) {
			redisUtil.addObj(CacheConstants.P_CORE_USER_TAKEN_VALIDATE_TYPE_LIST + userId, list, null);
		}
		return ResultBO.ok(list);
	}

	// PC端 提款请求第二步
	@Override
	public ResultBO<?> takenReqValidate(TakenValidateTypeVO takenValidateTypeVO) throws Exception {
		String token = takenValidateTypeVO.getToken();
		UserInfoBO userInfo = userUtil.getUserByToken(token);
		if (ObjectUtil.isBlank(userInfo)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
		}
		Integer userId = userInfo.getId();
		// 验证用户提款次数
		ResultBO<?> resultBO = findUserTakenTimesAndValidate(userId);
		if (resultBO.isError()) {
			return resultBO;
		}

		List<TakenValidateTypeVO> list = redisUtil.getObj(CacheConstants.P_CORE_USER_TAKEN_VALIDATE_TYPE_LIST + userId, new ArrayList<TakenValidateTypeVO>());
		if (ObjectUtil.isBlank(list)) {
			return ResultBO.err(MessageCodeConstants.TAKEN_VALIDATE_TYPE_NOT_FOUNE_ERROR_SERVICE);
		}

		Short type = takenValidateTypeVO.getType();
		TakenValidateTypeEnum takenValidateTypeEnum = PayConstants.TakenValidateTypeEnum.getEnumByKey(type);
		resultBO = TakenUtil.validateTakenReq(takenValidateTypeVO, takenValidateTypeEnum);
		if (resultBO.isError()) {
			return resultBO;
		}
		// 获取具体的验证方式
		TakenValidateTypeVO takenValidateTypeInfo = getTakenValidateTypeFromRedis(list, type);
		if (ObjectUtil.isBlank(takenValidateTypeInfo)) {
			return ResultBO.err(MessageCodeConstants.TAKEN_VALIDATE_TYPE_NOT_FOUNE_ERROR_SERVICE);
		}
		// 输入的验证参数
		String validateStr = takenValidateTypeVO.getValidateStr();
		String fullCode = takenValidateTypeInfo.getFullCode();
		if (ObjectUtil.isBlank(fullCode)) {
			return ResultBO.err(MessageCodeConstants.TAKEN_VALIDATE_TYPE_NOT_FOUNE_ERROR_SERVICE);
		}
		PassportVO passportVO = null;
		switch (takenValidateTypeEnum) {
		case EMAIL:
			// 调用邮箱验证码验证
			passportVO = new PassportVO(takenValidateTypeVO.getValidateStr(), token, UserConstants.MessageTypeEnum.DRAW_MSG.getKey());
			resultBO = verifyCodeService.checkEmailVerifyCode(passportVO);
			if (resultBO.isError()) {
				return resultBO;
			}
			break;
		case MOBILE:
			// 调用手机验证码验证
			passportVO = new PassportVO(takenValidateTypeVO.getValidateStr(), token, UserConstants.MessageTypeEnum.DRAW_MSG.getKey());
			resultBO = verifyCodeService.checkMobileVerifyCode(passportVO);
			if (resultBO.isError()) {
				return resultBO;
			}
			break;
		case IDCARD:
			String idCard = fullCode.substring(fullCode.length() - 8, fullCode.length());
			if (!idCard.equals(validateStr)) {
				return ResultBO.err(MessageCodeConstants.TAKEN_IDCARD_VALIDATE_CODE_ERROR_SERVICE);
			}
			break;
		case BANKCARD:
			String bankCard = fullCode.substring(fullCode.length() - 8, fullCode.length());
			if (!bankCard.equals(validateStr)) {
				return ResultBO.err(MessageCodeConstants.TAKEN_BANKCARD_VALIDATE_CODE_ERROR_SERVICE);
			}
			break;
		default:
			break;
		}
		// 获取银行卡的内容
		TakenValidateTypeVO bankValidate = getTakenValidateTypeFromRedis(list, PayConstants.TakenValidateTypeEnum.BANKCARD.getKey());
		if (ObjectUtil.isBlank(bankValidate)) {
			return ResultBO.err(MessageCodeConstants.NOT_BIND_BANK_CARD);
		}
		TakenUserInfoVO takenUserInfoVO = new TakenUserInfoVO();
		// takenUserInfoVO.setBankCard(bankValidate.getCode());
		// takenUserInfoVO.setBranchBankName(bankValidate.getName());
		takenUserInfoVO.setIdNo(StringUtil.hideString(userInfo.getIdCard(), (short) 1));// 身份证号码
		takenUserInfoVO.setRealName(bankValidate.getRealName());
		List<TakenBankCardVO> takenBankList = getUserBankInfo(userId, null);
		if (ObjectUtil.isBlank(takenBankList)) {
			return ResultBO.err(MessageCodeConstants.TAKEN_BANK_CARD_NOT_FOUNE_ERROR_SERVICE);
		}
		// 用户最近支付id 等于存的id 默认显示它
		if (!ObjectUtil.isBlank(userInfo.getUserPayId())) {
			for (int i = takenBankList.size() - 1; i >= 0; i--) {
				TakenBankCardVO takenBankCard = takenBankList.get(i);
				// 银行ID一致，并且是储蓄卡，将其
				if (takenBankCard.getBankId().equals(userInfo.getUserPayId()) && takenBankCard.getBankType().equals(PayConstants.BankCardTypeEnum.BANK_CARD.getKey())) {
					TakenBankCardVO first = takenBankCard;
					takenBankList.remove(takenBankCard);
					takenBankList.add(0, first);// 设置成第一位
				}
			}
		}
		// 提款展示给前端的钱包余额信息
		TakenUserWalletVO userWallet = memberWalletService.findWalletByUserId(userId);
		if (ObjectUtil.isBlank(userWallet)) {
			return ResultBO.err(MessageCodeConstants.PAY_USER_WALLET_ERROR_SERVICE);
		}
		takenUserInfoVO.setUserWallet(userWallet);

		takenUserInfoVO.setBankList(takenBankList);
		String takenToken = TokenUtil.createTokenStr();
		// takenUserInfoVO.setTakenToken(takenToken);
		redisUtil.addString(TakenUtil.makeTakenTokenKey(token), takenToken, CacheConstants.FIVE_MINUTES);
		return ResultBO.ok(takenUserInfoVO);
	}

	/**  
	* 方法说明: 获取用户可用储蓄卡列表
	* @auth: xiongJinGang
	* @param userId
	* @param bankCardId 银行卡ID，可以为空
	* @time: 2017年4月19日 上午10:30:44
	* @return: List<TakenBankCardVO> 
	*/
	private List<TakenBankCardVO> getUserBankInfo(Integer userId, Integer bankCardId) {
		List<PayBankcardBO> bankCardList = memberBankcardService.findUserBankList(userId);
		// 先不用缓存
		/*if (isTest.equals("true")) {// 测试环境先不用缓存
			bankCardList = memberBankcardService.findUserBankList(userId);
		} else {
			bankCardList = memberBankcardService.findUserBankListFromCache(userId);
		}*/
		List<PayBankBO> bankList = payBankService.findAllBank();
		List<TakenBankCardVO> takenBankList = new ArrayList<TakenBankCardVO>();
		TakenBankCardVO takenBankCardVO = null;
		if (ObjectUtil.isBlank(bankCardList)) {
			return takenBankList;
		}
		for (PayBankcardBO bankcard : bankCardList) {
			// 是储蓄卡，才进行下一步
			if (bankcard.getBanktype().equals(PayConstants.BankCardTypeEnum.BANK_CARD.getKey())) {
				if (ObjectUtil.isBlank(bankCardId)) {// 获取所有的储蓄卡.
					for (PayBankBO payBankBO : bankList) {
						if (bankcard.getBankid() == payBankBO.getId()) {
							takenBankCardVO = new TakenBankCardVO();
							setTakenBankCardVO(takenBankCardVO, payBankBO, bankcard);
							takenBankCardVO.setIsDefault(bankcard.getIsdefault());
							takenBankList.add(takenBankCardVO);
							break;
						}
					}
				} else {
					// 银行卡ID一致，获取银行简称
					if (bankcard.getId().equals(bankCardId)) {
						for (PayBankBO payBankBO : bankList) {
							if (bankcard.getBankid().equals(payBankBO.getId())) {
								takenBankCardVO = new TakenBankCardVO();
								setTakenBankCardVO(takenBankCardVO, payBankBO, bankcard);
								takenBankCardVO.setIsDefault(bankcard.getIsdefault());
								takenBankList.add(takenBankCardVO);
								break;
							}
						}
						break;
					}
				}
			}
		}
		return takenBankList;
	}

	/**  
	* 方法说明: 设置提款对象
	* @auth: xiongJinGang
	* @param takenBankCardVO
	* @param payBankBO
	* @param bankcard
	* @time: 2017年4月21日 上午11:21:37
	* @return: TakenBankCardVO 
	*/
	private TakenBankCardVO setTakenBankCardVO(TakenBankCardVO takenBankCardVO, PayBankBO payBankBO, PayBankcardBO bankcard) {
		takenBankCardVO.setBankCard(StringUtil.hideHeadString(bankcard.getCardcode()));
		takenBankCardVO.setFullBankCard(bankcard.getCardcode());// 先不存到里面
		takenBankCardVO.setBankCardId(bankcard.getId());
		takenBankCardVO.setBankId(bankcard.getBankid());
		takenBankCardVO.setBranchBankName(bankcard.getBankname());// 运行名称
		takenBankCardVO.setOpenBank(bankcard.getOpenbank());// 是否开通快捷支付
		takenBankCardVO.setBankType(bankcard.getBanktype());// 银行卡类型
		takenBankCardVO.setBankName(payBankBO.getName());// 银行名称
		takenBankCardVO.setbLogo(payBankBO.getbLogo());
		takenBankCardVO.setsLogo(payBankBO.getsLogo());
		return takenBankCardVO;
	}

	/**  
	* 方法说明: 根据验证类型获取验证方式
	* @auth: xiongJinGang
	* @param list
	* @param type
	* @time: 2017年4月18日 下午5:22:35
	* @return: TakenValidateTypeVO 
	*/
	private TakenValidateTypeVO getTakenValidateTypeFromRedis(List<TakenValidateTypeVO> list, Short type) {
		for (TakenValidateTypeVO takenValidateTypeVO : list) {
			if (takenValidateTypeVO.getType().equals(type)) {
				return takenValidateTypeVO;
			}
		}
		return null;
	}

	/**  
	* 方法说明: 获取用户提款次数
	* @auth: xiongJinGang
	* @param userId
	* @time: 2017年4月24日 上午10:06:53
	* @return: Integer 
	*/
	private Integer findUserTakenTimes(Integer userId) {
		String today = DateUtil.getNow(DateUtil.DATE_FORMAT);
		return transTakenMapper.getUserTakenTimes(userId, today);
	}

	/**  
	* 方法说明: 验证用户提款次数
	* @auth: xiongJinGang
	* @param userId
	* @time: 2017年4月24日 上午10:19:52
	* @return: ResultBO<?> 
	*/
	private ResultBO<?> findUserTakenTimesAndValidate(Integer userId) {
		return TakenUtil.validateTakenTimes(findUserTakenTimes(userId));
	}
}
