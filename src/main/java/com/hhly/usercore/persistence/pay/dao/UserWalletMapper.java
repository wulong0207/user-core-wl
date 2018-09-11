package com.hhly.usercore.persistence.pay.dao;

import com.hhly.skeleton.user.bo.UserWalletBO;
import com.hhly.usercore.persistence.pay.po.UserWalletPO;

/**
 * @desc 用户钱包
 * @author xiongjingang
 * @date 2017年3月16日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface UserWalletMapper {

	/**  
	* 方法说明: 查找用户钱包信息
	* @auth: xiongjingang
	* @param userId
	* @time: 2017年3月16日 下午4:17:09
	* @return: UserWalletBO 
	*/
	UserWalletBO getWalletByUserId(Integer userId);

	/**  
	* 方法说明: 添加用户钱包
	* @auth: xiongJinGang
	* @param userWalletPO
	* @time: 2017年3月24日 下午5:45:01
	* @return: int 
	*/
	int addUserWallet(UserWalletPO userWalletPO);
}
