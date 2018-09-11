
package com.hhly.usercore.local.member.service.impl;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.exception.Assert;
import com.hhly.usercore.local.channel.service.ChannelInfoService;
import com.hhly.usercore.local.member.service.MemberInfoService;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月8日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("memberInfoService")
public class MemberInfoServiceImpl implements MemberInfoService {
	
	@Autowired
	private UserInfoDaoMapper userInfoDaoMapper;
	
	@Autowired
	private ChannelInfoService channelInfoService;
	

	@Override
	public UserInfoPO findSimpleInfoByChannel(String channelId, String openId, Short orderNum) {
		
    	Assert.paramNotNull(channelId, "channelId");
    	Assert.paramNotNull(openId, "openId");
		Assert.paramNotNull(orderNum, "orderNum");
        
        String channelOpenID = channelInfoService.getChannelOpenID(channelId, openId, orderNum);

        UserInfoPO po = new UserInfoPO();
        po.setChannelOpenID(channelOpenID);		
		
		return findSimpleInfo(po);
	}
	
	@Override
	public UserInfoPO findSimpleInfoById(Integer memberId) {
		UserInfoPO po = new UserInfoPO();
        po.setId(memberId);
		return findSimpleInfo(po);
	}


	@Override
	public int getUserIdByChannelOpenId(OrderChannelVO vo) {
		String channelOpenId = vo.getChannelId()+"_"+vo.getMemberId();
		int userId = userInfoDaoMapper.getUserIdByChannelOpenId(channelOpenId);
		return userId;
	}

	private UserInfoPO findSimpleInfo(UserInfoPO po){
		return userInfoDaoMapper.findSimpleUserInfo(po);
	}

}
