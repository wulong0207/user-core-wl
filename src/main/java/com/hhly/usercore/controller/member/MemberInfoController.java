
package com.hhly.usercore.controller.member;

import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.user.vo.TPInfoVO;
import com.hhly.usercore.local.member.service.MemberInfoService;
import com.hhly.usercore.persistence.member.po.UserInfoPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月8日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RestController
@RequestMapping("/member/info")
public class MemberInfoController {
	
	@Autowired
	MemberInfoService memberInfoService;

	@RequestMapping("/findSimpleInfoByChannel")
	public Object findSimpleInfoByChannel(@RequestBody TPInfoVO vo){
		
		UserInfoPO simplePO = memberInfoService.findSimpleInfoByChannel(vo.getChannelId(),vo.getOpenid(),vo.getOrderNum());
		if(ObjectUtil.isBlank(simplePO))
			return ResultBO.err("40202");
		else
			return ResultBO.ok(simplePO);
	}

	/**
	 * @desc 获取会员id
	 * @author zhouyang
	 * @date 2018.6.8
	 * @param vo
	 * @return
	 */
	@RequestMapping("/getUserId")
	public Object queryUserIdByChannelOpenId(@RequestBody OrderChannelVO vo) {
		int userId = memberInfoService.getUserIdByChannelOpenId(vo);
		JSONObject json = new JSONObject();
		if (!ObjectUtil.isBlank(userId)) {
			json.put("userId", userId);
		} else {
			return ResultBO.err("40202");
		}
		return ResultBO.ok(json);
	}



	
	
}
