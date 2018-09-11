package com.hhly.usercore.base.utils;

import java.net.URLDecoder;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.user.bo.UserMessageBO;
import com.hhly.skeleton.user.vo.UserMessageVO;
import com.hhly.usercore.base.common.service.SmsService;
import com.hhly.usercore.persistence.member.dao.VerifyCodeDaoMapper;
import com.hhly.usercore.persistence.message.po.UserMessagePO;

/**
 * @version 1.0
 * @auth chenkangning
 * @date 2017/5/24
 * @desc 发送短信
 * @compay 益彩网络科技有限公司
 */
@Component
public class SendCodeUtil {
    
    @Resource
    private RedisUtil redisUtil;
    
    @Resource
    private VerifyCodeDaoMapper verifyCodeDaoMapper;
    
    @Autowired
    SmsService smsService;


    private String getCode(UserMessageBO userMessageBO) throws Exception {
        String code;
        if (!ObjectUtil.isBlank(userMessageBO)) {
            if (DateUtil.getDifferenceTime(new Date(), userMessageBO.getCreateTime()) < CacheConstants.TEN_MINUTES_TO_MILLISECOND) {
                code = userMessageBO.getCode();
            } else {
                code = EncryptUtil.getRandomCode6();
            }
        } else {
            code = EncryptUtil.getRandomCode6();
        }
        return code;
    }
    
    /**
     * 发送验证码<br>
     * 1.验证1分钟条件
     * 2.验证是否超过每日最大条数<br>
     * 3.发送验证码<br>
     * 4.写入数据库<br>
     * 5.添加redis缓存<br>
     *
     * @param mobile   手机号
     * @param sendType 发送类型ss
     * @return ResultBO
     * @throws Exception 异常
     */
    public ResultBO<?> sendCodeEntrance(String mobile, Short sendType, UserMessageBO userMessageBO, Integer userId) throws Exception {
        String oneMinutekey = CacheConstants.getMinuteKey(mobile, sendType);
        
        //防止一分钟重复发送短信key
        ResultBO<?> resultBO = validateOneMinute(oneMinutekey);
        if(!ObjectUtil.isBlank(resultBO)) {
            return resultBO;
        }
        //2.验证是否超过每日最大条数
        resultBO = validateCodeConut(mobile, sendType);
        if(!ObjectUtil.isBlank(resultBO)) {
            return resultBO;
        }

        String code = getCode(userMessageBO);
        //一分钟之类，同一手机同一类型只能发送一条短信,如果前面通过验证了，这里添加到redis里面，设置一分钟有效期
        redisUtil.addString(oneMinutekey, code, CacheConstants.ONE_MINUTES);
        String content = processMobilesContent(code);
        //发送短信
        UserMessageVO vo = new UserMessageVO();
        vo.setAccount(mobile);
        vo.setMessage(content);
        boolean result = smsService.doSendSms(vo);
        if(result) {
            //发送成功
            //1.写入数据库
            insertDatabase(userId, mobile, code, content, sendType, UserConstants.VerifyCodeTypeEnum.SMS.getKey());
            //2.存入redis缓存，有效期15分钟
            redisUtil.addString(mobile + sendType.toString(), code, CacheConstants.FIFTEEN_MINUTES);
        }
        return null;
    }
    
    private ResultBO<?> validateOneMinute (String oneMinutekey) {
        String oneMinuteValue = redisUtil.getString(oneMinutekey);
        if(!StringUtils.isBlank(oneMinuteValue)) {
            //一分钟之类，同一手机同一类型只能发送一条短信
            return ResultBO.err(MessageCodeConstants.ONE_MINUTE_TIPS);
        }
        return null;
    }
    
    /**
     * 得到当前日期,默认yyyyMMdd
     *
     * @return string
     */
    public String getNowDate () {
        return getNowDate("yyyyMMdd");
    }
    
    /**
     * 得到当前日期
     *
     * @param style 如yyyyMMdd
     * @return 按style格式化的日期
     */
    private String getNowDate (String style) {
        return DateFormatUtils.format(new Date(), style);
    }
    
    /**
     * 验证发送验证码条数
     * 如果大于发送条件的话返回提示
     *
     * @param mobile   手机号
     * @param sendType 发送类型
     * @return null | ResultBO
     */
    private ResultBO<?> validateCodeConut (String mobile, Short sendType) {
        //用来计数的key
        String key = getNowDate() + mobile + sendType;
        //redisUtil.delString(key);
        Long count = redisUtil.incr(key, 1);
        System.out.println("当天发送条数：" + count);
        // 验证短信发送次数是否超出
        if(count > UserConstants.SEND_MAX) {
            return ResultBO.err(MessageCodeConstants.THE_VERIFYCODE_SEND_MAX_IS_TEN_FIELD);
        }
        Long expireTime = (DateUtil.getDayEnd().getTime() - new Date().getTime()) / 1000;
        redisUtil.expire(key, expireTime);
        return null;
    }
    
    /**
     * 写入数据库
     *
     * @param mobile   手机号
     * @param code     验证码
     * @param content  内容
     * @param sendType 短信类型,如登录，注册
     * @param type     数据类型，如：短信|邮件
     * @throws Exception 异常
     */
    private void insertDatabase (Integer userId, String mobile, String code, String content, Short sendType, Short type) throws Exception {
        UserMessagePO userMessagePO = new UserMessagePO();
        userMessagePO.setUserId(userId);
        userMessagePO.setCode(code);
        userMessagePO.setAccount(mobile);
        userMessagePO.setMessage(URLDecoder.decode(content, UserConstants.UTF_8));
        userMessagePO.setStatus(UserConstants.IS_FALSE);
        userMessagePO.setMessageType(sendType);
        userMessagePO.setType(type);
        verifyCodeDaoMapper.addVerifyCode(userMessagePO);
    }
    
    /**
     * 拼装短信内容
     *
     * @param code 验证码
     * @return 拼好后的短信内容
     * @throws Exception 异常
     */
    private String processMobilesContent (String code) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(UserConstants.VERIFYCODE);
        stringBuffer.append(code);
        stringBuffer.append(UserConstants.VALIDATE_MOBILE);
        stringBuffer.append(UserConstants.TWONCAI);
        return stringBuffer.toString();
    }
}
