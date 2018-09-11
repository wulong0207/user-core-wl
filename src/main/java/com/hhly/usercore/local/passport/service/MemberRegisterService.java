
package com.hhly.usercore.local.passport.service;

import com.hhly.skeleton.user.vo.PassportVO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月25日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface MemberRegisterService {
	
	/**
	 * 渠道注册返回用户Id
	 * @param vo
	 * @return
	 * @date 2018年4月25日下午6:22:09
	 * @author cheng.chen
	 */
	Integer regByChannel(PassportVO vo);
}
