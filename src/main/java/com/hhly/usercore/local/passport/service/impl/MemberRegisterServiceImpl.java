
package com.hhly.usercore.local.passport.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.constants.UserConstants.LoginTypeEnum;
import com.hhly.skeleton.base.constants.UserConstants.UserOperationEnum;
import com.hhly.skeleton.base.exception.ServiceRuntimeException;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.TPInfoVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.local.channel.service.ChannelInfoService;
import com.hhly.usercore.local.member.service.MemberLogService;
import com.hhly.usercore.local.member.service.MemberSecurityService;
import com.hhly.usercore.local.member.service.MemberWalletService;
import com.hhly.usercore.local.passport.service.MemberRegisterService;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.member.po.UserModifyLogPO;

/**
 * @desc
 * @author cheng chen
 * @date 2018年4月25日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service
public class MemberRegisterServiceImpl implements MemberRegisterService {

	@Autowired
	ChannelInfoService channelInfoService;
	
	@Autowired
	MemberSecurityService memberSecurityService;
	
	@Autowired
	MemberLogService memberLogService;
	
	@Autowired
	MemberWalletService memberWalletService;

	@Autowired
	UserInfoDaoMapper userInfoDaoMapper;
	
	@Autowired
	PublicMethod publicMethod;

	@Override
	public Integer regByChannel(PassportVO vo) {
		Integer userId = null;
		String channelOpenID = channelInfoService.getChannelOpenID(vo.getChannelId(), vo.getOpenId(), vo.getOrderNum());
		vo.setOpenId(channelOpenID);

		UserInfoVO userInfoVO = new UserInfoVO();
		userInfoVO.setChannelOpenID(channelOpenID);

		UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
		
		if(!ObjectUtil.isBlank(userInfoBO))
			throw new ServiceRuntimeException("会员已存在");

		TPInfoVO tpInfoVO = new TPInfoVO();
		tpInfoVO.setOpenid(channelOpenID);
		tpInfoVO.setMobile(vo.getMobile());
		tpInfoVO.setIp(vo.getIp());
		tpInfoVO.setType(LoginTypeEnum.CHANNEL.getKey());
		tpInfoVO.setChannelId(vo.getChannelId());
		tpInfoVO.setAccountName(vo.getAccount());
		tpInfoVO.setNickname(vo.getNickname());
		tpInfoVO.setPlatform(vo.getPlatform());
		userId = createUser(tpInfoVO);
		
		return userId;
	}

	/**
	 * 
	 * @param vo
	 * @return
	 * @date 2018年4月25日下午5:51:17
	 * @author cheng.chen
	 */
	private Integer createUser(TPInfoVO vo) {
		UserInfoPO userInfoPO = new UserInfoPO();
		//用户账号名验证
		if(!ObjectUtil.isBlank(vo.getAccountName()))
			userInfoPO.setAccount(publicMethod.getRadomName(vo.getAccountName()));
		else
			userInfoPO.setAccount(publicMethod.getRadomName(null));
		//用户昵称验证
		if (!ObjectUtil.isBlank(vo.getNickname()))
			userInfoPO.setNickName(publicMethod.getRadomName(vo.getNickname()));
		else
			userInfoPO.setNickName(userInfoPO.getAccount());
		if (!ObjectUtil.isBlank(vo.getMobile())){
			UserInfoVO mobileVO = new UserInfoVO();
			mobileVO.setMobile(vo.getMobile());
			int mobileCount = userInfoDaoMapper.findBindMECount(mobileVO);
			if(mobileCount == 0)
				userInfoPO.setMobile(vo.getMobile());
		}
        if(!ObjectUtil.isBlank(userInfoPO.getMobile())){
	        userInfoPO.setMobileStatus(UserConstants.IS_TRUE);
	        userInfoPO.setIsMobileLogin(UserConstants.IS_TRUE);
        }else{
	        userInfoPO.setMobileStatus(UserConstants.IS_FALSE);
	        userInfoPO.setIsMobileLogin(UserConstants.IS_FALSE);
        }
		if (!ObjectUtil.isBlank(vo.getHeadimgurl()))
			userInfoPO.setHeadUrl(vo.getHeadimgurl());
		if (!ObjectUtil.isBlank(vo.getSex()))
			userInfoPO.setSex(vo.getSex());
		if (!ObjectUtil.isBlank(vo.getProvince()) || !ObjectUtil.isBlank(vo.getCity()))
			userInfoPO.setAddress(vo.getProvince() + vo.getCity());
		if (!ObjectUtil.isBlank(vo.getOpenid())) {
			if (vo.getType() == LoginTypeEnum.WECHAT.getKey()) {
				userInfoPO.setWechatOpenID(vo.getOpenid());
				userInfoPO.setWechatName(vo.getNickname());
			} else if (vo.getType() == LoginTypeEnum.QQ.getKey()) {
				userInfoPO.setHeadUrl(vo.getFigureurl_qq_2());
				userInfoPO.setQqOpenID(vo.getOpenid());
				userInfoPO.setQqName(vo.getNickname());
			} else if (vo.getType() == LoginTypeEnum.CHANNEL.getKey()) {
				userInfoPO.setChannelOpenID(vo.getOpenid());
			}
		}
		if (ObjectUtil.isBlank(vo.getChannelId())) {
			userInfoPO.setChannelId(UserConstants.OTHER_CHANNEL);
		} else {
			userInfoPO.setChannelId(vo.getChannelId());
		}
		userInfoPO.setAccountStatus(UserConstants.IS_TRUE);
		userInfoPO.setEmailStatus(UserConstants.IS_FALSE);
		userInfoPO.setIsEmailLogin(UserConstants.IS_FALSE);
		userInfoPO.setAccountModify(UserConstants.IS_FALSE);
		userInfoPO.setPlatform(vo.getPlatform());
		// 添加会员账户
		userInfoDaoMapper.addUser(userInfoPO);
		// 用户id
		Integer userId = userInfoPO.getId();
		// 添加会员钱包
		memberWalletService.initWallet(userId);
		// 添加日志
		UserModifyLogPO userModifyLogPO = new UserModifyLogPO(userId, UserOperationEnum.REGISTER_SUCCESS.getKey(), 
				UserConstants.IS_TRUE, vo.getIp(), null, vo.getOpenid(), UserConstants.UserOperationEnum.REGISTER_SUCCESS.getValue());
		memberLogService.insertModifyLog(userModifyLogPO);
		return userId;
	}

}
