
package com.hhly.usercore.local.member.service;

import com.hhly.skeleton.pay.vo.TakenUserWalletVO;
import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import com.hhly.skeleton.user.bo.ChannelMemberWalletBO;

import java.util.List;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月19日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface MemberWalletService {

	Double findTotalByUserId(Integer userId);
	
	void initWallet(Integer userId);

	TakenUserWalletVO findWalletByUserId(Integer userId);

	/**
	 * 查询用户渠道钱包列表
	 * @date 2018.6.8
	 * @author zhouyang
	 * @param vo
	 * @return
	 */
	List<ChannelMemberWalletBO> queryChannelMemberWalletList(OrderChannelVO vo);
}
