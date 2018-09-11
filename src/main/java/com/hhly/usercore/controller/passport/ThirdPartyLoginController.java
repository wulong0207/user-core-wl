package com.hhly.usercore.controller.passport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.TPInfoVO;
import com.hhly.usercore.remote.passport.service.IThirdPartyLoginService;

@RestController
@RequestMapping("/passport/login")
public class ThirdPartyLoginController {

	@Autowired
	private IThirdPartyLoginService iThirdPartyLoginService;
	
	/**
	 * 渠道登录
	 * 
	 * @param tpInfoVO
	 * @return
	 */
	@RequestMapping(value = "channel", method = RequestMethod.POST)
	public ResultBO<?> tpChannelLogin(@RequestBody TPInfoVO vo) {
		return iThirdPartyLoginService.tpChannelLogin(vo);
	}
}
