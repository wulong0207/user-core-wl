
package com.hhly.usercore.persistence.operate.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月4日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
//defaultRollback = true说明: true ? 测试数据不会污染数据库 : 会真正添加到数据库当中
@TransactionConfiguration(transactionManager = "transactionManager" , defaultRollback = true) 
public class OperateMarketChannelDaoMapperTest {

	
	@Autowired
	OperateMarketChannelDaoMapper operateMarketChannelDaoMapper;
	
	@Test
	public void testFindTopChannelId() {
		System.out.println(operateMarketChannelDaoMapper.findTopChannelId("46", (short)3));
	}

}
