package com.hhly.usercore.base.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hhly.skeleton.activity.vo.ActivityMessageModel;
import com.hhly.skeleton.activity.vo.MsgVO;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.common.ActivityEnum;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.pay.vo.PayBankcardVO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.usercore.base.rabbitmq.provider.MessageProvider;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;

import net.sf.json.JSONObject;

/**
 * 完善资料发送MQ AOP
 * 
 * @author huangchengfang1219
 *
 */
@Component("newUserAspect")
@Aspect
@Order(10)
public class NewUserAspect {

	private static Logger logger = LoggerFactory.getLogger(NewUserAspect.class);

	@Value("${activity_channel_queue}")
	private String activityChannelQueue;
	@Autowired
	private MessageProvider messageProvider;
	@Autowired
	private UserInfoDaoMapper userInfoMapper;
	@Autowired
	private UserUtil userUtil;

	/**
	 * 完善手机号号码:手机注册后
	 * 
	 * @param joinPoint
	 * @param rtnValue
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	@AfterReturning(pointcut = "execution(* com.hhly.usercore.remote.passport.service.IMemberRegisterService.regUser(..))", returning = "rtnValue")
	public void afterRegUser(JoinPoint joinPoint, Object rtnValue) throws Throwable {
		ResultBO<UserInfoBO> result = (ResultBO<UserInfoBO>) rtnValue;
		if (result == null || result.isError()) {
			String message = result != null ? result.getMessage() : null;
			logger.info("用户注册失败，不参加新用户-注册！message:{}", message);
			return;
		}
		UserInfoBO userInfoBO = result.getData();
		// 发送注册送
		sendActivityMsg(userInfoBO.getToken(), ActivityEnum.ActivityMqConsEnum.REGISTER.getValue());
		// 发送绑定手机号送
		if (ObjectUtil.isBlank(userInfoBO.getMobile())) {
			logger.info("非手机号注册，不参加新用户-绑定手机号码！ userId:{}", userInfoBO.getId());
		} else {
			sendActivityMsg(userInfoBO.getToken(), ActivityEnum.ActivityMqConsEnum.BIND_MOBILE_NO.getValue());
		}
	}

	@AfterReturning(pointcut = "execution(* com.hhly.usercore.remote.passport.service.IThirdPartyLoginService.tp*Login(..))", returning = "rtnValue")
	@SuppressWarnings("unchecked")
	public void afterTPLogin(JoinPoint joinPoint, Object rtnValue) throws Throwable {
		ResultBO<UserInfoBO> result = (ResultBO<UserInfoBO>) rtnValue;
		if (result == null || result.isError()) {
			String message = result != null ? result.getMessage() : null;
			logger.info("用户注册失败，不参加新用户-注册！message:{}", message);
			return;
		}
		UserInfoBO userInfoBO = result.getData();
		if (!UserConstants.IS_TRUE.equals(userInfoBO.getFristRegister())) {
			logger.info("第三方登录非注册，不参加新用户-注册！userId:{}", userInfoBO.getId());
			return;
		}
		// 发送注册送
		sendActivityMsg(userInfoBO.getToken(), ActivityEnum.ActivityMqConsEnum.REGISTER.getValue());
	}

	/**
	 * 完善手机号号码:活动注册后
	 * 
	 * @param joinPoint
	 * @param rtnValue
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	@AfterReturning(pointcut = "execution(* com.hhly.usercore.remote.activity.service.IMemberActivityService.regFromActivity(..))", returning = "rtnValue")
	public void afterRegFromActivity(JoinPoint joinPoint, Object rtnValue) throws Throwable {
		ResultBO<UserInfoBO> result = (ResultBO<UserInfoBO>) rtnValue;
		if (result == null || result.isError()) {
			String message = result != null ? result.getMessage() : null;
			logger.info("用户注册失败，不参加新用户-注册！message:{}", message);
			return;
		}
		UserInfoBO userInfoBO = result.getData();
		// 发送注册送
		sendActivityMsg(userInfoBO.getToken(), ActivityEnum.ActivityMqConsEnum.REGISTER.getValue());
		// 发送绑定手机号送
		if (ObjectUtil.isBlank(userInfoBO.getMobile())) {
			logger.info("手机号为空，不参加新用户-绑定手机号码！userId:{}", userInfoBO.getId());
		} else {
			sendActivityMsg(userInfoBO.getToken(), ActivityEnum.ActivityMqConsEnum.BIND_MOBILE_NO.getValue());
		}
	}

	/**
	 * 完善手机号号码: 修改手机号码
	 * 
	 * @param joinPoint
	 */
	@SuppressWarnings("unchecked")
	@Around("execution(* com.hhly.usercore.remote.member.service.IMemberInfoService.updateMobile(..))")
	public Object aroundUpdateMobile(ProceedingJoinPoint joinPoint) throws Throwable {
		PassportVO passportVO = (PassportVO) joinPoint.getArgs()[0];
		if (passportVO == null || ObjectUtil.isBlank(passportVO.getToken())) {
			logger.info("修改手机号码获取不到用户，不参加新用户-绑定手机号码活动！");
			return joinPoint.proceed();
		}
		UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
		if (ObjectUtil.isBlank(tokenInfo)) {
			logger.info("获取不到用户，不参加新用户-绑定手机号码活动！token:{}", passportVO.getToken());
			return joinPoint.proceed();
		}
		if (!ObjectUtil.isBlank(tokenInfo.getMobile()) && tokenInfo.getMobileStatus() == 1) {
			logger.info("用户修改手机，不参加新用户-绑定手机号码活动！ userId:{}, mobile:{}", tokenInfo.getId(), tokenInfo.getMobile());
			return joinPoint.proceed();
		}
		// 防止缓存问题，从数据查询用户手机号确认存在
		UserInfoBO userInfoBO = userInfoMapper.findUserInfoByUserId(tokenInfo.getId());
		if (!ObjectUtil.isBlank(userInfoBO) && !ObjectUtil.isBlank(userInfoBO.getMobile()) && tokenInfo.getMobileStatus() == 1) {
			logger.info("用户修改手机，不参加新用户-绑定手机号码活动！ userId:{}, mobile:{}", tokenInfo.getId(), userInfoBO.getMobile());
			return joinPoint.proceed();
		}
		Object rtnValue = joinPoint.proceed();
		ResultBO<UserInfoBO> result = (ResultBO<UserInfoBO>) rtnValue;
		if (result == null || result.isError()) {
			logger.info("用户修改手机失败！不参加新用户-绑定手机号码活动！userId:{}", tokenInfo.getId());
			return rtnValue;
		}
		sendActivityMsg(tokenInfo.getToken(), ActivityEnum.ActivityMqConsEnum.BIND_MOBILE_NO.getValue());
		return rtnValue;
	}

	/**
	 * 完善真实姓名与身份证信息: 实名认证
	 * 
	 * @param joinPoint
	 */
	@AfterReturning(pointcut = "execution(* com.hhly.usercore.remote.passport.service.IMemberRegisterService.perfectRealName(..))", returning = "rtnValue")
	public void afterPerfectRealName(JoinPoint joinPoint, Object rtnValue) {
		ResultBO<?> result = (ResultBO<?>) rtnValue;
		PassportVO passportVO = (PassportVO) joinPoint.getArgs()[0];
		if (passportVO == null || result == null || result.isError()) {
			String idCard = passportVO != null ? passportVO.getIdCard() : null;
			logger.info("实名认证失败，不参加新用户-实名认证活动！idCard:{}", idCard);
			return;
		}
		sendActivityMsg(passportVO.getToken(), ActivityEnum.ActivityMqConsEnum.BIND_ID_CARD.getValue());
	}

	/**
	 * 绑定银行卡: PC端添加银行卡
	 * 
	 * @param joinPoint
	 * @param rtnValue
	 */
	@AfterReturning(pointcut = "execution(* com.hhly.usercore.remote.member.service.IMemberBankcardService.addBandCardPc(..))", returning = "rtnValue")
	public void afterAddBankCardPc(JoinPoint joinPoint, Object rtnValue) {
		ResultBO<?> result = (ResultBO<?>) rtnValue;
		PayBankcardVO userBank = (PayBankcardVO) joinPoint.getArgs()[0];
		if (result == null || result.isError() || userBank == null) {
			Integer userId = null;
			if (userBank != null && !ObjectUtil.isBlank(userBank.getToken())) {
				UserInfoBO tokenInfo = userUtil.getUserByToken(userBank.getToken());
				userId = tokenInfo != null ? tokenInfo.getId() : null;
			}
			logger.info("添加银行卡失败，不参加新用户-绑定银行卡活动！userId:{}", userId);
			return;
		}
		sendActivityMsg(userBank.getToken(), ActivityEnum.ActivityMqConsEnum.BIND_BANK_CARD.getValue());
	}

	/**
	 * 绑定银行卡: 移动端添加银行卡
	 * 
	 * @param joinPoint
	 * @param rtnValue
	 */
	@AfterReturning(pointcut = "execution(* com.hhly.usercore.remote.member.service.IMemberBankcardService.addBankCard(..))", returning = "rtnValue")
	public void afterAddBankCard(JoinPoint joinPoint, Object rtnValue) {
		ResultBO<?> result = (ResultBO<?>) rtnValue;
		String token = (String) joinPoint.getArgs()[0];
		PayBankcardVO userBank = (PayBankcardVO) joinPoint.getArgs()[1];
		if (result == null || result.isError() || ObjectUtil.isBlank(token) || userBank == null) {
			logger.info("添加银行卡失败，不参加新用户-绑定银行卡活动！");
			return;
		}
		sendActivityMsg(token, ActivityEnum.ActivityMqConsEnum.BIND_BANK_CARD.getValue());
	}

	private void sendActivityMsg(String token, Integer type) {
		ActivityMessageModel model = new ActivityMessageModel();
		model.setMessageSource("user-core");
		model.setType(type);
		MsgVO msgVO = new MsgVO();
		msgVO.setTransId(token);
		model.setMessage(msgVO);
		logger.info("发送新用户活动MQ: {}", JSONObject.fromObject(model).toString());
		messageProvider.sendMessage(activityChannelQueue, model);
	}
}
