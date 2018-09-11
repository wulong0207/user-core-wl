
package com.hhly.usercore.persistence.member.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.usercore.persistence.member.po.UserInfoPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年1月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
//defaultRollback = true说明: true ? 测试数据不会污染数据库 : 会真正添加到数据库当中
@TransactionConfiguration(transactionManager = "transactionManager" , defaultRollback = true) 
public class UserInfoDaoMapperTest {

	@Autowired
	UserInfoDaoMapper userInfoDaoMapper;
	
	@Test
	public void testFindUserInfoList() {
		UserInfoVO vo = new UserInfoVO();
		vo.setMobile("13510761111");
		List<UserInfoBO> list = userInfoDaoMapper.findUserInfoList(vo);
		System.err.println(JSONArray.toJSONString(list));
	}

	@Test
	public void findUserIdList() {
		UserInfoVO vo = new UserInfoVO();
		List<Integer> list = userInfoDaoMapper.findUserIdList(vo);
		System.err.println(JSONArray.toJSONString(list));
	}

	public UserInfoBO findUserInfoByUserId(Integer userId) {
		return userInfoDaoMapper.findUserInfoByUserId(userId);
	}

	public int findUserInfoByAccount(UserInfoVO userInfoVO) {
		return userInfoDaoMapper.findUserInfoByAccount(userInfoVO);
	}

	public UserInfoBO findUserInfoToCache(UserInfoVO userInfoVO) {
		return userInfoDaoMapper.findUserInfoToCache(userInfoVO);
	}

	public UserInfoBO findUserWalletById(Integer userId) {
		return userInfoDaoMapper.findUserWalletById(userId);
	}

	public int findUserInfoByIdCard(String idCard) {
		return userInfoDaoMapper.findUserInfoByIdCard(idCard);
	}

	public UserInfoBO findUserIndexByUserId(UserInfoVO vo) {
		return userInfoDaoMapper.findUserIndexByUserId(vo);
	}

	public UserInfoBO findUserPasonalDataByUserId(Integer userId) {
		return userInfoDaoMapper.findUserPasonalDataByUserId(userId);
	}

	public UserInfoBO findUserInfo(UserInfoVO userInfoVO) {
		return userInfoDaoMapper.findUserInfo(userInfoVO);
	}

	public int updateUserInfo(UserInfoPO userInfoPO) {
		return userInfoDaoMapper.updateUserInfo(userInfoPO);
	}

	public int findBindMECount(UserInfoVO userInfoVO) {
		return userInfoDaoMapper.findBindMECount(userInfoVO);
	}

	public int addUser(UserInfoPO userInfoPO) {
		return userInfoDaoMapper.addUser(userInfoPO);
	}

	@Test
	public void findSimpleUserInfo() {
		UserInfoPO userInfoPO = new UserInfoPO();
		userInfoPO.setId(1);
		UserInfoPO dataPO = userInfoDaoMapper.findSimpleUserInfo(userInfoPO);
		System.err.println(JSONObject.toJSONString(dataPO));
	}
	
}
