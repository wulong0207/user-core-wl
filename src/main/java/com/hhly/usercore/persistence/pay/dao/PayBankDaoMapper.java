package com.hhly.usercore.persistence.pay.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.hhly.skeleton.pay.bo.PayBankBO;

public interface PayBankDaoMapper {
	/**  
	* 方法说明:获取所有银行信息 
	* @auth: xiongJinGang
	* @time: 2017年4月8日 下午4:11:05
	* @return: List<PayBankBO> 
	*/
	List<PayBankBO> getAll();

	/**  
	* 方法说明: 根据银行ID获取银行信息
	* @auth: xiongJinGang
	* @param id
	* @time: 2017年4月8日 下午4:10:09
	* @return: PayBankBO 
	*/
	PayBankBO getBankById(@Param("id") Integer id);
}