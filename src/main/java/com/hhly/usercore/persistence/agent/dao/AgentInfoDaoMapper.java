package com.hhly.usercore.persistence.agent.dao;

import com.hhly.usercore.persistence.agent.po.AgentInfoPO;

public interface AgentInfoDaoMapper {
	
    int insert(AgentInfoPO po);
    
    AgentInfoPO findByUserId(Integer userId);    
    
    AgentInfoPO findByCode(String agentCode);
    
    int findCountByUserId(Integer id);

    int findCountByCode(String agentCode);
}