package com.hhly.usercore.remote.member.service.impl;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.pay.bo.AddBankCardFirsBO;
import com.hhly.skeleton.pay.bo.PayBankBO;
import com.hhly.skeleton.pay.bo.PayBankSegmentBO;
import com.hhly.skeleton.pay.bo.PayBankcardBO;
import com.hhly.skeleton.pay.vo.PayBankcardVO;
import com.hhly.skeleton.user.bo.SendSmsCountBO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserMessageBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.skeleton.user.vo.UserMessageVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.common.service.BankcardSegmentService;
import com.hhly.usercore.base.common.service.SmsService;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.SendCodeUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.local.bank.service.PayBankService;
import com.hhly.usercore.persistence.member.dao.BankcardMapper;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.dao.VerifyCodeDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.message.po.UserMessagePO;
import com.hhly.usercore.persistence.pay.po.PayBankcardPO;
import com.hhly.usercore.persistence.security.po.UserModifyLogPO;
import com.hhly.usercore.remote.member.service.IMemberBankcardV11Service;
import com.hhly.usercore.remote.member.service.IMemberSecurityService;
import com.hhly.usercore.remote.passport.service.IMemberRegisterService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.*;

/**
 * @author zhouyang
 * @version 1.1
 * @desc
 * @date 2018/6/23
 * @company 益彩网络科技公司
 */
@Service("iMemberBankcardV11Service")
public class MemberBankcardV11ServiceImpl implements IMemberBankcardV11Service {

    private static final Logger logger = Logger.getLogger(MemberBankcardV11ServiceImpl.class);


    @Autowired
    private BankcardMapper bankcardMapper;

    @Autowired
    private VerifyCodeDaoMapper verifyCodeDaoMapper;

    @Autowired
    private UserInfoDaoMapper userInfoDaoMapper;

    @Resource
    private BankcardSegmentService bankcardSegmentService;

    @Autowired
    private IMemberRegisterService memberRegisterService;

    @Resource
    private PayBankService payBankService;

    @Autowired
    private IMemberSecurityService memberSecurityService;

    @Autowired
    private UserUtil userUtil;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private PublicMethod publicMethod;

    @Autowired
    private SmsService smsService;

    @Value("${before_file_url}")
    private String before_file_url;

    @Override
    public ResultBO<?> getBankName(PayBankcardVO vo) {
        String cardCode = vo.getCardcode();
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (userInfoBO == null) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        // 判断此用户是否已经添加过此张银行卡
        List<?> list = bankcardMapper.selectByUserIdAndCardCodeIsExist(userInfoBO.getId().toString(), cardCode);

        if(!ObjectUtil.isBlank(list)) {
            return ResultBO.err(MessageCodeConstants.IS_USER_BANKCARD_EXIST_SERVICE);
        }
        AddBankCardFirsBO addBankCardFirsBO = new AddBankCardFirsBO();
        addBankCardFirsBO.setCardCode(cardCode);
        addBankCardFirsBO.setUserName(StringUtil.hideString(userInfoBO.getRealName(), (short) 5));
        addBankCardFirsBO.setMobile(userInfoBO.getMobile());
        String idCard = userInfoBO.getIdCard();
        if (!StringUtil.isBlank(idCard)) {
            addBankCardFirsBO.setIdCard(StringUtil.hideString(idCard, (short) 1));
        }
        if (!ObjectUtil.isBlank(addBankCardFirsBO.getMobile())) {
            addBankCardFirsBO.setMobile(StringUtil.hideString(addBankCardFirsBO.getMobile(), (short)3));
        }

        ResultBO<?> resultBO = bankcardSegmentService.findPayBankSegmentByCard(cardCode);
        PayBankSegmentBO payBankSegmentBO;
        if (!ObjectUtil.isBlank(resultBO.getData())) {
            payBankSegmentBO = (PayBankSegmentBO) resultBO.getData();
            addBankCardFirsBO.setBankId(payBankSegmentBO.getBankId().toString());
            if (payBankSegmentBO.getBankId() != null) {
                PayBankBO payBankBO = payBankService.findBankById(payBankSegmentBO.getBankId());
                if (!ObjectUtil.isBlank(payBankBO)) {
                    addBankCardFirsBO.setBankName(payBankBO.getName());
                } else {
                    addBankCardFirsBO.setBankName(payBankSegmentBO.getBankName());
                }
                addBankCardFirsBO.setCardType(payBankSegmentBO.getCardType().intValue());
            }
            String slog = bankcardMapper.selectPaybank(payBankSegmentBO.getBankId());
            addBankCardFirsBO.setSlog(before_file_url + slog);
        }

        return resultBO.getSuccess() == ResultBO.getErr() ? resultBO : ResultBO.ok(addBankCardFirsBO);
    }

    @Override
    public ResultBO<?> getValidateCode(PayBankcardVO vo) throws Exception {
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (userInfoBO == null) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
                || (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
        }
        String mobile = vo.getMobile();
        if (!ObjectUtil.isBlank(mobile)) {
            ResultBO<?> resultBo = ValidateUtil.validateMobile(mobile);
            if (resultBo.isError()) {
                return resultBo;
            }
            if (ObjectUtil.isBlank(userInfoBO.getMobile())) {
                UserInfoVO userInfoVO = new UserInfoVO();
                userInfoVO.setMobile(vo.getMobile());
                UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
                if (!ObjectUtil.isBlank(bo)) {
                    return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
                }
            }
        } else {
            if (!ObjectUtil.isBlank(userInfoBO.getMobile())) {
                mobile = userInfoBO.getMobile();
            } else {
                return ResultBO.err(MessageCodeConstants.MOBILE_IS_NULL_FIELD);
            }
        }
        String oneMinuteValue = redisUtil.getString(CacheConstants.getMinuteKey(mobile, UserConstants.MessageTypeEnum.BIND_BANKCARD.getKey()));
        if(!ObjectUtil.isBlank(oneMinuteValue)) {
            //一分钟之类，同一手机同一类型只能发送一条短信
            return ResultBO.err(MessageCodeConstants.ONE_MINUTE_TIPS);
        }
        UserMessageVO messageVO = new UserMessageVO(mobile, UserConstants.MessageTypeEnum.BIND_BANKCARD.getKey(), UserConstants.IS_FALSE, UserConstants.VerifyCodeTypeEnum.SMS.getKey());
        UserMessageBO userMessageBO = verifyCodeDaoMapper.findPreviousCode(messageVO);
        String code = code(userMessageBO);
        String content = this.appendStr(code, UserConstants.MessageTypeEnum.BIND_BANKCARD.getValue());
        return sendSms(userInfoBO.getId(), mobile, code, content, UserConstants.MessageTypeEnum.BIND_BANKCARD.getKey());
    }

    @Override
    public ResultBO<?> addBankCard(PayBankcardVO vo) throws Exception {
        logger.debug("BankcardServiceImpl.addBankCard input params >>> " + vo.toString());

        PayBankcardPO bankcardPO = new PayBankcardPO();
        ResultBO<?> resultBO = validate(vo.getToken(), vo);
        if (resultBO != null) {
            return resultBO;
        }
        int row;


        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());

        ResultBO<?> resultBO1 = realNameAuth(vo.getToken(), vo, userInfoBO);
        if (resultBO1 != null) {
            return resultBO1;
        }
        //把其它银行卡设置为非默认
        updateDisableDefault(userInfoBO);
        // 保存银行卡信息
        //UserInfoBO dataUserInfo = userInfoDaoMapper.findUserIndexByUserId(userInfoBO.getId());
        BeanUtils.copyProperties(vo, bankcardPO);
        bankcardPO.setRealname("此字段暂无用");
        bankcardPO.setUserId(userInfoBO.getId());
        bankcardPO.setStatus(UserConstants.IS_TRUE.intValue());
        bankcardPO.setOpenbank(!Objects.equals(vo.getOpenbank(), UserConstants.IS_FALSE) ? UserConstants.IS_TRUE : vo.getOpenbank());
        bankcardPO.setIsdefault(UserConstants.IS_TRUE);
        row = bankcardMapper.addBankCard(bankcardPO);

        // 添加操作日志
        UserModifyLogPO userModifyLogPO = new UserModifyLogPO(bankcardPO.getUserId(), UserConstants.UserOperationEnum.ADD_BANKCARD.getKey(), UserConstants.IS_TRUE, bankcardPO.getIp(), null,
                bankcardPO.getBankname() + "-" + bankcardPO.getCardcode(), "移动端添加银行卡");
        memberSecurityService.addModifyLog(userModifyLogPO);
        if (row > 0) {
            updateUserBankInfo(bankcardPO);
            resetRedisUserInfo(vo, userInfoBO);
            clearUserBankCache(userInfoBO);// 清除用户的支付方式缓存
            return ResultBO.ok();
        }

        return ResultBO.err(MessageCodeConstants.HESSIAN_ERROR_SYS);
    }

    private ResultBO<?> validate(String token, PayBankcardVO payBankCardVO) throws Exception{
        UserInfoBO userInfoBO = userUtil.getUserByToken(token);
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE)
                || (!ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) && new Date().before(userInfoBO.getForbitEndTime()))) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
        }
        UserInfoBO userInfoBO1 = userInfoDaoMapper.findUserInfo(publicMethod.id(userInfoBO.getId()));
        if (ObjectUtil.isBlank(userInfoBO1)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        payBankCardVO.setUserid(userInfoBO.getId());
        // 判断银行id是否符合要求
        if (payBankCardVO.getBankid() != UserConstants.IS_FALSE.intValue()) {
            PayBankBO payBankBO = payBankService.findBankById(payBankCardVO.getBankid());
            if (ObjectUtil.isBlank(payBankBO)) {
                return ResultBO.err(MessageCodeConstants.INVALID_BANK_ID_ERROR);
            }
        } else {
            return ResultBO.err(MessageCodeConstants.INVALID_BANK_ID_ERROR);
        }

        // 银行卡名称为空
        if (StringUtil.isBlank(payBankCardVO.getBankname())) {
            return ResultBO.err(MessageCodeConstants.BANK_NAME_IS_NULL);
        }
        // 判断银行卡号是否为空
        if (StringUtil.isBlank(payBankCardVO.getCardcode())) {
            return ResultBO.err(MessageCodeConstants.CARD_CODE_IS_NULL_FIELD);
        }

        // 银行卡类型错误
        if (payBankCardVO.getBanktype() != UserConstants.BankCardType.DEPOSIT_CARD.getKey() && payBankCardVO.getBanktype() != UserConstants.BankCardType.CREDIT_CARD.getKey()) {
            return ResultBO.err(MessageCodeConstants.BANK_TYPE_IS_NULL_OR_ERROR);
        }

        if (StringUtil.isBlank(payBankCardVO.getRealname())) {
            return ResultBO.err(MessageCodeConstants.REALNAME_IS_NULL_FIELD);
        }
        //真实姓名为空
        if (!payBankCardVO.getRealname().contains("*")){
            //验证真实姓名
            ResultBO<?> validateRealName = ValidateUtil.validateRealName(payBankCardVO.getRealname());
            if (validateRealName.isError()) {
                return validateRealName;
            }
            //验证身份证号
            ResultBO<?> validateIdCard = ValidateUtil.validateIdCard(payBankCardVO.getIdCard());
            if (validateIdCard.isError()) {
                return validateIdCard;
            }
        }


        ResultBO<?> resultBO = validateCreditCard(payBankCardVO);
        if (resultBO != null) {
            return resultBO;
        }


        String mob ="";
        if (!ObjectUtil.isBlank(payBankCardVO.getMobile())) {
            ResultBO<?> resultBo = ValidateUtil.validateMobile(payBankCardVO.getMobile());
            if (resultBo.isError()) {
                return resultBo;
            }
            if (ObjectUtil.isBlank(userInfoBO.getMobile())) {
                UserInfoVO vo = new UserInfoVO();
                vo.setMobile(payBankCardVO.getMobile());
                UserInfoBO bo = userInfoDaoMapper.findUserInfo(vo);
                if (!ObjectUtil.isBlank(bo)) {
                    return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
                }
                mob = payBankCardVO.getMobile();
            }
        } else {
            if (!ObjectUtil.isBlank(userInfoBO.getMobile())) {
                payBankCardVO.setMobile(userInfoBO.getMobile());
            } else {
                return ResultBO.err(MessageCodeConstants.MOBILE_IS_NULL_FIELD);
            }
        }

        // 验证码校验
        if (StringUtil.isBlank(payBankCardVO.getCode())) {
            return ResultBO.err(MessageCodeConstants.VERIFYCODE_IS_NULL_FIELD);
        } else {
             /*if(!"123456".equals(payBankCardVO.getCode())) {
			    return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
			}*/
            Integer errCount = 0;
            //redisUtil.delString(userId+mobile+CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT);
            String countStr = redisUtil.getString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile());
            if (ObjectUtil.isBlank(countStr)) {
                countStr = "0";
            }
            errCount = Integer.valueOf(countStr);
            String redisCode = redisUtil.getString(payBankCardVO.getMobile() + UserConstants.MessageTypeEnum.BIND_BANKCARD.getKey());
            // 根据缓存判断验证码有效性
            if (ObjectUtil.isBlank(redisCode)) {
                return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
            }
            if (!payBankCardVO.getCode().equals(redisCode)) {
                errCount++;
                UserInfoPO infoPO = new UserInfoPO();
                infoPO.setId(userInfoBO.getId());
                if (errCount == 3) {
                    infoPO.setAccountStatus(UserConstants.IS_FALSE);
                    infoPO.setForbitEndTime(DateUtil.addHour(new Date(),1));
                    userInfoDaoMapper.updateUserInfo(infoPO);
                    redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile(), errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
                    if (!ObjectUtil.isBlank(userInfoBO)) {
                        userInfoBO.setAccountStatus(UserConstants.IS_FALSE);
                        publicMethod.modifyCache(payBankCardVO.getToken(), userInfoBO, payBankCardVO.getPlatform());
                    }
                    publicMethod.insertUserForbitLog(payBankCardVO.getIp(),userInfoBO.getId());
                    userUtil.clearUserToken(payBankCardVO.getToken());
                    userUtil.clearUserById(userInfoBO.getId());
                    return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_THREE);
                } else if (errCount == 8) {
                    infoPO.setAccountStatus(UserConstants.IS_FALSE);
                    infoPO.setForbitEndTime(DateUtil.addHour(new Date(),3));
                    userInfoDaoMapper.updateUserInfo(infoPO);
                    redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile(), errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
                    if (!ObjectUtil.isBlank(userInfoBO)) {
                        userInfoBO.setAccountStatus(UserConstants.IS_FALSE);
                        publicMethod.modifyCache(payBankCardVO.getToken(), userInfoBO, payBankCardVO.getPlatform());
                    }
                    publicMethod.insertUserForbitLog(payBankCardVO.getIp(),userInfoBO.getId());
                    userUtil.clearUserToken(payBankCardVO.getToken());
                    userUtil.clearUserById(userInfoBO.getId());
                    return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_EIGHT);
                } else if (errCount > 10) {
                    infoPO.setAccountStatus(UserConstants.IS_FALSE);
                    infoPO.setForbitEndTime(getTime());
                    userInfoDaoMapper.updateUserInfo(infoPO);
                    redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile(), errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
                    if (!ObjectUtil.isBlank(userInfoBO)) {
                        userInfoBO.setAccountStatus(UserConstants.IS_FALSE);
                        publicMethod.modifyCache(payBankCardVO.getToken(), userInfoBO, payBankCardVO.getPlatform());
                    }
                    publicMethod.insertUserForbitLog(payBankCardVO.getIp(),userInfoBO.getId());
                    userUtil.clearUserToken(payBankCardVO.getToken());
                    userUtil.clearUserById(userInfoBO.getId());
                    return ResultBO.err(MessageCodeConstants.VERIFY_ERR_COUNT_TEN);
                } else {
                    redisUtil.addString(CacheConstants.C_CORE_VERIFY_CODE_ERR_COUNT+userInfoBO.getId()+userInfoBO.getMobile(), errCount.toString(), DateUtil.compareAndGetSeconds(getTime(),new Date()));
                }
                return ResultBO.err(MessageCodeConstants.VERIFYCODE_ERROR_SERVICE);
            }
            UserMessagePO userMessagePO = new UserMessagePO();
            userMessagePO.setAccount(payBankCardVO.getMobile());
            userMessagePO.setCode(payBankCardVO.getCode());
            userMessagePO.setStatus(UserConstants.IS_TRUE);
            verifyCodeDaoMapper.updateVerifyCodeStatus(userMessagePO);
            //完成以上验证，且手机号码不存在，更新至数据库里
            if (!ObjectUtil.isBlank(mob)) {
                UserInfoPO po = new UserInfoPO();
                po.setMobile(mob);
                po.setMobileStatus(UserConstants.IS_TRUE);
                po.setIsMobileLogin(UserConstants.IS_TRUE);
                po.setId(userInfoBO.getId());
                userInfoDaoMapper.updateUserInfo(po);
                userInfoBO.setMobile(mob);
                userInfoBO.setMobileStatus(UserConstants.IS_TRUE);
                userInfoBO.setIsMobileLogin(UserConstants.IS_TRUE);
                publicMethod.modifyCache(token, userInfoBO,payBankCardVO.getPlatform());
            }
            // 验证完成，清除缓存
            redisUtil.delString(payBankCardVO.getMobile() + UserConstants.MessageTypeEnum.BIND_BANKCARD.getKey());

        }

        // 获取具体的银行信息
        ResultBO<?> bo = findPayBankByCard(payBankCardVO.getCardcode());
        if (bo.isError()) {
            return bo;
        }

        if (!ObjectUtil.isBlank(payBankCardVO.getIsdefault()) && payBankCardVO.getIsdefault().equals(UserConstants.IS_TRUE)) {
            // 如果有设置默认银行卡，校验是不是已经设置过，如果有，就返回提示
            List<PayBankcardBO> list = bankcardMapper.selectBankCard(payBankCardVO);
            if (!ObjectUtil.isBlank(list)) {
                return ResultBO.err(MessageCodeConstants.DEFAULT_BANK_CARD_EXIST_SERVICE);
            }
        }
        // 判断此用户是否已经添加过此张银行卡
        List<?> list = bankcardMapper.selectByUserIdAndCardCodeIsExist(userInfoBO.getId().toString(), payBankCardVO.getCardcode());
        if (!ObjectUtil.isBlank(list)) {
            return ResultBO.err(MessageCodeConstants.IS_USER_BANKCARD_EXIST_SERVICE);
        }
        return null;
    }

    /**
     * 实名认证
     *
     * @param token
     * @param payBankCardVO
     * @param userInfoBO
     * @return
     * @throws Exception
     */
    private ResultBO<?> realNameAuth(String token, PayBankcardVO payBankCardVO, UserInfoBO userInfoBO) throws Exception {
        ResultBO<?> resultBO;
        if (StringUtil.isBlank(userInfoBO.getIdCard()) && !payBankCardVO.getIdCard().contains("*")) {
            resultBO = ValidateUtil.validateIdCard(payBankCardVO.getIdCard());
            if (resultBO.isError()) {
                return resultBO;
            }
        }
        // 判断身份证是不是包含*号，包含的话表示已经做过实名认证，此处不处理
        if (StringUtil.isBlank(userInfoBO.getIdCard()) && !payBankCardVO.getIdCard().contains("*")) {
            resultBO = realNameAuthentication(payBankCardVO, userInfoBO);
            userInfoBO.setIdCard(payBankCardVO.getIdCard());
            userInfoBO.setRealName(payBankCardVO.getRealname());
            if (resultBO.isError()) {
                return resultBO;
            }
            publicMethod.modifyCache(token, userInfoBO, userInfoBO.getLoginPlatform());
        }
        return null;
    }

    private void updateDisableDefault (UserInfoBO userInfoBO) {
        //把其它银行卡设置为非默认
        PayBankcardPO payBankcardPO = new PayBankcardPO();
        payBankcardPO.setUserId(userInfoBO.getId());
        bankcardMapper.updateDisableDefault(payBankcardPO);
    }

    /**
     * 保存实名认证信息
     *
     * @param payBankCardVO
     * @param userInfoBO
     * @return
     * @throws Exception
     */
    private ResultBO<?> realNameAuthentication(PayBankcardVO payBankCardVO, UserInfoBO userInfoBO) throws Exception {
        // 实名认证
        PassportVO passportVO = new PassportVO();
        passportVO.setIdCard(payBankCardVO.getIdCard());
        passportVO.setRealName(payBankCardVO.getRealname());
        passportVO.setUserId(userInfoBO.getId());
        passportVO.setToken(payBankCardVO.getToken());
        passportVO.setIp(payBankCardVO.getIp());
        return memberRegisterService.perfectRealName(passportVO);
    }

    private void updateUserBankInfo(PayBankcardPO bankcardPO){
        UserInfoPO po = new UserInfoPO();
        po.setId(bankcardPO.getUserId());
        po.setUserPayId(bankcardPO.getBankid());
        po.setUserPayCardcode(bankcardPO.getCardcode());
        int num = userInfoDaoMapper.updateUserInfo(po);
        logger.info("用户 : " + po.getId() + "执行添加银行卡 - 修改用户银行卡信息操作 : " + num);
    }

    /**
     * 更新redis信息
     */
    private void resetRedisUserInfo (PayBankcardVO payBankcardVO, UserInfoBO userInfoBO) {
        userInfoBO.setValidatePass(UserConstants.IS_TRUE);
        publicMethod.modifyCache(payBankcardVO.getToken(), userInfoBO, userInfoBO.getLoginPlatform());
    }

    /**
     * 方法说明: 清除缓存
     * @auth: xiongJinGang
     * @param userInfoBO
     * @time: 2017年5月26日 下午3:53:00
     * @return: void
     */
    private void clearUserBankCache(UserInfoBO userInfoBO) {
        redisUtil.delAllString(CacheConstants.P_CORE_USER_PAY_CHANNEL + userInfoBO.getId());
        redisUtil.delAllString(CacheConstants.P_CORE_USER_BANK_CARD_LIST+ userInfoBO.getId());
        redisUtil.delAllString(CacheConstants.P_CORE_PAY_BANK_CHANNEL_SINGLE);
    }

    private ResultBO<?> findPayBankByCard(String bankCard) {
        // 比对银行卡信息
        // 1从缓存里面获取PayBankSegmentBO集合
        List<PayBankSegmentBO> payBankSegmentBOList = redisUtil.getObj(CacheConstants.PAY_BANK_SEGMENTBO_LIST_KEY, new ArrayList<PayBankSegmentBO>());
        PayBankSegmentBO bo = null;
        if(ObjectUtil.isBlank(payBankSegmentBOList)){
            payBankSegmentBOList = bankcardSegmentService.selectGroup();
        }
        for (PayBankSegmentBO payBankSegmentBO : payBankSegmentBOList) {
            if (!ObjectUtil.isBlank(payBankSegmentBO.getTopCut())) {
                // 先检索是不是符合xxxxx开头的
                if (bankCard.startsWith(String.valueOf(payBankSegmentBO.getTopCut()))) {
                    bo = payBankSegmentBO;
                    break;
                }
            }
        }
        // 如果对象为空，表示卡号不符合要求
        if (ObjectUtil.isBlank(bo)) {
            return ResultBO.err(MessageCodeConstants.BANKCARD_ERROR_SERVICE);
        } else if (bankCard.length() != bo.getCardLength()) {
            // 如果是以xxxx开头的，再检查卡号长度是不是一样
            return ResultBO.err(MessageCodeConstants.BANKCARD_ERROR_SERVICE);
        }
        return ResultBO.ok(bo);
    }

    /**
     * 1. 对卡号上的每位数字乘以权重。其规则是，如果卡号数字个数是偶数，则第一位乘以2，否则就乘以1，然后以后分别是,1,2,1,2,1,2;
     * <p>
     * 2. 如果每位数字乘以权重后超过9 ,则需要减去 9;
     * <p>
     * 3. 将所有的处理过的加权数字求和，用 数字 10 求模运算;
     * <p>
     * 4. 余数应该是0，否则可能是输入错误。也可能是一个假号。
     *
     * @param cardNo
     * @return
     */
    public static boolean validateCreditCard(String cardNo) {
        if (StringUtils.isBlank(cardNo) || !StringUtils.isNumeric(cardNo)) {
            return false;
        }
        int len = cardNo.length();
        boolean isOdd = len % 2 == 1;
        int total = 0;
        int tem = 0;
        for (int i = 0; i < len; i++) {
            tem = Integer.valueOf(String.valueOf(cardNo.charAt(i)));
            if (i == 0 && !isOdd) {

                tem = tem << 1;

            } else if (i > 0 && i % 2 == 0) {

                tem = tem << 1;

            }

            if (tem > 9) {
                tem = tem - 9;
            }
            total = total + tem;
        }
        return total % 10 == 0;
    }

    private static Date getTime(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 信用卡校验
     *
     * @param payBankcardVO
     * @return
     */
    private ResultBO<?> validateCreditCard(PayBankcardVO payBankcardVO) {
        // 如果是信用卡的话，
        if (!ObjectUtil.isBlank(payBankcardVO.getBanktype()) && Objects.equals(payBankcardVO.getBanktype(), Short.parseShort(UserConstants.BankCardType.CREDIT_CARD.getKey() + ""))) {
            // 1.校验银行卡号是否符合算法
            if (!validateCreditCard(payBankcardVO.getCardcode())) {
                return ResultBO.err(MessageCodeConstants.INVALID_CREDIT_CARD_ERROR);
            }
            // 2.信用卡有效期
            if (StringUtil.isBlank(payBankcardVO.getOverdue())) {
                return ResultBO.err(MessageCodeConstants.BANK_CARD_OVERDUE_IS_NULL_FIELD);
            } else {
                try {
                    if (!DateUtil.validateCredCardOver(payBankcardVO.getOverdue())) {
                        return ResultBO.err(MessageCodeConstants.CREDIT_CARD_EXPIRED);
                    }
                } catch (ParseException e) {
                    return ResultBO.err(MessageCodeConstants.INVALID_CREDIT_CARD_FORMAT);
                }
            }
            payBankcardVO.setBanktype(UserConstants.BankCardType.CREDIT_CARD.getKey());
        } else {
            payBankcardVO.setBanktype(UserConstants.BankCardType.DEPOSIT_CARD.getKey());
        }
        return null;
    }

    private String code(UserMessageBO userMessageBO) {
        String code = "";
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

    private ResultBO<?> sendSms(Integer userId, String userName, String code, String content, Short sendType) {
        //查询手机发送短信验证码条数
        UserMessageVO userMessageVO = new UserMessageVO(userName, code, content,
                sendType, UserConstants.VerifyCodeTypeEnum.SMS.getKey());
        int count = verifyCodeDaoMapper.findVerifyCodeCount(userMessageVO);
        //验证短信发送次数是否超出
        if (count > UserConstants.SEND_MAX) {
            return ResultBO.err(MessageCodeConstants.THE_VERIFYCODE_SEND_MAX_IS_TEN_FIELD);
        }
        boolean result = false;	//发送短信息请求
        try {
            result = smsService.doSendSms(userMessageVO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!result) {
            return ResultBO.err(MessageCodeConstants.SMS_SEND_DEFEAT_SYS);
        }
        //设置接口请求时间间隔，快速登录30s，其它时候60s
        long cacheTime = sendType.equals(UserConstants.MessageTypeEnum.FAST_LOGIN_MSG.getKey()) ? CacheConstants.THIRTY_SECONDS
                : CacheConstants.ONE_MINUTES;
        redisUtil.addString(CacheConstants.getMinuteKey(userName, sendType), code, cacheTime);
        UserMessagePO userMessagePO = null;
        try {
            userMessagePO = new UserMessagePO(userId, userName, code, URLDecoder.decode(content, UserConstants.UTF_8),
                    sendType, UserConstants.IS_FALSE, UserConstants.VerifyCodeTypeEnum.SMS.getKey());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        verifyCodeDaoMapper.addVerifyCode(userMessagePO);
        redisUtil.addString(userName+sendType, code,CacheConstants.FIFTEEN_MINUTES);
        SendSmsCountBO sendSmsCountBO = new SendSmsCountBO(count+1);
        return ResultBO.ok(sendSmsCountBO);
    }

    /**
     * 封装短信拼接
     * @param code	验证码
     * @return
     * @throws Exception
     */
    private String appendStr(String code, String operateType) {
        StringBuffer str = new  StringBuffer();
        str.append("您的验证码：");
        str.append(code);
        str.append("，您正在进行"+ operateType+"操作");
        str.append("，请勿向他人泄露");
        String msg = str.toString();
        return msg;
    }
}
