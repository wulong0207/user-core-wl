package com.hhly.usercore.persistence.pay.dao;

import java.util.List;

import com.hhly.skeleton.user.bo.UserWinInfoBO;
import com.hhly.skeleton.user.vo.UserWinInfoVO;
import org.apache.ibatis.annotations.Param;


public interface UserWinningStatisticsDaoMapper {

	/**
	 * 查询首页中奖信息统计
	 * @return
	 */
	List<UserWinInfoBO> findHomeWin();
	
	List<UserWinInfoBO> queryUserWinByLottery(UserWinInfoVO vo);

	/**
	 * 查询中奖轮播信息
	 * @param vo
	 * @return
	 */
	List<com.hhly.skeleton.lotto.base.order.bo.UserWinInfoBO> queryUserWinInfo(UserWinInfoVO vo);
}