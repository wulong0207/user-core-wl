package com.hhly.usercore.agent.controller.pay;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.pay.vo.PayBankcardVO;
import com.hhly.usercore.remote.member.service.IMemberBankcardService;

/**
 * @desc 代理输入输出控制层
 * @author xiongJinGang
 * @date 2018年3月2日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RestController
@RequestMapping("/agent")
public class AgentPayController {

	private static final Logger logger = LoggerFactory.getLogger(AgentPayController.class);

	@Resource
	private IMemberBankcardService memberBankcardService;

	/**  
	* 方法说明: 获取用户储蓄卡列表
	* @auth: xiongJinGang
	* @param payBankcard
	* @throws Exception
	* @time: 2018年3月2日 下午3:15:27
	* @return: ResultBO<?> 
	*/
	@RequestMapping(value = "/findBankList", method = RequestMethod.POST)
	public ResultBO<?> findBankList(@RequestBody PayBankcardVO payBankcard) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("获取token为：" + payBankcard.getToken() + " 的银行卡列表");
		}
		// 只获取储蓄卡
		return memberBankcardService.selectBankCardByType(payBankcard.getToken(), UserConstants.BankCardType.DEPOSIT_CARD.getKey());
	}

	/**  
	* 方法说明: 根据用户银行卡号，获取银行基本信息
	* @auth: xiongJinGang
	* @param agentBankParamVO
	* @time: 2018年3月2日 上午10:29:08
	* @return: ResultBO<?> 
	*/
	@RequestMapping(value = "/findBankInfo", method = RequestMethod.POST)
	public ResultBO<?> findBankInfo(@RequestBody PayBankcardVO payBankcard) {
		if (logger.isDebugEnabled()) {
			logger.debug("获取token为：" + payBankcard.getToken() + " 的银行卡：" + payBankcard.getCardcode() + "信息");
		}
		return memberBankcardService.getBankName(payBankcard.getToken(), payBankcard.getCardcode());
	}

	/**  
	* 方法说明: 添加银行卡
	* @auth: xiongJinGang
	* @param payBankcard
	* @throws Exception
	* @time: 2018年3月2日 下午2:31:59
	* @return: ResultBO<?> 
	*/
	@RequestMapping(value = "/addBankCard", method = RequestMethod.POST)
	public ResultBO<?> addBankCard(@RequestBody PayBankcardVO payBankcard) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("给token为：" + payBankcard.getToken() + " 添加银行卡：" + payBankcard.getCardcode());
		}
		return memberBankcardService.addBankCard(payBankcard.getToken(), payBankcard);
	}

}
