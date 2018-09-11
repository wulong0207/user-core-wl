
package com.hhly.usercore.agent.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.usercore.agent.service.AgentRegisterService;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.persistence.agent.dao.AgentInfoDaoMapper;
import com.hhly.usercore.persistence.agent.dao.AgentRebateConfigDaoMapper;
import com.hhly.usercore.persistence.agent.dao.AgentRebateDaoMapper;
import com.hhly.usercore.persistence.agent.po.AgentInfoPO;
import com.hhly.usercore.persistence.agent.po.AgentRebateConfigPO;
import com.hhly.usercore.persistence.agent.po.AgentRebatePO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年3月3日
 * @company 益彩网络科技公司
 * @version 1.0
 */

@Service
public class AgentRegisterServiceImpl implements AgentRegisterService {

	@Autowired
	UserUtil userUtil;
	
	@Autowired
	RedisUtil redisUtil;
	
	@Autowired
	PublicMethod publicMethod;
	
	@Autowired
	AgentInfoDaoMapper agentInfoDaoMapper;
	
	@Autowired
	AgentRebateDaoMapper agentRebateDaoMapper;
	
	@Autowired
	AgentRebateConfigDaoMapper agentRebateConfigDaoMapper;
	
	@Override
	public Object validate(JSONObject json) {
		String token = json.getString("token");
        if(ObjectUtil.isBlank(token)) {
            return ResultBO.err(MessageCodeConstants.PARAM_IS_FIELD);
        }		
		UserInfoBO tokenInfo = userUtil.getUserByToken(token);
        if(ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
		UserInfoBO resultBO = new UserInfoBO();
		resultBO.setMobile(tokenInfo.getMobile());
		resultBO.setMobileStatus(tokenInfo.getMobileStatus());
		resultBO.setIsMobileLogin(tokenInfo.getIsMobileLogin());
		resultBO.setRealName(tokenInfo.getRealName());
		resultBO.setIdCard(tokenInfo.getIdCard());
		
		//设置是否实名验证
		publicMethod.attestationStatus(resultBO, resultBO);
		
		return ResultBO.ok(resultBO);
	}



	@Override
	public Object register(JSONObject json) {
		String token = json.getString("token");
        if(ObjectUtil.isBlank(token)) {
            return ResultBO.err(MessageCodeConstants.PARAM_IS_FIELD);
        }	
		UserInfoBO tokenInfo = userUtil.getUserByToken(token);
        if(ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        Integer userId = userUtil.getUserIdByToken(token);
        int agentCount = agentInfoDaoMapper.findCountByUserId(userId);
        if(agentCount > 0)
        	return ResultBO.err(MessageCodeConstants.AGENT_INFO_EXIST);
        
        //创建代理信息

        AgentInfoPO po = new AgentInfoPO();
        po.setUserId(userId);
        po.setAgentCode(getAgentCode());
        
        //设置上级代理信息
        String parentAgentCode = tokenInfo.getAgentCode();
        if(!ObjectUtil.isBlank(parentAgentCode)){
            AgentInfoPO parentAgentPO = agentInfoDaoMapper.findByCode(parentAgentCode);
            po.setParentAgentId(parentAgentPO.getId());
            po.setAgentLevel((short) (parentAgentPO.getAgentLevel() + 1));
        }else{
        	po.setAgentLevel((short)1);
        }
        po.setAgentStatus((short)1);
        agentInfoDaoMapper.insert(po);
        
        //创建代理返点模板
        AgentRebatePO rebatePO = new AgentRebatePO();
        rebatePO.setAgentId(po.getId());
        rebatePO.setDefaultFlag((short)0);
        rebatePO.setCreateBy("system");
        agentRebateDaoMapper.insert(rebatePO);
        
        //创建代理模板到代理
        List<AgentRebateConfigPO> list = agentRebateConfigDaoMapper.findTemplate();
        if(!ObjectUtil.isBlank(list)){
            list.forEach(n -> {
            	n.setRebateId(rebatePO.getId());
            	n.setCreateBy("system");
            });
            agentRebateConfigDaoMapper.insertBatch(list);	
        }
        
		return ResultBO.ok(po);
	}
	
	private String getAgentCode(){
		String agentCode = EncryptUtil.getRandomString(8);
		int num = agentInfoDaoMapper.findCountByCode(agentCode);
		if(num > 0)
			agentCode = getAgentCode();
		return agentCode;
	}
}
