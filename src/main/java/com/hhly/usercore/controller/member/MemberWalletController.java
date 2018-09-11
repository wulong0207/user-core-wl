
package com.hhly.usercore.controller.member;

import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import com.hhly.skeleton.user.bo.ChannelMemberWalletBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.user.vo.TPInfoVO;
import com.hhly.usercore.local.member.service.MemberInfoService;
import com.hhly.usercore.local.member.service.MemberWalletService;
import com.hhly.usercore.persistence.member.po.UserInfoPO;

import java.util.List;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月18日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RestController
@RequestMapping("/member/wallet")
public class MemberWalletController {
	
	@Autowired
	MemberInfoService memberInfoService;
	
	@Autowired
	MemberWalletService memberWalletService;

	@RequestMapping("/channel/findTotal")
	public Object findTotalByChannel(@RequestBody TPInfoVO vo){
		Double totalMoney = 0d;
		UserInfoPO simplePO = memberInfoService.findSimpleInfoByChannel(vo.getChannelId(),vo.getOpenid(),vo.getOrderNum());
		if(!ObjectUtil.isBlank(simplePO)){
			totalMoney = memberWalletService.findTotalByUserId(simplePO.getId());
		}
		JSONObject resultJSON = new JSONObject();
		resultJSON.put("totalMoney", totalMoney);
		return ResultBO.ok(resultJSON);
	}

	/**
	 * 查询用户渠道钱包列表
	 * @date 2018.6.8
	 * @author zhouyang
	 * @param vo
	 * @return
	 */
	@RequestMapping(value = "/channel/list", method = RequestMethod.POST)
	public Object queryChannelMemberWalletList(@RequestBody OrderChannelVO vo) {
		List<ChannelMemberWalletBO> list = memberWalletService.queryChannelMemberWalletList(vo);
		return ResultBO.ok(list);
	}
}
