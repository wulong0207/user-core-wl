package com.hhly.usercore.persistence.member.dao;

import java.util.List;

import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import org.apache.ibatis.annotations.Param;

/**
 * 用户基本信息mapper接口
 * @desc
 * @author zhouyang
 * @date 2017年3月16日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface UserInfoDaoMapper {

	/**
	 * 查询用户集合
	 * @param userInfoVO
	 * @return
	 */
	List<Integer> findUserIdList(UserInfoVO userInfoVO);

	/**
	 * 通过用户id查询用户信息
	 * @param userId
	 * @return
	 */
	UserInfoBO findUserInfoByUserId(Integer userId);

	/**
	 * 通过账户名获取用户信息
	 * @param userInfoVO
	 * @return
	 */
	int findUserInfoByAccount(UserInfoVO userInfoVO);

	/**
	 * 用户登录验证通过，将用户某些信息存入缓存中
	 *
	 * @param userInfoVO
	 * @return
	 */
	UserInfoBO findUserInfoToCache(UserInfoVO userInfoVO);

	/**
	 * 查询钱包信息以及红包过期数量
	 *
	 * @param userId
	 * @return
	 */
	UserInfoBO findUserWalletById(Integer userId);

	/**
	 * 注册 - 查询身份证号码绑定的帐号数量
	 *
	 * @param idCard 身份证号码
	 * @return
	 */
	int findUserInfoByIdCard(String idCard);

	/**
	 * 用户首页信息展示
	 * @param vo 用户id
	 * @return
	 */
	UserInfoBO findUserIndexByUserId(UserInfoVO vo);
	
	/**
	 * 个人资料信息展示
	 * @param userId 用户id
	 * @return
	 */
	UserInfoBO findUserPasonalDataByUserId(Integer userId);
	
	/**
	 * 注册 - 通过手机号查询
	 * @return
	 */
	UserInfoBO findUserInfo(UserInfoVO userInfoVO);

	
	/**
	 * 修改用户信息
	 * @param userInfoPO
	 * @return
	 */
	int updateUserInfo(UserInfoPO userInfoPO);

	/**
	 * 查询手机号或邮箱地址绑定的帐号数量
	 * @param userInfoVO
	 * @return
	 */
	int findBindMECount(UserInfoVO userInfoVO);

	/**
	 * 查询会员用户集合
	 * @return
	 * @date 2017年8月24日上午11:46:37
	 * @author cheng.chen
	 */
	List<UserInfoBO> findUserInfoList(UserInfoVO userInfoVO);

	/**
	 * 新增用户
	 * @param userInfoPO
	 * @return
	 */
	int addUser(UserInfoPO userInfoPO);
	
	/**
	 * 新增改版后的sql
	 */
	/**
	 * 条件查询用户简单对象信息
	 * @param userInfoPO
	 * @return
	 * @date 2018年4月8日下午7:24:44
	 * @author cheng.chen
	 */
	UserInfoPO findSimpleUserInfo(UserInfoPO userInfoPO);

	/**
	 * 通过渠道openId获取本站用户id
	 * @param channelOpenId
	 * @return
	 */
	int getUserIdByChannelOpenId(@Param("channelOpenId") String channelOpenId);

}
