package com.hhly.usercore.local.bank.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.PayConstants;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.pay.bo.PayBankBO;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.local.bank.service.PayBankService;
import com.hhly.usercore.persistence.pay.dao.PayBankDaoMapper;

/**
 * @desc 银行实现层
 * @author xiongJinGang
 * @date 2017年4月8日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("payBankService")
public class PayBankServiceImpl implements PayBankService {
	@Resource
	private PayBankDaoMapper payBankDaoMapper;
	@Resource
	private RedisUtil redisUtil;
	@Value("${before_file_url}")
	private String before_file_url;// 图片路径

	@Override
	public PayBankBO findBankById(Integer id) {
		return payBankDaoMapper.getBankById(id);
	}

	@Override
	public PayBankBO findBankFromCache(Integer bankId) {
		List<PayBankBO> list = findAllBankFromCache();
		if (!ObjectUtil.isBlank(list)) {
			for (PayBankBO payBankBO : list) {
				if (payBankBO.getId().equals(bankId)) {
					return payBankBO;
				}
			}
		}
		return null;
	}

	/**  
	* 方法说明: 获取所有银行列表，存到缓存中
	* @auth: xiongJinGang
	* @time: 2017年4月20日 下午2:53:39
	* @return: List<PayBankBO> 
	*/
	private List<PayBankBO> findAllBankFromCache() {
		String key = CacheConstants.P_CORE_PAY_BANK_LIST;
		List<PayBankBO> list = redisUtil.getObj(key, new ArrayList<PayBankBO>());
		if (ObjectUtil.isBlank(list)) {
			list = payBankDaoMapper.getAll();
			if (!ObjectUtil.isBlank(list)) {
				redisUtil.addObj(key, list, CacheConstants.ONE_DAY);
			}
		}
		return list;
	}

	@Override
	public List<PayBankBO> findAllBank() {
		List<PayBankBO> list = payBankDaoMapper.getAll();
		if (!ObjectUtil.isBlank(list)) {
			for (PayBankBO payBankBO : list) {
				String bLogo = payBankBO.getbLogo();
				if (!ObjectUtil.isBlank(bLogo) && !bLogo.startsWith("http://")) {
					payBankBO.setbLogo(before_file_url + bLogo);
				}
				String sLogo = payBankBO.getsLogo();
				if (!ObjectUtil.isBlank(sLogo) && !sLogo.startsWith("http://")) {
					payBankBO.setsLogo(before_file_url + sLogo);
				}
			}
		}
		return list;
	}

	@Override
	public List<PayBankBO> findBankByType(Short payType) {
		List<PayBankBO> list = findAllBankFromCache();
		for (int i = list.size() - 1; i >= 0; i--) { // 倒序
			// 银行类型不匹配或者状态是信用，都不要
			if (!list.get(i).getPayType().equals(payType) || list.get(i).getStatus().equals(PayConstants.BankStatusEnum.DISABLE.getKey())) {
				list.remove(i);
			}
		}
		return list;
	}
}
