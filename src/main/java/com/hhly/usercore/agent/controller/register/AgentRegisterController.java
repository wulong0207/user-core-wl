
package com.hhly.usercore.agent.controller.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.usercore.agent.service.AgentRegisterService;
import com.hhly.usercore.remote.member.service.IMemberInfoService;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;
import com.hhly.usercore.remote.passport.service.IMemberRegisterService;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年3月3日
 * @company 益彩网络科技公司
 * @version 1.0
 */

@RestController
@RequestMapping("/agent/register")
public class AgentRegisterController {
	
	@Autowired
	IMemberInfoService memberInfoService;
	
	@Autowired
	IVerifyCodeService verifyCodeService;
	
	@Autowired
	IMemberRegisterService memberRegisterService;
	
	@Autowired
	AgentRegisterService agentRegisterService;

	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	public Object validate(@RequestBody JSONObject json){
		return agentRegisterService.validate(json);
	}
	
	@RequestMapping(value = "/getMobileCode", method = RequestMethod.POST)
	public Object getMobileCode(@RequestBody JSONObject json){	
		return verifyCodeService.sendNewMobileVerifyCode(json.toJavaObject(PassportVO.class));
	}	
	
	@RequestMapping(value = "/setMobile", method = RequestMethod.POST)
	public Object setMobile(@RequestBody JSONObject json){	
		return memberInfoService.updateMobile(json.toJavaObject(PassportVO.class));
	}
	
	@RequestMapping(value = "/setIdcard", method = RequestMethod.POST)
	public Object setIdcard(@RequestBody JSONObject json){	
		return memberRegisterService.perfectRealName(json.toJavaObject(PassportVO.class));
	}	
	
	@RequestMapping(method = RequestMethod.POST)
	public Object register(@RequestBody JSONObject json){
		return agentRegisterService.register(json);
	}
}
