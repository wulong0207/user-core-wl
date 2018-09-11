package com.hhly.usercore.persistence.agent.dao;

import java.util.List;

import com.hhly.usercore.persistence.agent.po.AgentRebateConfigPO;

public interface AgentRebateConfigDaoMapper {
	
	List<AgentRebateConfigPO> findTemplate();
	
	void insertBatch(List<AgentRebateConfigPO> list);
}