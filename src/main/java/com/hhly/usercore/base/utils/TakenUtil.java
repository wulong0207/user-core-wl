package com.hhly.usercore.base.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.Constants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.PayConstants;
import com.hhly.skeleton.base.constants.PayConstants.TakenValidateTypeEnum;
import com.hhly.skeleton.base.util.MathUtil;
import com.hhly.skeleton.base.util.NumberUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.pay.bo.TransRechargeBO;
import com.hhly.skeleton.pay.vo.TakenAmountInfoVO;
import com.hhly.skeleton.pay.vo.TakenConfirmVO;
import com.hhly.skeleton.pay.vo.TakenRealAmountVO;
import com.hhly.skeleton.pay.vo.TakenRechargeCountVO;
import com.hhly.skeleton.pay.vo.TakenReqParamVO;
import com.hhly.skeleton.pay.vo.TakenValidateTypeVO;

public class TakenUtil {

	/**  
	* 方法说明: 验证提款参数
	* @auth: xiongJinGang
	* @param takenValidateTypeVO
	* @param takenValidateTypeEnum
	* @time: 2017年4月18日 下午4:58:05
	* @return: ResultBO<?> 
	*/
	public static ResultBO<?> validateTakenReq(TakenValidateTypeVO takenValidateTypeVO, TakenValidateTypeEnum takenValidateTypeEnum) {
		if (ObjectUtil.isBlank(takenValidateTypeEnum)) {
			return ResultBO.err(MessageCodeConstants.TAKEN_VALIDATE_TYPE_ERROR_SERVICE);
		}
		String validateStr = takenValidateTypeVO.getValidateStr();
		switch (takenValidateTypeEnum) {
		case EMAIL:
			if (ObjectUtil.isBlank(validateStr)) {
				return ResultBO.err(MessageCodeConstants.TAKEN_MOBILE_VALIDATE_CODE_IS_NULL_FIELD);
			}
			break;
		case IDCARD:
			if (ObjectUtil.isBlank(validateStr)) {
				return ResultBO.err(MessageCodeConstants.TAKEN_IDCARD_VALIDATE_CODE_IS_NULL_FIELD);
			}
			// 验证是否是8位
			if (validateStr.length() != Constants.VALIDATE_IDCARD_BANCARD_END_LENGTH) {
				return ResultBO.err(MessageCodeConstants.TAKEN_IDCARD_VALIDATE_CODE_ERROR_SERVICE);
			}
			break;
		case BANKCARD:
			if (ObjectUtil.isBlank(validateStr)) {
				return ResultBO.err(MessageCodeConstants.TAKEN_BANKCARD_VALIDATE_CODE_IS_NULL_FIELD);
			}
			// 验证是否是8位
			if (validateStr.length() != Constants.VALIDATE_IDCARD_BANCARD_END_LENGTH) {
				return ResultBO.err(MessageCodeConstants.TAKEN_BANKCARD_VALIDATE_CODE_ERROR_SERVICE);
			}
			break;
		case MOBILE:
			if (ObjectUtil.isBlank(validateStr)) {
				return ResultBO.err(MessageCodeConstants.TAKEN_MOBILE_VALIDATE_CODE_IS_NULL_FIELD);
			}
			break;
		default:
			break;
		}
		return ResultBO.ok();
	}

	/**  
	* 方法说明: 验证提款参数
	* @auth: xiongJinGang
	* @param takenReqParamVO
	* @time: 2017年4月19日 上午11:06:14
	* @return: ResultBO<?> 
	*/
	public static ResultBO<?> validateTakenParam(TakenReqParamVO takenReqParamVO, boolean validateTakenIds) {
		if (ObjectUtil.isBlank(takenReqParamVO.getBankCardId())) {
			return ResultBO.err(MessageCodeConstants.PAY_BANK_CARD_ID_IS_NULL_FIELD);
		}
		if (ObjectUtil.isBlank(takenReqParamVO.getTakenAmount())) {
			return ResultBO.err(MessageCodeConstants.TAKEN_AMOUNT_IS_NULL_FIELD);
		}
		/*if (ObjectUtil.isBlank(takenReqParamVO.getTakenToken())) {
			return ResultBO.err(MessageCodeConstants.TAKEN_CONFIRM_TIME_OUT_ERROR_SERVICE);
		}*/
		if (validateTakenIds) {
			if (ObjectUtil.isBlank(takenReqParamVO.getTakenIds())) {
				return ResultBO.err(MessageCodeConstants.TAKEN_CONFIRM_IS_NULL_FIELD);
			}
		} else {
			if (ObjectUtil.isBlank(takenReqParamVO.getBankName())) {
				return ResultBO.err(MessageCodeConstants.BANK_NAME_IS_NULL_FIELD);
			}
		}
		return ResultBO.ok();
	}

	/**  
	* 方法说明: 计算充值金额（按20%计算，充值20%的金额大于钱包中的剩余20%的金额）
	* @auth: xiongJinGang
	* @param top20BalanceInWallet
	* @time: 2017年4月20日 上午10:40:06
	* @return: TakenRechargeCountVO 
	*/
	public static TakenRechargeCountVO countRecharge(List<TransRechargeBO> list, Double top20BalanceInWallet, TakenRechargeCountVO takenRechargeCountVO) {
		if (!ObjectUtil.isBlank(list)) {
			List<TransRechargeBO> needBackList = takenRechargeCountVO.getList();
			if (ObjectUtil.isBlank(needBackList)) {
				needBackList = new ArrayList<TransRechargeBO>();
			}
			// 总的20%金额
			Double totalTop20Balance = takenRechargeCountVO.getTotalTop20Balance();
			// 总的充值金额
			Double totalBalance = takenRechargeCountVO.getTotalBalance();
			boolean isBiger = false;// 是否大于当前钱包中20%的余额
			for (TransRechargeBO transRechargeBO : list) {
				// 充值到钱包中的金额
				Double arrivalAmount = transRechargeBO.getArrivalAmount();
				// 充值时，如果用了充值红包，那们除掉购彩后的剩余金额就存在这个inWallet中了
				if (!ObjectUtil.isBlank(transRechargeBO.getInWallet())) {
					arrivalAmount = transRechargeBO.getInWallet();
				}
				if (!ObjectUtil.isBlank(arrivalAmount)) {
					Double top20Balance = MathUtil.mul(arrivalAmount, Constants.USER_WALLET_TWENTY_PERCENT);// 拆分成20%的金额
					totalTop20Balance = MathUtil.add(totalTop20Balance, top20Balance);
					totalBalance = MathUtil.add(totalBalance, arrivalAmount);// 充值总金额
					needBackList.add(transRechargeBO);
					if (MathUtil.compareTo(totalTop20Balance, top20BalanceInWallet) >= 0) {
						isBiger = true;
						break;
					}
				}
			}
			takenRechargeCountVO.setTotalBalance(totalBalance);
			takenRechargeCountVO.setTotalTop20Balance(totalTop20Balance);
			takenRechargeCountVO.setBigger(isBiger);
			takenRechargeCountVO.setList(needBackList);
		}
		return takenRechargeCountVO;
	}

	/**  
	* 方法说明: 提款请求
	* @auth: xiongJinGang
	* @param takenReqParamVO
	* @time: 2017年4月25日 上午10:13:34
	* @return: String 
	*/
	public static String makeTakenKey(TakenReqParamVO takenReqParamVO) {
		return new StringBuffer(takenReqParamVO.getToken()).append("_").append(takenReqParamVO.getBankCardId()).append("_").append(takenReqParamVO.getTakenAmount()).toString();
	}

	/**  
	* 方法说明: 验证用户的提款次数
	* @auth: xiongJinGang
	* @param takenTimes
	* @time: 2017年4月24日 上午10:18:41
	* @return: ResultBO<?> 
	*/
	public static ResultBO<?> validateTakenTimes(Integer takenTimes) {
		if (!ObjectUtil.isBlank(takenTimes)) {
			if (takenTimes >= Constants.TAKEN_TIMES_FOR_ONE_DAY) {
				return ResultBO.err(MessageCodeConstants.TAKEN_TIMES_ERROR_SERVICE);
			}
		}
		return ResultBO.ok();
	}

	/**  
	* 方法说明: 验证提款信息
	* @auth: xiongJinGang
	* @param takenIds
	* @time: 2017年4月20日 下午6:20:41
	* @return: ResultBO<?> 
	*/

	private static ResultBO<?> validateTakenIds(String takenIds) {
		takenIds = StringUtil.replaceSign(takenIds);
		Map<Integer, Integer> takenIdMap = new HashMap<Integer, Integer>();
		String[] ids = takenIds.split(",");
		for (String id : ids) {
			id = StringUtil.trimSpace(id);
			if (!NumberUtil.isDigits(id)) {
				return ResultBO.err(MessageCodeConstants.TAKEN_CONFIRM_ERROR_SERVICE);
			}
			Integer idInt = Integer.parseInt(id);
			takenIdMap.put(idInt, idInt);
		}
		if (ObjectUtil.isBlank(takenIdMap)) {
			return ResultBO.err(MessageCodeConstants.TAKEN_CONFIRM_ERROR_SERVICE);
		}
		return ResultBO.ok(takenIdMap);
	}

	/**  
	* 方法说明: 获取实际的提款信息
	* @auth: xiongJinGang
	* @param takenReqParamVO
	* @param takenConfirmVO
	* @time: 2017年4月25日 上午9:52:49
	* @return: ResultBO<?> 
	 * @throws ParseException 
	*/
	@SuppressWarnings({ "unchecked" })
	public static ResultBO<?> getRealTaken(TakenReqParamVO takenReqParamVO, TakenConfirmVO takenConfirmVO) throws ParseException {
		// 获取需要提现的信息
		String takenIds = takenReqParamVO.getTakenIds();
		List<TakenAmountInfoVO> takenList = takenConfirmVO.getList();
		ResultBO<?> resultBO = validateTakenIds(takenIds);
		if (resultBO.isError()) {
			return resultBO;
		}
		List<TakenAmountInfoVO> needTakenList = new ArrayList<TakenAmountInfoVO>();
		Map<Integer, Integer> takenIdMap = (Map<Integer, Integer>) resultBO.getData();
		if (takenIdMap.size() > takenList.size()) {
			return ResultBO.err(MessageCodeConstants.TAKEN_CONFIRM_ERROR_SERVICE);
		}

		for (Map.Entry<Integer, Integer> takenId : takenIdMap.entrySet()) {
			boolean flag = false;
			for (TakenAmountInfoVO takenAmountInfoVO : takenList) {
				if (takenId.getKey().equals(takenAmountInfoVO.getTakenId())) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				// 传过来的takenID不存在，返回错误
				return ResultBO.err(MessageCodeConstants.TAKEN_CONFIRM_ERROR_SERVICE);
			}
		}
		Double totalTakenAmount = 0.0;
		for (TakenAmountInfoVO takenAmountInfoVO : takenList) {
			// 存在正常提款请求的，不能取消提款
			if (takenAmountInfoVO.getStatus().equals(PayConstants.TakenAmountStatusEnum.NORMAL.getKey())) {
				if (!takenIdMap.containsKey(takenAmountInfoVO.getTakenId())) {
					return ResultBO.err(MessageCodeConstants.TAKEN_CONFIRM_ERROR_SERVICE);
				}
				totalTakenAmount = MathUtil.add(MathUtil.formatAmountToDouble(takenAmountInfoVO.getTakenAmount()), totalTakenAmount);
				needTakenList.add(takenAmountInfoVO);
			} else {
				if (takenIdMap.containsKey(takenAmountInfoVO.getTakenId())) {
					totalTakenAmount = MathUtil.add(MathUtil.formatAmountToDouble(takenAmountInfoVO.getTakenAmount()), totalTakenAmount);
					needTakenList.add(takenAmountInfoVO);
				}
			}
		}
		if (ObjectUtil.isBlank(needTakenList)) {
			return ResultBO.err(MessageCodeConstants.TAKEN_CONFIRM_ERROR_SERVICE);
		}
		TakenRealAmountVO takenRealAmountVO = new TakenRealAmountVO(totalTakenAmount, needTakenList);
		return ResultBO.ok(takenRealAmountVO);
	}

	/**  
	* 方法说明: 生成提款token键
	* @auth: xiongJinGang
	* @param token
	* @time: 2017年4月25日 上午10:58:51
	* @return: String 
	*/
	public static String makeTakenTokenKey(String token) {
		return new StringBuffer(CacheConstants.P_CORE_USER_TAKEN_TOKEN).append(token).toString();
	}
}
