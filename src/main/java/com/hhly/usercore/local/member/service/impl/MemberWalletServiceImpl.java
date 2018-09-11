
package com.hhly.usercore.local.member.service.impl;

import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import com.hhly.skeleton.user.bo.ChannelMemberWalletBO;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.MathUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.pay.vo.TakenUserWalletVO;
import com.hhly.usercore.local.member.service.MemberWalletService;
import com.hhly.usercore.persistence.member.dao.UserWalletDaoMapper;
import com.hhly.usercore.persistence.member.po.UserWalletPO;

import java.util.List;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月19日
 * @company 益彩网络科技公司
 * @version 1.0
 */

@Service("memberWalletService")
public class MemberWalletServiceImpl implements MemberWalletService {
	
	private static final Logger log = Logger.getLogger(MemberWalletServiceImpl.class);
	
	@Autowired
	UserWalletDaoMapper userWalletDaoMapper;

	@Override
	public Double findTotalByUserId(Integer userId) {
		Double totalMoney = 0d;
		UserWalletPO po = findInfo(userId);
		if(!ObjectUtil.isBlank(po))
			totalMoney = MathUtil.add(po.getTotalCashBalance(), po.getEffRedBalance());
		return totalMoney;
	}
	
	@Override
	public TakenUserWalletVO findWalletByUserId(Integer userId) {
		TakenUserWalletVO vo = null;
		UserWalletPO UserWalletPO = userWalletDaoMapper.findByUserId(userId);
		if (!ObjectUtil.isBlank(UserWalletPO)) {
			vo = new TakenUserWalletVO();
			try {
				BeanUtils.copyProperties(vo, UserWalletPO);
			} catch (Exception e) {
				log.error("");
			} 
			vo.setTotalAmount(UserWalletPO.getTotalCashBalance());// 账户总余额
		}
		return vo;
	}

	@Override
	public List<ChannelMemberWalletBO> queryChannelMemberWalletList(OrderChannelVO vo) {
		List<ChannelMemberWalletBO> wList = userWalletDaoMapper.queryChannelMemberWalletList(vo);
		if (!ObjectUtil.isBlank(wList)) {
			for (ChannelMemberWalletBO walletBO : wList) {
				if (!ObjectUtil.isBlank(walletBO.getChannelMemberId()) && walletBO.getChannelMemberId().contains("_")) {
					String memberId = walletBO.getChannelMemberId().split("_")[1].toString().trim();
					walletBO.setChannelMemberId(memberId);
				}
			}
		}
		return wList;
	}

	@Override
	public void initWallet(Integer userId) {
		UserWalletPO userWalletPO = new UserWalletPO(userId, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO,
				UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.IS_TRUE, 1);
		userWalletDaoMapper.insert(userWalletPO);	
	}



	private UserWalletPO findInfo(Integer userId){
		return userWalletDaoMapper.findByUserId(userId);
	}
}
