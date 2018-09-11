
package com.hhly.usercore.agent.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年3月3日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface AgentRegisterService {

	Object validate(JSONObject json);
	
	Object register(JSONObject json);
}
