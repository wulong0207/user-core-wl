package com.hhly.usercore.controller.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.UserWinInfoVO;
import com.hhly.usercore.remote.member.service.IMemberWinningService;

@RestController
@RequestMapping("/member/winning")
public class MemberWinningController {

	@Autowired
	private IMemberWinningService memberWinningService;

	/**
	 * 分彩种查询中奖统计信息
	 * 
	 * @param vo
	 * @return
	 */
	@RequestMapping(value = "/lottery/list", method = RequestMethod.POST)
	public Object queryUserWinByLottery(@RequestBody UserWinInfoVO vo) {
		return ResultBO.ok(memberWinningService.queryUserWinByLottery(vo));
	}

	/**
	 * 查询最新中奖信息
	 * 
	 * @param vo
	 * @return
	 */
	@RequestMapping(value = "/new/list", method = RequestMethod.POST)
	public Object queryWinInfoList(@RequestBody UserWinInfoVO vo) {
		return memberWinningService.queryWinInfoList(vo);
	}
}
