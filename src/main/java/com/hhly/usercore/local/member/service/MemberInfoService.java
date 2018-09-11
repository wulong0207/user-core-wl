
package com.hhly.usercore.local.member.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import com.hhly.usercore.persistence.member.po.UserInfoPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月8日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface MemberInfoService {

	/**
	 * 渠道查询会员简单详情
	 * @return
	 * @date 2018年4月8日下午6:13:47
	 * @author cheng.chen
	 */
	UserInfoPO findSimpleInfoByChannel(String channelId, String openId, Short orderNum);
	
	/**
	 * 用户id查询会员简单详情
	 * @param memberId
	 * @return
	 * @date 2018年4月25日下午6:10:49
	 * @author cheng.chen
	 */
	UserInfoPO findSimpleInfoById(Integer memberId);

	/**
	 * 通过渠道openId获取本站用户id
	 * @param vo
	 * @return
	 */
	int getUserIdByChannelOpenId(OrderChannelVO vo);
}
