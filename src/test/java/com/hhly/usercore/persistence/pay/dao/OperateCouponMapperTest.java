
package com.hhly.usercore.persistence.pay.dao;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.hhly.skeleton.pay.bo.OperateCouponBO;
import com.hhly.skeleton.pay.vo.OperateCouponVO;


/**
 * @desc    
 * @author  cheng chen
 * @date    2018年1月23日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
//defaultRollback = true说明: true ? 测试数据不会污染数据库 : 会真正添加到数据库当中
@TransactionConfiguration(transactionManager = "transactionManager" , defaultRollback = true) 
public class OperateCouponMapperTest {

	@Autowired
	OperateCouponMapper operateCouponMapper;
	
	@Test
	public void testGetUserRedBalance() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCouponeCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserCoupone() {
		OperateCouponVO vo = new OperateCouponVO();
		vo.setUserId(30);
		vo.setRedType((short)1);
//		vo.setRedStatus("3");
		vo.setPageIndex(0);
		vo.setPageSize(10);
		List<OperateCouponBO> list = operateCouponMapper.getUserCoupone(vo);
		System.err.println(JSONArray.toJSONString(list));
	}

	@Test
	public void testFindCouponCountStatus() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindCouponCountRedType() {
		fail("Not yet implemented");
	}

}
