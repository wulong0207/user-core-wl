package com.hhly.usercore.persistence.pay.dao;

import com.hhly.skeleton.pay.bo.PayBankLimitBO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PayBankLimitDaoMapper {

	List<PayBankLimitBO> selectAll();

	/**  
	* 方法说明: 根据银行ID获取限额
	* @auth: xiongJinGang
	* @param bankId 银行ID
	* @param bankType 银行卡类型。1储蓄卡 2信用卡
	* @time: 2017年4月7日 上午11:03:19
	* @return: PayBankLimitBO 
	*/
	PayBankLimitBO getPayBankLimitByBankId(@Param("bankId") Integer bankId, @Param("bankType") Short bankType);

}