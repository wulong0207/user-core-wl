package com.hhly.usercore.local.bank.service;

import java.util.List;

import com.hhly.skeleton.pay.bo.PayBankBO;

/**
 * @desc 银行service接口
 * @author xiongJinGang
 * @date 2017年4月8日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface PayBankService {

	/**  
	* 方法说明: 从缓存中获取具体的银行信息
	* @auth: xiongJinGang
	* @param bankId
	* @time: 2017年4月21日 下午2:29:53
	* @return: PayBankBO 
	*/
	PayBankBO findBankFromCache(Integer bankId);

	/**  
	* 方法说明: 获取所有的银行卡信息
	* @auth: xiongJinGang
	* @time: 2017年4月21日 下午2:30:55
	* @return: List<PayBankBO> 
	*/
	List<PayBankBO> findAllBank();

	/**  
	* 方法说明: 根据银行ID查找银行信息
	* @auth: xiongJinGang
	* @param id
	* @time: 2017年5月5日 下午3:25:44
	* @return: PayBankBO 
	*/
	PayBankBO findBankById(Integer id);

	/**  
	* 方法说明: 根据支付类型获取银行
	* @auth: xiongJinGang
	* @param payType
	* @time: 2017年5月19日 下午8:01:10
	* @return: List<PayBankBO> 
	*/
	List<PayBankBO> findBankByType(Short payType);
}
