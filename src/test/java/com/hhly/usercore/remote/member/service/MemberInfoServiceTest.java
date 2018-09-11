
package com.hhly.usercore.remote.member.service;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.UserInfoVO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年3月28日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
//defaultRollback = true说明: true ? 测试数据不会污染数据库 : 会真正添加到数据库当中
@TransactionConfiguration(transactionManager = "transactionManager" , defaultRollback = true) 
public class MemberInfoServiceTest {
	
	@Autowired
	IMemberInfoService memberInfoService;

	@Test
	public void testGetDisturb() {
		fail("Not yet implemented");
	}

	@Test
	public void testDoNotDisturb() {
		fail("Not yet implemented");
	}

	@Test
	public void testValidateNickname() {
		fail("Not yet implemented");
	}

	@Test
	public void testShowUserIndex() {
		String token = "3121022aad3dd4b39b77575287004ca63";
		UserInfoVO vo = new UserInfoVO();
		vo.setToken(token);
		ResultBO bo = memberInfoService.showUserIndex(vo);
		System.out.println(JSONObject.toJSONString(bo));
	}

	@Test
	public void testShowUserPersonalData() {
		fail("Not yet implemented");
	}

	@Test
	public void testShowUserLoginData() {
		fail("Not yet implemented");
	}

	@Test
	public void testCheckMobile() {
		fail("Not yet implemented");
	}

	@Test
	public void testCheckEmail() {
		fail("Not yet implemented");
	}

	@Test
	public void testCheckCreditCardNum() {
		fail("Not yet implemented");
	}

	@Test
	public void testCheckIDCard() {
		fail("Not yet implemented");
	}

	@Test
	public void testCheckPassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateNickname() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateAccount() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdatePassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateMobile() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateEmail() {
		fail("Not yet implemented");
	}

	@Test
	public void testOpenMobileLogin() {
		fail("Not yet implemented");
	}

	@Test
	public void testCloseMobileLogin() {
		fail("Not yet implemented");
	}

	@Test
	public void testOpenEmailLogin() {
		fail("Not yet implemented");
	}

	@Test
	public void testCloseEmailLogin() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindUserInfoByToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testExpireRedisKey() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateLastUsePayId() {
		fail("Not yet implemented");
	}

	@Test
	public void testUploadHeadPortrait() {
		fail("Not yet implemented");
	}

	@Test
	public void testSenNodeMsg() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserInfoByAgent() {
		fail("Not yet implemented");
	}

	@Test
	public void testVerifyMobile() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTokenStatus() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddMemberInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateMemberInfo() {
		fail("Not yet implemented");
	}

}
