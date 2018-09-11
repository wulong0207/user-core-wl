package com.hhly.usercore.remote.member.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.common.LotteryEnum;
import com.hhly.skeleton.base.constants.SymbolConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.NumberFormatUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.constants.Constants;
import com.hhly.skeleton.user.bo.UserWinInfoBO;
import com.hhly.skeleton.user.vo.UserWinInfoVO;
import com.hhly.usercore.persistence.pay.dao.UserWinningStatisticsDaoMapper;
import com.hhly.usercore.remote.member.service.IMemberWinningService;
import org.springframework.util.StringUtils;

/**
 * 中奖统计
 *
 * @author huangchengfang1219
 * @date 2017年12月14日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("memberWinningService")
public class MemberWinningServiceImpl implements IMemberWinningService {

	@Autowired
	private UserWinningStatisticsDaoMapper userWinningStatisticsDaoMapper;

	/**
	 * 查询钱包信息统计
	 * 
	 * @return
	 */
	public List<UserWinInfoBO> findHomeWin() {
		return userWinningStatisticsDaoMapper.findHomeWin();
	}

	public List<UserWinInfoBO> queryUserWinByLottery(UserWinInfoVO vo) {
		if(vo.getPageIndex() == null) {
			vo.setPageIndex(Constants.NUM_0);
		}
		if(vo.getPageSize() == null) {
			vo.setPageSize(Constants.NUM_10);
		}
		return userWinningStatisticsDaoMapper.queryUserWinByLottery(vo);
	}

	@Override
	public ResultBO<?> queryUserWinInfo(String lotteryCodes) {

		List<String> codeList = null;
		if (!ObjectUtil.isBlank(lotteryCodes)) {
			codeList = Arrays.asList(StringUtils.tokenizeToStringArray(lotteryCodes, SymbolConstants.COMMA));
		}
		UserWinInfoVO winInfoVO = new UserWinInfoVO();
		winInfoVO.setCodeList(codeList);
		List<com.hhly.skeleton.lotto.base.order.bo.UserWinInfoBO> winInfoList = userWinningStatisticsDaoMapper.queryUserWinInfo(winInfoVO);
		List<String> list = new ArrayList<>();
		for (int i = 0; i<winInfoList.size(); i++) {
			StringBuffer str = new StringBuffer();
			if (!ObjectUtil.isBlank(winInfoList.get(i).getNickName()) && !ObjectUtil.isBlank(winInfoList.get(i).getPreBonus()) && !ObjectUtil.isBlank(winInfoList.get(i).getLotteryName())) {
				str.append(UserConstants.CONGRATULATION );
				str.append(StringUtil.encrptionNickname(winInfoList.get(i).getNickName()));
				str.append(UserConstants.IN);
				if (winInfoList.get(i).getLotteryCode().equals(LotteryEnum.Lottery.FB.getName())) {
					str.append(LotteryEnum.Lottery.FB.getDesc());
				} else if (winInfoList.get(i).getLotteryCode().equals(LotteryEnum.Lottery.BB.getName())) {
					str.append(LotteryEnum.Lottery.BB.getDesc());
				} else {
					str.append(winInfoList.get(i).getLotteryName());
				}
				str.append(UserConstants.WIN );
				str.append(NumberFormatUtil.dispose(winInfoList.get(i).getPreBonus()));
				list.add(str.toString());
			}
		}
		return ResultBO.ok(list);
	}


	@Override
	public ResultBO<?> queryWinInfoList(UserWinInfoVO vo) {
		return ResultBO.ok(userWinningStatisticsDaoMapper.queryUserWinInfo(vo));
	}


}
