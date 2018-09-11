package com.hhly.usercore.base.common.service.impl;



import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.mq.msg.SendModel;
import com.hhly.skeleton.base.util.MailUtil;
import com.hhly.skeleton.user.vo.UserMessageVO;
import com.hhly.usercore.base.common.service.SmsService;
import com.hhly.usercore.base.rabbitmq.provider.MessageProvider;

/**
 * 短信发送实现类
 * @desc
 * @author zhouyang
 * @date 2017年2月20日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service("smsService")
public class SmsServiceImpl implements SmsService {

	private static final Logger logger = Logger.getLogger(SmsServiceImpl.class);

	@Autowired
	MessageProvider messageProvider;

	@Value("${send_queue}")
	private String sendQueue;


	@Override
	public boolean doSendSms(UserMessageVO userMessageVO) throws Exception {
		boolean isSuccess = true;
		short type = 2;
		try {
			SendModel sendModel = new SendModel();
			sendModel.setAccount(userMessageVO.getAccount());
			sendModel.setType(type);
			sendModel.setContent(userMessageVO.getMessage());
			messageProvider.sendMessage(sendQueue, sendModel);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(ResultBO.err(MessageCodeConstants.SMS_SEND_DEFEAT_SYS));
			return false;
		}
		return isSuccess;
	}

	@Override
	public boolean doSendMail(UserMessageVO userMessageVO) throws Exception {
		boolean isSuccess = true;
		short type = 5;
		try {
			SendModel sendModel = new SendModel();
			sendModel.setAccount(userMessageVO.getAccount());
			sendModel.setType(type);
			sendModel.setContent(userMessageVO.getMessage());
			messageProvider.sendMessage(sendQueue, sendModel);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(ResultBO.err(MessageCodeConstants.MAIL_SEND_DEFEAT_SYS));
			return false;
		}
		return isSuccess;
	}

	public static void main(String[] args) throws Exception {
//		SmsSendUtil sms = new SmsSendUtil(UserConstants.SN, UserConstants.PWD);
//		String result = sms.mdsmssend("15817375451", "测试短信, 测试短信供应商的连接", "", "", "", "");
//		System.out.println(result);

		MailUtil mail = new MailUtil();
		boolean isSuccess = mail.sendMail("测试邮件", UserConstants.YICAI_NET, "chenc848@2ncai.com", "2499390165@qq.com");
		System.out.println(isSuccess);
	}


}
