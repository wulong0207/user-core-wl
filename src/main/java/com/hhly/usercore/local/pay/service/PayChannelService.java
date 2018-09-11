package com.hhly.usercore.local.pay.service;


import java.util.List;

import com.hhly.skeleton.pay.channel.bo.PayChannelBO;
import com.hhly.skeleton.pay.channel.vo.PayChannelVO;

/**
 * @author lgs on
 * @version 1.0
 * @desc 支付渠道
 * @date 2017/3/22.
 * @company 益彩网络科技有限公司
 */
public interface PayChannelService {

	/**
	 * 根据条件查询
	 *
	 * @param payChannelVO 条件对象
	 * @return List<PayChannelPO> 结果集
	 */
	List<PayChannelBO> selectByCondition(PayChannelVO payChannelVO);

	/**  
	* 方法说明: 根据银行ID获取渠道信息（支持缓存）
	* @auth: xiongJinGang
	* @param bankId
	* @time: 2017年4月10日 上午9:53:03
	* @return: List<PayChannelBO> 
	*/
	List<PayChannelBO> findChannelByBankIdUseCache(Integer bankId);
}
