package com.hhly.usercore.local.pay.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.pay.channel.bo.PayChannelBO;
import com.hhly.skeleton.pay.channel.vo.PayChannelVO;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.local.pay.service.PayChannelService;
import com.hhly.usercore.persistence.pay.dao.PayChannelDaoMapper;

/**
 * @author lgs on
 * @version 1.0
 * @desc
 * @date 2017/3/22.
 * @company 益彩网络科技有限公司
 */
@Service
public class PayChannelServiceImpl implements PayChannelService {

	@Autowired
	private PayChannelDaoMapper payChannelDaoMapper;
	@Resource
	private RedisUtil redisUtil;

	/**
	 * 根据条件查询
	 *
	 * @param payChannelVO 条件对象
	 * @return List<PayChannelPO> 结果集
	 */
	@Override
	public List<PayChannelBO> selectByCondition(PayChannelVO payChannelVO) {
		return payChannelDaoMapper.selectByCondition(payChannelVO);
	}

	@Override
	public List<PayChannelBO> findChannelByBankIdUseCache(Integer bankId) {
		String key = CacheConstants.P_CORE_PAY_BANK_CHANNEL_SINGLE + bankId;
		List<PayChannelBO> list = redisUtil.getObj(key, new ArrayList<PayChannelBO>());
		if (ObjectUtil.isBlank(list)) {
			//已经根据order_id进行升序排序了
			list = payChannelDaoMapper.getChannelByBankId(bankId);
			redisUtil.addObj(key, list, CacheConstants.ONE_DAY);// 存一天
		}
		return list;
		// return payChannelDaoMapper.getChannelByBankId(bankId);
	}
}
