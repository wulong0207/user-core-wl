
package com.hhly.usercore.persistence.agent.dao;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.hhly.skeleton.base.util.EncryptUtil;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年3月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
//defaultRollback = true说明: true ? 测试数据不会污染数据库 : 会真正添加到数据库当中
@TransactionConfiguration(transactionManager = "transactionManager" , defaultRollback = true) 
public class AgentInfoDaoMapperTest {

	@Autowired
	AgentInfoDaoMapper agentInfoDaoMapper;
	
	@Test
	public void testInsert() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindByCode() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindCountByUserId() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindCountByCode() {
		fail("Not yet implemented");
	}
	
	@Test
	public void test(){
		getAgentCode();
	}
	public static int i = 0;
	public String getAgentCode(){
		String agentCode = EncryptUtil.getRandomString(8);
		System.err.println(agentCode);
		int num = agentInfoDaoMapper.findCountByCode(agentCode);
		if(i == 3){
			return agentCode;
		}
		if(num == 0){
			i++;
			agentCode = getAgentCode();
		}
		return agentCode;
	}

}
