
package com.hhly.usercore.local.channel.service;

import com.hhly.skeleton.task.order.vo.OrderChannelVO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月20日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface ChannelInfoService {

	String getChannelOpenID(String channelId, String openId, Short orderNum);

	/**
	 * @desc 获取最上级渠道id
	 * @date 2018.6.12
	 * @author zhouyang
	 * @param vo
	 * @return
	 */
	String getChannelTopId(OrderChannelVO vo);
}
