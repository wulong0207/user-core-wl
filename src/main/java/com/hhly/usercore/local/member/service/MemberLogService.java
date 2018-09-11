
package com.hhly.usercore.local.member.service;

import com.hhly.usercore.persistence.member.po.UserModifyLogPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月25日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface MemberLogService {

	/**
	 * 添加用户操作日志
	 * @param po
	 * @date 2018年4月26日上午10:52:47
	 * @author cheng.chen
	 */
	void insertModifyLog(UserModifyLogPO po);
}
