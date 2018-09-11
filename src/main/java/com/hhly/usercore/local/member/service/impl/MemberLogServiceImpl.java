
package com.hhly.usercore.local.member.service.impl;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.usercore.base.enums.MessageType.MemberOperateNode;
import com.hhly.usercore.base.rabbitmq.provider.MessageProvider;
import com.hhly.usercore.local.member.service.MemberLogService;
import com.hhly.usercore.local.member.service.MemberSecurityService;
import com.hhly.usercore.persistence.member.dao.UserInfoLastLogDaoMapper;
import com.hhly.usercore.persistence.member.dao.UserModifyLogDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoLastLogPO;
import com.hhly.usercore.persistence.member.po.UserModifyLogPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月25日
 * @company 益彩网络科技公司
 * @version 1.0
 */

@Service
public class MemberLogServiceImpl implements MemberLogService {
	
	private static final Logger log = Logger.getLogger(MemberLogServiceImpl.class);
	
	@Autowired
	MemberSecurityService memberSecurityService;
	
	@Autowired
	MessageProvider messageProvider;

	@Autowired
	UserModifyLogDaoMapper userModifyLogDaoMapper;
	
	@Autowired
	UserInfoLastLogDaoMapper userInfoLastLogDaoMapper;
	
	
	
	@Override
	public void insertModifyLog(UserModifyLogPO po) {
		po.setProvince(memberSecurityService.findAreaByIp(po.getUserIp()));
		UserInfoLastLogPO lastLogPO = userInfoLastLogDaoMapper.findLastLogByUserId(po.getUserId());
        if(!ObjectUtil.isBlank(lastLogPO)&&po.getUserIp()!=null&&lastLogPO.getIp()!=null && !Objects.equals(lastLogPO.getIp(), po.getUserIp())){
        	log.info("发送ip跟换短信：备注："+po.getRemark()+",历史ip:"+lastLogPO.getIp()+",最新ip:"+po.getUserIp());
        	messageProvider.sendMemberNode(po.getUserId(), MemberOperateNode.register.getNodeId());
        }		
        userModifyLogDaoMapper.insert(po);		
	}

}
