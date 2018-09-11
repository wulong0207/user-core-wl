package com.hhly.usercore.base.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.Constants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.mq.msg.MessageModel;
import com.hhly.skeleton.base.mq.msg.OperateNodeMsg;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.RegularValidateUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.user.bo.KeywordBO;
import com.hhly.skeleton.user.bo.MUserIpBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserInfoLastLogBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.skeleton.user.vo.UserModifyLogVO;
import com.hhly.usercore.base.rabbitmq.provider.MessageProvider;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.agent.dao.AgentInfoDaoMapper;
import com.hhly.usercore.persistence.agent.po.AgentInfoPO;
import com.hhly.usercore.persistence.dic.dao.KeywordMapper;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.security.dao.UserSecurityDaoMapper;
import com.hhly.usercore.persistence.security.po.UserModifyLogPO;

/**
 * @author zhouyang
 * @version 1.0
 * @desc 用户中心共用方法
 * @date 2017/6/1
 * @company 益彩网络科技公司
 */
@Component
@EnableAsync
public class PublicMethod {
	
    @Autowired
    private UserInfoDaoMapper userInfoDaoMapper;

    @Autowired
    private UserSecurityDaoMapper userSecurityDaoMapper;
    
    @Autowired
	private MessageProvider messageProvider;

    @Autowired
    private KeywordMapper keywordMapper;
    
	@Autowired
	private AgentInfoDaoMapper agentInfoDaoMapper;
    
    @Resource
    private RedisUtil redisUtil;
    
    @Autowired
    private UserUtil userUtil;

    @Value("${before_file_url}")
    private String before_file_url;
	private static final Logger LOGGER =  LoggerFactory.getLogger(PublicMethod.class);

    /**
     * 查询账户钱包和红包
     * @param userInfoBO
     * @param userId
     * @return
     */
    public  UserInfoBO getUserIndexBO(UserInfoBO userInfoBO, Integer userId){
        UserInfoBO userWallet = userInfoDaoMapper.findUserWalletById(userId);
        if (!ObjectUtil.isBlank(userWallet)) {
            userInfoBO.setRedPackBalance(userWallet.getRedPackBalance());
            userInfoBO.setUserWalletBalance(userWallet.getUserWalletBalance());
            userInfoBO.setExpireCount(userWallet.getExpireCount());
        }
        return userWallet;
    }

    /**
     * 计算账户安全积分
     * @param userInfoBO
     * @return
     */
    public Integer getSafeIntegration(UserInfoBO userInfoBO){
        Integer safeInteral = 0;
        if (!ObjectUtil.isBlank(userInfoBO.getAccount())) {
            safeInteral+=14;
        }
        if (!ObjectUtil.isBlank(userInfoBO.getMobile()) && userInfoBO.getMobileStatus().equals(UserConstants.IS_TRUE)) {
            safeInteral+=18;
        }
        if (!ObjectUtil.isBlank(userInfoBO.getEmail())) {
            safeInteral+=14;
        }
        if (!ObjectUtil.isBlank(userInfoBO.getRealName()) && !ObjectUtil.isBlank(userInfoBO.getIdCard())) {
            safeInteral+=20;
        }
        if (!ObjectUtil.isBlank(userInfoBO.getBankCount()) && userInfoBO.getBankCount() > 0) {
            safeInteral+=14;
        }
        if (!ObjectUtil.isBlank(userInfoBO) && !ObjectUtil.isBlank(userInfoBO.getPassword())) {
            safeInteral+=10;
        }
        UserInfoLastLogBO userInfoLastLogBO = userSecurityDaoMapper.findLastLoginDetail(userInfoBO.getId());
        if(!ObjectUtil.isBlank(userInfoLastLogBO)) {
            if(!ObjectUtil.isBlank(userInfoLastLogBO.getLastUpdatePwdTime())){
                if (DateUtil.getDifferenceTime(new Date(), userInfoLastLogBO.getLastUpdatePwdTime()) < UserConstants.THREE_MONTH) {
                    safeInteral+=10;
                }
            }
        }
        return safeInteral;
    }

    /**
     * 返回信息处理
     * @param userInfoBO
     * @param reUserInfoBO
     * @param token
     */
    public void toRedis(UserInfoBO userInfoBO, UserInfoBO reUserInfoBO, String token, Short loginType){
        //账户的登录方式
        reUserInfoBO.setLoginType(loginType);
        reUserInfoBO.setValidatePass(UserConstants.IS_FALSE);
        reUserInfoBO.setToken(token);
        //账户安全积分
        reUserInfoBO.setSafeIntegral(getSafeIntegration(userInfoBO));
        //账户钱包信息
        getUserIndexBO(reUserInfoBO, userInfoBO.getId());
        //账户是否实名认证
        attestationStatus(userInfoBO, reUserInfoBO);
        //账户是否实名认证
        pwdStatus(userInfoBO, reUserInfoBO);
        //账户上一次登录时间地点
        lastLogTime(reUserInfoBO);
    }

    /**
     * 账户是否绑定第三方帐号
     * @param tokenInfo
     * @param resultInfoBO
     */
    public void tpBindStatus(UserInfoBO tokenInfo, UserInfoBO resultInfoBO) {
        if (!ObjectUtil.isBlank(tokenInfo.getQqOpenID())) {
            resultInfoBO.setBindQQ(UserConstants.IS_TRUE);
        } else {
            resultInfoBO.setBindQQ(UserConstants.IS_FALSE);
        }
        if (!ObjectUtil.isBlank(tokenInfo.getWechatUnionID())) {
            resultInfoBO.setBindWechat(UserConstants.IS_TRUE);
        } else {
            resultInfoBO.setBindWechat(UserConstants.IS_FALSE);
        }
        if (!ObjectUtil.isBlank(tokenInfo.getSinaBlogOpenID())) {
            resultInfoBO.setBindWB(UserConstants.IS_TRUE);
        } else {
            resultInfoBO.setBindWB(UserConstants.IS_FALSE);
        }
    }

    /**
     * 账户是否实名认证
     * @param userInfoBO
     * @param reUserInfoBO
     */
    public void attestationStatus(UserInfoBO userInfoBO, UserInfoBO reUserInfoBO) {
        if (!ObjectUtil.isBlank(userInfoBO.getRealName()) && !ObjectUtil.isBlank(userInfoBO.getIdCard())) {
            reUserInfoBO.setAttestationRealName(UserConstants.IS_TRUE);
        } else {
            reUserInfoBO.setAttestationRealName(UserConstants.IS_FALSE);
        }
    }

    /**
     * 账户是否设置密码
     * @param userInfoBO
     * @param reUserInfoBO
     */
    public void pwdStatus(UserInfoBO userInfoBO, UserInfoBO reUserInfoBO) {
        if (!ObjectUtil.isBlank(userInfoBO.getPassword())) {
            reUserInfoBO.setIsSetPassword(UserConstants.IS_TRUE);
        } else {
            reUserInfoBO.setIsSetPassword(UserConstants.IS_FALSE);
        }
    }

    /**
     * 账户上一次登录的时间地点
     * @param reUserInfoBO
     */
    public void lastLogTime(UserInfoBO reUserInfoBO) {
        //账户上次登录的时间和地点
        UserInfoLastLogBO userInfoLastLogBO = userSecurityDaoMapper.findLastLoginDetail(reUserInfoBO.getId());
        if (!ObjectUtil.isBlank(userInfoLastLogBO)) {
            reUserInfoBO.setLastLoginTime(userInfoLastLogBO.getLastLoginTime());
            reUserInfoBO.setIp(userInfoLastLogBO.getProvince());
        }
    }

    /**
     * 账户返回信息加密
     * @param userInfoBO
     * @param reUserInfoBO
     */
    public void encryption(UserInfoBO userInfoBO, UserInfoBO reUserInfoBO){
        //账户敏感信息加密
        if (!ObjectUtil.isBlank(userInfoBO.getIdCard())) {
            reUserInfoBO.setIdCard(StringUtil.hideString(userInfoBO.getIdCard(), (short)1));
        }
        if (!ObjectUtil.isBlank(userInfoBO.getMobile())) {
            reUserInfoBO.setMobile(StringUtil.hideString(userInfoBO.getMobile(), (short)3));
        }
        if (!ObjectUtil.isBlank(userInfoBO.getEmail())) {
            reUserInfoBO.setEmail(StringUtil.hideString(userInfoBO.getEmail(), (short)4));
        }
        if (!ObjectUtil.isBlank(userInfoBO.getRealName())) {
            reUserInfoBO.setRealName(StringUtil.hideString(userInfoBO.getRealName(), (short)5));
        }
    }

    /**
     * 设置登录成功日志参数
     * @param userInfoBO
     * @return
     */
    public UserModifyLogVO setLogVOValue(UserInfoBO userInfoBO) {
        UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
        userModifyLogVO.setUserId(userInfoBO.getId());
        userModifyLogVO.setOperationStatus(UserConstants.IS_FALSE);
        userModifyLogVO.setUserAction(UserConstants.UserOperationEnum.LOGIN_FAIL_PASSWORD.getKey());
        return userModifyLogVO;
    }

    /**
     * 用户id
     * @param id
     * @return
     */
    public UserInfoVO id(Integer id) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(id);
        return userInfoVO;
    }

    /**
     * 帐户名
     * @param account
     * @return
     */
    public UserInfoVO account(String account) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setAccount(account);
        return userInfoVO;
    }

    /**
     * 帐户名和昵称唯一
     * @param account
     * @param nickname
     * @return
     */
    public UserInfoVO account(Integer id, String account, String nickname) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(id);
        vo.setAccount(account);
        vo.setNickname(nickname);
        return vo;
    }

    /**
     * 昵称
     * @param nickname
     * @return
     */
    public UserInfoVO nickname(String nickname) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setNickname(nickname);
        return userInfoVO;
    }

    /**
     * 手机号作为帐号查询参数
     * @param mobile
     * @return
     */
    public UserInfoVO mobile(String mobile) {

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setMobile(mobile);
        userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
        userInfoVO.setIsMobileLogin(UserConstants.IS_TRUE);
        return userInfoVO;
    }

    /**
     * 邮箱地址作为帐号查询参数
     * @param email
     * @return
     */
    public UserInfoVO email(String email) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setEmail(email);
        userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
        userInfoVO.setIsEmailLogin(UserConstants.IS_TRUE);
        return userInfoVO;
    }

    public UserInfoVO fdMobile(PassportVO passportVO){
        UserInfoVO userInfoVO = new UserInfoVO();
        if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
            userInfoVO.setIsMobileLogin(UserConstants.IS_TRUE);
            userInfoVO.setMobile(passportVO.getUserName());
            userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
        } else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)) {
            userInfoVO.setEmail(passportVO.getUserName());
            userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
            userInfoVO.setIsEmailLogin(UserConstants.IS_TRUE);
            userInfoVO.setMobile(passportVO.getMobile());
            userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
        } else {
            userInfoVO.setAccount(passportVO.getUserName());
            userInfoVO.setMobile(passportVO.getMobile());
            userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
        }
        return userInfoVO;
    }

    public UserInfoVO fdEmail(PassportVO passportVO) {
        UserInfoVO userInfoVO = new UserInfoVO();
        if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_MOBILE)) {
            userInfoVO.setIsMobileLogin(UserConstants.IS_TRUE);
            userInfoVO.setMobile(passportVO.getUserName());
            userInfoVO.setMobileStatus(UserConstants.IS_TRUE);
            userInfoVO.setEmail(passportVO.getEmail());
            userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
        } else if (passportVO.getUserName().matches(RegularValidateUtil.REGULAR_EMAIL)) {
            userInfoVO.setEmail(passportVO.getUserName());
            userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
            userInfoVO.setIsEmailLogin(UserConstants.IS_TRUE);
        } else {
            userInfoVO.setAccount(passportVO.getUserName());
            userInfoVO.setEmail(passportVO.getEmail());
            userInfoVO.setEmailStatus(UserConstants.IS_TRUE);
        }
        return userInfoVO;
    }

    public ResultBO<?> smsSendType(PassportVO passportVO) {
        if (passportVO.getSendType().equals(UserConstants.MessageTypeEnum.REG_MSG.getKey())) {
            UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(mobile(passportVO.getUserName()));
            if (!ObjectUtil.isBlank(userInfoBO)) {
                return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
            }
        } else if(!passportVO.getSendType().equals(UserConstants.MessageTypeEnum.FAST_LOGIN_MSG.getKey())){
            UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(mobile(passportVO.getUserName()));
            if (ObjectUtil.isBlank(userInfoBO)) {
                return ResultBO.err(MessageCodeConstants.MOBILE_IS_NOT_FOUND_SERVICE);
            }
        }
        return ResultBO.ok();
    }

    public ResultBO<?> smsSendType(String mobile, Short sendType) {
        if (sendType.equals(UserConstants.MessageTypeEnum.REG_MSG.getKey())) {
            UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(mobile(mobile));
            if (!ObjectUtil.isBlank(userInfoBO)) {
                return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
            }
        } else if(!sendType.equals(UserConstants.MessageTypeEnum.FAST_LOGIN_MSG.getKey())){
            UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(mobile(mobile));
            if (ObjectUtil.isBlank(userInfoBO)) {
                return ResultBO.err(MessageCodeConstants.MOBILE_IS_NOT_FOUND_SERVICE);
            }
        }
        return ResultBO.ok();
    }

    public ResultBO<?> mailSendType(PassportVO passportVO) {
        if (passportVO.getSendType().equals(UserConstants.MessageTypeEnum.REG_MSG.getKey())) {
            UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(email(passportVO.getUserName()));
            if (!ObjectUtil.isBlank(userInfoBO)) {
                return ResultBO.err(MessageCodeConstants.EMAIL_IS_REGISTERED_SERVICE);
            }
        } else {
            UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(email(passportVO.getUserName()));
            if (ObjectUtil.isBlank(userInfoBO)) {
                return ResultBO.err(MessageCodeConstants.EMAIL_IS_NOT_FOUND_SERVICE);
            }
        }
        return ResultBO.ok();
    }

    public ResultBO<?> mailSendType(String email, Short sendType) {
        if (sendType.equals(UserConstants.MessageTypeEnum.REG_MSG.getKey())) {
            UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(email(email));
            if (!ObjectUtil.isBlank(userInfoBO)) {
                return ResultBO.err(MessageCodeConstants.EMAIL_IS_REGISTERED_SERVICE);
            }
        } else {
            UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(email(email));
            if (ObjectUtil.isBlank(userInfoBO)) {
                return ResultBO.err(MessageCodeConstants.EMAIL_IS_NOT_FOUND_SERVICE);
            }
        }
        return ResultBO.ok();
    }

    /**
     * 修改成功
     * @return
     */
    public ResultBO<?> resultBO (String message) {
        ResultBO<?> resultBO = new ResultBO<>();
        resultBO.setMessage(message);
        resultBO.setSuccess(UserConstants.IS_TRUE);
        resultBO.setErrorCode(MessageCodeConstants.TRUE);
        return resultBO;
    }

    /**
     * 添加日志
     * @param id
     * @param userAction
     * @param status
     * @param ip
     * @param logBefore
     * @param logAfter
     * @param remark
     */
    public void insertOperateLog(Integer id, Short userAction, Short status, String ip, String logBefore, String logAfter, String remark) {
        UserModifyLogPO userModifyLogPO = new UserModifyLogPO(id, userAction, status, ip, logBefore, logAfter, remark);
        userModifyLogPO.setProvince(queryProvinceForIp(ip));
        UserInfoLastLogBO userInfoLastLogBO = userSecurityDaoMapper.findLastLoginDetail(id);
        userSecurityDaoMapper.addModifyLog(userModifyLogPO);
        if(!ObjectUtil.isBlank(userInfoLastLogBO)&&ip!=null&&userInfoLastLogBO.getIp()!=null && !Objects.equals(userInfoLastLogBO.getIp(), ip)){
        	LOGGER.debug("发送ip跟换短信：备注："+remark+",历史ip:"+userInfoLastLogBO.getIp()+",最新ip:"+ip);
        	senNodeMsg(id, 11);
        }
    }
    
    private String queryProvinceForIp (String ip) {
        String str[] = ip.split("\\.");
        List<MUserIpBO> list = userSecurityDaoMapper.queryProvinceLike(str[0] + "." + str[1]);
        if(!ObjectUtil.isBlank(list)) {
            Long ipLong = processIp(ip);
            for (MUserIpBO ipBO : list) {
                //ENDIPNUM>=? AND STARTIPNUM<=? LIMIT ?
                if(new Long(ipBO.getStartipnum()) <= ipLong && new Long(ipBO.getEndipnum()) >= ipLong) {
                    return ipBO.getCountry();
                }
            }
        }
        return null;
    }
    
    private Long processIp (String ip) {
        String[] ipNode = ip.split("\\.");
        //14.215.177.37
        Long ipLong = Long.parseLong(ipNode[0]) * 256 * 256 * 256 + Long.parseLong(ipNode[1]) * 256 * 256 + Long.parseLong(ipNode[2]) * 256 + Long.parseLong(ipNode[3]);
        return ipLong;
    }

    /**
     * 添加登录成功日志
     * @param ip
     * @param userInfoBO
     * @throws Exception
     */
    public void inserLoginSuccessLog(String ip, UserInfoBO userInfoBO) {
        UserModifyLogPO userModifyLogPO = new UserModifyLogPO(userInfoBO.getId(), UserConstants.UserOperationEnum.LOGIN_SUCCESS.getKey() ,ip,UserConstants.IS_TRUE+"|"+ ""+"|"+""+"|"+ UserConstants.UserOperationEnum.LOGIN_SUCCESS.getValue(), null);
        userModifyLogPO.setProvince(queryProvinceForIp(ip));       
        UserInfoLastLogBO userInfoLastLogBO = userSecurityDaoMapper.findLastLoginDetail(userInfoBO.getId());
        userSecurityDaoMapper.addLoginLog(userModifyLogPO);
        if(!ObjectUtil.isBlank(userInfoLastLogBO)&&ip!=null&&userInfoLastLogBO.getIp()!=null && !Objects.equals(userInfoLastLogBO.getIp(), ip)){
        	LOGGER.debug("发送ip跟换短信：备注：登录成功 ,历史ip:"+userInfoLastLogBO.getIp()+",最新ip:"+ip);
        	senNodeMsg(userInfoBO.getId(),11);
        }
    }

    public void insertUserForbitLog(String ip, Integer userId) {
        UserModifyLogPO userModifyLogPO = new UserModifyLogPO(userId, UserConstants.UserOperationEnum.USER_IS_FORBIT.getKey(),UserConstants.IS_TRUE ,ip ,"" ,"",UserConstants.UserOperationEnum.USER_IS_FORBIT.getValue());
        userModifyLogPO.setProvince(queryProvinceForIp(ip));
        userSecurityDaoMapper.addModifyLog(userModifyLogPO);
        if(!ObjectUtil.isBlank(ip)){
            LOGGER.debug("发送ip跟换短信：备注：禁用账户 ,ip:"+ip);
            senNodeMsg(userId,19);
        }
    }


    /**
     * 添加修改密码日志
     * @param ip
     * @param userInfoBO
     * @throws Exception
     */
    @Async
    public void insertUpadatePwdLog(String ip, UserInfoBO userInfoBO) {
        //添加修改密码日志
        UserModifyLogPO userModifyLogPO = new UserModifyLogPO(userInfoBO.getId(), UserConstants.UserOperationEnum.PASSWORD_UPDATE_SUCCESS.getKey(),ip,UserConstants.IS_TRUE +"|"+ ""+"|"+""+"|"+ UserConstants.UserOperationEnum.PASSWORD_UPDATE_SUCCESS.getValue(), null);
        userModifyLogPO.setProvince(queryProvinceForIp(ip));
        userSecurityDaoMapper.addLoginLog(userModifyLogPO);
    }

    /**
     * 获取头像url
     * @param userInfoBO
     * @param resultInfoBO
     */
    public void setHeadUrl(UserInfoBO userInfoBO, UserInfoBO resultInfoBO) {
        if (!ObjectUtil.isBlank(userInfoBO.getHeadUrl())) {
            if (userInfoBO.getHeadUrl().contains("http")) {
                resultInfoBO.setHeadUrl(userInfoBO.getHeadUrl());
            } else {
                resultInfoBO.setHeadUrl(before_file_url+userInfoBO.getHeadUrl());
            }
        }
    }
    
    /**
	 * 会员ip变更发送消息通知用户
	 * @param UserId
	 */
	public void senNodeMsg(Integer UserId, Integer nodeId){
        MessageModel model = new  MessageModel();
        OperateNodeMsg bodyMsg = new OperateNodeMsg();
        model.setKey(Constants.MSG_NODE_RESEND);
        model.setMessageSource("user_core");
        bodyMsg.setNodeId(nodeId);
        bodyMsg.setNodeData(""+UserId);
        model.setMessage(bodyMsg);
        messageProvider.sendMessage(Constants.QUEUE_NAME_MSG_QUEUE, model);
	}

    public ResultBO<?> checkKeyword(String username){
        if(ObjectUtil.isBlank(username)){
            return ResultBO.err(MessageCodeConstants.USERNAME_IS_NULL_FIELD);
        }
        List<KeywordBO> keywordBOs = redisUtil.getObj(CacheConstants.C_CORE_ACCOUNT_KEYWORD, new ArrayList<KeywordBO>());
        if(ObjectUtil.isBlank(keywordBOs)){
            keywordBOs = keywordMapper.queryKeywordInfo();
            redisUtil.addObj(CacheConstants.C_CORE_ACCOUNT_KEYWORD, keywordBOs, CacheConstants.TWELVE_HOURS);
            if(ObjectUtil.isBlank(keywordBOs)){
                return ResultBO.ok();
            }
        }
        for (KeywordBO keyword : keywordBOs) {
            if(username.indexOf(keyword.getKeyword())>=0){
                return ResultBO.err(MessageCodeConstants.USERNAME_IS_REGISTERED_SERVICE);
            }
        }
        return ResultBO.ok();
    }
    
    public String getNickname(String nickName) {
    	//昵称敏感词验证, 放最上
        ResultBO<?> checkKeyword = checkKeyword(nickName);
        if (checkKeyword.isError()) {
            return getRadomName(null);
        }
        //昵称规则验证
        ResultBO<?> validateNickname = ValidateUtil.validateNickname(nickName);
        if (validateNickname.isError()) {
            char[] chars = nickName.toCharArray();
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < nickName.length(); i++) {
                String str = String.valueOf(chars[i]);
                if (str.matches(RegularValidateUtil.REGULAR_NICKNAME)) {
                    stringBuffer.append(str);
                }
            }
            String nickname = stringBuffer.toString().trim();
            validateNickname = ValidateUtil.validateNickname(nickname);

            if (validateNickname.isError()) {
                return getRadomName(null);
            } else {
                nickName = nickname;
            }
        }
        
        //昵称重复递归验证
        nickName = getRadomName(nickName);

        return nickName;
    }    

    public String getRadomName(String name) {
    	if(ObjectUtil.isBlank(name))
    		name = EncryptUtil.getRandomString(UserConstants.TEN);
    	 int userCount = userInfoDaoMapper.findUserInfoByAccount(account(null, name, name));
        if(userCount > 0) {
            name = EncryptUtil.getRandomString(UserConstants.TEN);
            name = getRadomName(name);
        }
        return name;
    }

    /**
     *
     * @param token
     * @param tokenInfo
     * @param platform
     */
    public void modifyCache(String token, UserInfoBO tokenInfo, Short platform) {
        if (!ObjectUtil.isBlank(platform) && (platform.equals(UserConstants.PlatformEnum.PLATFORM_IOS.getKey()) ||
                platform.equals(UserConstants.PlatformEnum.PLATFORM_ANDROID.getKey()))) {
        	userUtil.addUserCacheByToken(token, tokenInfo, CacheConstants.ONE_WEEK);
        } else {
        	userUtil.addUserCacheByToken(token, tokenInfo, CacheConstants.ONE_DAY);
        }
    }
    
    public void setAgentInfo(UserInfoBO bo){
		AgentInfoPO agentInfoPO = agentInfoDaoMapper.findByUserId(bo.getId());
		if(ObjectUtil.isBlank(agentInfoPO)){
			bo.setIsAgent((short)0);
		}else{
			if(agentInfoPO.getAgentStatus() == 0){
				bo.setIsAgent((short)2);
			}else{
				bo.setIsAgent((short)1);
			}
		}    	
    }
}
