
package com.hhly.usercore.local.channel.service.impl;

import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.constants.SymbolConstants;
import com.hhly.skeleton.base.exception.Assert;
import com.hhly.skeleton.base.exception.ServiceRuntimeException;
import com.hhly.skeleton.base.util.Md5Util;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.usercore.local.channel.service.ChannelInfoService;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.operate.dao.OperateMarketChannelDaoMapper;
import com.hhly.usercore.persistence.operate.po.OperateMarketChannelPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月20日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("channelInfoService")
public class ChannelInfoServiceImpl implements ChannelInfoService {
	
	private final static Logger log = Logger.getLogger(ChannelInfoServiceImpl.class);
	
	@Autowired
	OperateMarketChannelDaoMapper operateMarketChannelDaoMapper;
	
	@Autowired
	UserInfoDaoMapper userInfoDaoMapper;

	@Override
	public String getChannelOpenID(String channelId, String openId, Short orderNum) {
    	Assert.paramNotNull(channelId, "channelId");
    	Assert.paramNotNull(openId, "openId");
    	Assert.paramNotNull(orderNum, "orderNum");
        
        OperateMarketChannelPO channelPO = operateMarketChannelDaoMapper.findByChannelId(channelId);
        if(ObjectUtil.isBlank(channelPO))
        	throw new ServiceRuntimeException("渠道查询为null, 请联系研发人员");
        
        Short num;
        if(orderNum == 1)
        	num = (short) (channelPO.getGrade() - 1);
        else
        	num = channelPO.getGrade();
        String topChannelId = operateMarketChannelDaoMapper.findTopChannelId(channelId, num);
        if(ObjectUtil.isBlank(topChannelId))
        	throw new ServiceRuntimeException("topChannelId : " + topChannelId + ", 通过渠道id查询不到顶级渠道id");
        
        String channelOpenID = topChannelId + SymbolConstants.UNDERLINE + openId;
        String md5ChannelOpenId = Md5Util.md5_32(channelOpenID);
        log.info("执行查询MD5 渠道登录账号明文 : " + channelOpenID + ", 密文 : " + md5ChannelOpenId);
        UserInfoPO paramPO = new UserInfoPO();
        paramPO.setChannelOpenID(md5ChannelOpenId);
        UserInfoPO resultPO = userInfoDaoMapper.findSimpleUserInfo(paramPO);
        if(!ObjectUtil.isBlank(resultPO)){
        	paramPO.setId(resultPO.getId());
        	paramPO.setChannelOpenID(channelOpenID);
        	int flag = userInfoDaoMapper.updateUserInfo(paramPO);
        	log.info("执行修改密文为明文操作 : " + flag);
        }
		return channelOpenID;
	}

	/**
	 * @desc 获取最上级渠道id
	 * @date 2018.6.12
	 * @author zhouyang
	 * @param vo
	 * @return
	 */
	@Override
	public String getChannelTopId(OrderChannelVO vo) {
		OperateMarketChannelPO channelPO = operateMarketChannelDaoMapper.findByChannelId(vo.getChannelId());
		if(ObjectUtil.isBlank(channelPO)) {
			throw new ServiceRuntimeException("渠道查询为null, 请联系研发人员");
		}
		Short num = (short) (channelPO.getGrade() - 1);
		String topChannelId = operateMarketChannelDaoMapper.findTopChannelId(vo.getChannelId(), num);
		return topChannelId;
	}

}
