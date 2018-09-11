
package com.hhly.usercore.controller.passport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.exception.Assert;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.TPInfoVO;
import com.hhly.usercore.local.member.service.MemberInfoService;
import com.hhly.usercore.local.passport.service.MemberRegisterService;
import com.hhly.usercore.persistence.member.po.UserInfoPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月20日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RestController
@RequestMapping("/passport/register")
public class MemberRegisterController {

	@Autowired
	MemberInfoService memberInfoService;
	
	@Autowired
	MemberRegisterService memberRegisterService;
	
	/**
	 * 渠道注册判断
	 * 
	 * @param tpInfoVO
	 * @return
	 */
	@RequestMapping(value = "channel/isRegister", method = RequestMethod.POST)
	public ResultBO<?> isRegister(@RequestBody TPInfoVO vo) {
		UserInfoPO simplePO = memberInfoService.findSimpleInfoByChannel(vo.getChannelId(), vo.getOpenid(), vo.getOrderNum());
		JSONObject resultJson = new JSONObject();
		if(!ObjectUtil.isBlank(simplePO))
			resultJson.put("status", 1);
		else
			resultJson.put("status", 0);
		return ResultBO.ok(resultJson);
	}
	
	/**
	 * 快速注册, 返回用户id
	 * 
	 * @param tpInfoVO
	 * @return
	 */
	@RequestMapping(value = "channel/getUserIdOrRegister", method = RequestMethod.POST)
	public ResultBO<?> getUserIdOrRegister(@RequestBody PassportVO vo) {
		Integer memberId = null;
		UserInfoPO userInfoPO = memberInfoService.findSimpleInfoByChannel(vo.getChannelId(),vo.getOpenId(),vo.getOrderNum());
		
		if(ObjectUtil.isBlank(userInfoPO)){
			memberId = memberRegisterService.regByChannel(vo);
		}else{
			memberId = userInfoPO.getId();
		}
		Assert.paramNotNull(memberId, "memberId");
		return ResultBO.ok(memberId);
	}	
}
