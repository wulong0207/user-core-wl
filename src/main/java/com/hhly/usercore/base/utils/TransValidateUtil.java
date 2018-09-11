package com.hhly.usercore.base.utils;

import java.util.Date;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.PayConstants;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.pay.vo.TransParamVO;
import com.hhly.skeleton.pay.vo.TransRechargeVO;
import com.hhly.skeleton.pay.vo.TransTakenVO;
import com.hhly.skeleton.pay.vo.TransUserVO;

/**
 * @desc 交易接口通用查询参数验证
 * @author xiongjingang
 * @date 2017年3月7日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public class TransValidateUtil {

	/**  
	* 方法说明: 验证查询日期
	* @param transParamVO
	* @time: 2017年3月7日 上午11:05:29
	* @return: ResultBO<?> 
	*/
	public ResultBO<?> validateQueryDate(TransParamVO transParamVO) {
		// 时间比较
		Date startDate = transParamVO.getStartDate();
		Date endDate = transParamVO.getEndDate();

		if (ObjectUtil.isBlank(startDate)) {
			return ResultBO.err(MessageCodeConstants.TRANS_QUERY_STARTDATE_ERROR_SERVICE);
		} else {
			String startDateStr = DateUtil.convertDateToStr(startDate, DateUtil.DATE_FORMAT);
			startDateStr += " 00:00:00";
			startDate = DateUtil.convertStrToDate(startDateStr, DateUtil.DATE_FORMAT);
		}
		// 结束时间为空，默认当前时间
		if (ObjectUtil.isBlank(endDate)) {
			String endDateStr = DateUtil.getNow();
			endDate = DateUtil.convertStrToDate(endDateStr);
		} else {
			String endDateStr = DateUtil.convertDateToStr(endDate, DateUtil.DATE_FORMAT);
			endDateStr += " 23:59:59";
			endDate = DateUtil.convertStrToDate(endDateStr, DateUtil.DATE_FORMAT);
		}
		// 比较查询日期大小
		int num = DateUtil.compare(startDate, endDate);
		if (num >= 1) {
			return ResultBO.err(MessageCodeConstants.TRANS_QUERY_DATE_ERROR_SERVICE);
		}
		transParamVO.setStartDate(startDate);
		transParamVO.setEndDate(endDate);
		return ResultBO.ok(transParamVO);
	}

	/**  
	* 方法说明: 验证通用参数
	* @param validateService
	* @param transParamVO
	* @time: 2017年3月7日 下午2:28:57
	* @return: ResultBO<?> 
	*/
	public ResultBO<?> validateCommonParam(TransParamVO transParamVO) {
		ResultBO<?> dateResult = validateQueryDate(transParamVO);
		if (dateResult.isError()) {
			return dateResult;
		}
		transParamVO = (TransParamVO) dateResult.getData();
		Short moneyFlow = transParamVO.getMoneyFlow();
		if (!PayConstants.MoneyFlowEnum.containsKey(moneyFlow)) {
			transParamVO.setMoneyFlow(null);
		}
		return ResultBO.ok(transParamVO);
	}

	/**  
	* 方法说明: 验证提款记录参数
	* @param transTaken
	* @time: 2017年3月9日 下午6:10:00
	* @return: ResultBO<?> 
	*/
	public ResultBO<?> validateAddTaken(TransTakenVO transTaken) {
		if (ObjectUtil.isBlank(transTaken.getPayChannel())) {
			return ResultBO.err(MessageCodeConstants.TRANS_PAY_CHANNEL_IS_NULL_FIELD);
		}
		if (ObjectUtil.isBlank(transTaken.getTakenBank())) {
			return ResultBO.err(MessageCodeConstants.TRANS_TAKEN_BANK_IS_NULL_FIELD);
		}
		if (ObjectUtil.isBlank(transTaken.getTakenPlatform())) {
			return ResultBO.err(MessageCodeConstants.TRANS_TAKEN_PLATFORM_IS_NULL_FIELD);
		}
		if (ObjectUtil.isBlank(transTaken.getBankCardNum())) {
			return ResultBO.err(MessageCodeConstants.CARD_CODE_IS_NULL_FIELD);
		}
		if (ObjectUtil.isBlank(transTaken.getExtractAmount())) {
			return ResultBO.err(MessageCodeConstants.TRANS_AMOUNT_IS_NULL_FIELD);
		}
		return ResultBO.ok();
	}

	/**  
	* 方法说明: 验证充值参数
	* @param transRecharge
	* @time: 2017年3月10日 上午10:53:40
	* @return: ResultBO<?> 
	*/
	public ResultBO<?> validateAddRecharge(TransRechargeVO transRecharge) {
		Short rechargeChannel = transRecharge.getRechargeChannel();
		if (ObjectUtil.isBlank(rechargeChannel)) {
			return ResultBO.err(MessageCodeConstants.TRANS_RECHARGE_CHANNEL_IS_NULL_FIELD);
		} else {
			if (!PayConstants.PayChannelEnum.containsKey(rechargeChannel)) {
				return ResultBO.err(MessageCodeConstants.TRANS_PAY_CHANNEL_IS_ERROR_SERVICE);
			}
		}
		if (ObjectUtil.isBlank(transRecharge.getPayType())) {
			return ResultBO.err(MessageCodeConstants.TRANS_PAY_TYPE_IS_NULL_FIELD);
		}
		return ResultBO.ok();
	}

	/**  
	* 方法说明: 验证查询基本参数
	* @auth: xiongJinGang
	* @param transUserVO
	* @time: 2017年4月5日 下午4:48:26
	* @return: ResultBO<?> 
	*/
	public ResultBO<?> validateUserTransRecordByOrderCode(TransUserVO transUserVO) {
		// 订单编号为空
		if (ObjectUtil.isBlank(transUserVO.getOrderCode())) {
			return ResultBO.err(MessageCodeConstants.ORDER_CODE_IS_NULL_FIELD);
		}
		Short transType = transUserVO.getTransType();
		if (!ObjectUtil.isBlank(transType)) {
			if (!PayConstants.TransTypeEnum.containsKey(transType)) {
				return ResultBO.err(MessageCodeConstants.PAY_TRADE_TYPE_ERROR_SERVICE);
			}
		}
		return ResultBO.ok();
	}
}
