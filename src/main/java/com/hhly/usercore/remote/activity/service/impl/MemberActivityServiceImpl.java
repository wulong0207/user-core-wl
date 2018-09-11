package com.hhly.usercore.remote.activity.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserResultBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserActivityVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.pay.dao.UserWalletMapper;
import com.hhly.usercore.persistence.pay.po.UserWalletPO;
import com.hhly.usercore.remote.activity.service.IMemberActivityService;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;
import com.hhly.usercore.remote.passport.service.IMemberLoginService;

/**
 * @author zhouyang
 * @version 1.0
 * @desc 活动页面用户管理
 * @date 2017/8/8
 * @company 益彩网络科技公司
 */
@Service("iMemberActivityService")
public class MemberActivityServiceImpl implements IMemberActivityService {

    @Autowired
    private IVerifyCodeService verifyCodeService;

    @Autowired
    private UserInfoDaoMapper userInfoDaoMapper;

    @Autowired
    private UserWalletMapper userWalletMapper;

    @Autowired
    private IMemberLoginService memberLoginService;

    @Resource
    private PublicMethod publicMethod;

    @Resource
    private RedisUtil redisUtil;
    
	@Autowired
	private UserUtil userUtil;

    @Override
    public ResultBO<?> regFromActivity(PassportVO passportVO) {
        if (ObjectUtil.isBlank(passportVO)) {
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        }
        ResultBO<?> validateMobile = ValidateUtil.validateMobile(passportVO.getUserName());
        if (validateMobile.isError()) {
            return validateMobile;
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setMobile(passportVO.getUserName());
        UserInfoBO bo = userInfoDaoMapper.findUserInfo(userInfoVO);
        if (!ObjectUtil.isBlank(bo)) {
            return ResultBO.err(MessageCodeConstants.MOBILE_IS_REGISTERED_SERVICE);
        }
        UserInfoPO userInfoPO = new UserInfoPO(UserConstants.IS_TRUE, UserConstants.IS_TRUE, UserConstants.IS_FALSE,
                UserConstants.IS_FALSE, UserConstants.IS_TRUE, UserConstants.IS_FALSE);
        userInfoPO.setMobile(passportVO.getUserName());
        userInfoPO.setChannelId(passportVO.getChannelId());
        userInfoPO.setPlatform(passportVO.getPlatform());
        userInfoPO.setUserType(UserConstants.UserTypeEnum.USER.getValue());
        // 新增插入 代理系统编码
        if (!ObjectUtil.isBlank(passportVO.getAgentCode())) {
            userInfoPO.setAgentCode(passportVO.getAgentCode());
        }
        if (!ObjectUtil.isBlank(passportVO.getAccount())) {
            ResultBO<?> validateAccount = ValidateUtil.validateAccount(passportVO.getAccount());
            if (validateAccount.isError()) {
                return validateAccount;
            }
            ResultBO<?> checkKeyword = publicMethod.checkKeyword(passportVO.getAccount());
            if (checkKeyword.isError()) {
                return checkKeyword;
            }
            int userCount = userInfoDaoMapper.findUserInfoByAccount(publicMethod.account(null, passportVO.getAccount(), passportVO.getAccount()));
            // 验证用户名是否已存在
            if (userCount > UserConstants.ZERO_INTEGER) {
                return ResultBO.err(MessageCodeConstants.USERNAME_IS_REGISTERED_SERVICE);
            }
            userInfoPO.setAccount(passportVO.getAccount());
            userInfoPO.setNickName(passportVO.getAccount());
        } else {
            String nickname = publicMethod.getRadomName(null);
            userInfoPO.setAccount(nickname);
            userInfoPO.setNickName(nickname);
        }
        if (!ObjectUtil.isBlank(passportVO.getPassword1())) {
            try {
                ResultBO<?> validatePassword = ValidateUtil.validatePassword(passportVO.getPassword1());
                if (validatePassword.isError()) {
                    return validatePassword;
                }
                if (!passportVO.getPassword1().equals(passportVO.getPassword2())) {
                    return ResultBO.err(MessageCodeConstants.ENTERED_PASSWORDS_DIFFER_FIELD);
                }
                String rCode = EncryptUtil.getSalt();
                String password = EncryptUtil.encrypt(passportVO.getPassword1(), rCode);
                userInfoPO.setPassword(password);
                userInfoPO.setrCode(rCode);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        ResultBO<?> resultBO = verifyCodeService.checkVerifyCode(passportVO); // 验证验证码，绑定手机号
        if (resultBO.isError()) {
            return resultBO;
        }
        userInfoDaoMapper.addUser(userInfoPO);
        // 添加日志
        publicMethod.insertOperateLog(userInfoPO.getId(), UserConstants.UserOperationEnum.REGISTER_SUCCESS.getKey(),
                UserConstants.IS_TRUE, passportVO.getIp(), null, passportVO.getUserName(), UserConstants.UserOperationEnum.REGISTER_SUCCESS.getValue());
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.mobile(passportVO.getUserName()));
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        // 添加登录类型
        userInfoBO.setLoginType(UserConstants.LoginTypeEnum.MOBILE.getKey());
        UserWalletPO userWalletPO = new UserWalletPO(userInfoBO.getId(), UserConstants.ZERO, UserConstants.ZERO,
                UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.IS_TRUE,
                UserConstants.ZERO, 1);
        userWalletMapper.addUserWallet(userWalletPO);
        userInfoBO.setPlatform(passportVO.getPlatform());
        // 注册成功后，自动登录
        userInfoBO.setIp(passportVO.getIp());
        return memberLoginService.loginUserAutomation(userInfoBO);
    }

    @Override
    public ResultBO<?> verifyPerfectInfo(String token) {

        ResultBO<?> validateToken = ValidateUtil.validateToken(token);
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO tokenInfo = userUtil.getUserByToken(token);
        if (ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        //因缓存数据无法即时更新，从库里再查一次
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoToCache(publicMethod.id(tokenInfo.getId()));
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        UserResultBO userResultBO = new UserResultBO();
        if (ObjectUtil.isBlank(userInfoBO.getMobile()) || userInfoBO.getMobileStatus().equals(UserConstants.IS_FALSE)) {
            userResultBO.setStatus(UserConstants.IS_FALSE);
            return ResultBO.err(MessageCodeConstants.ACCOUNT_NOT_BIND_MOBILE, userResultBO, null);
        }
        if (ObjectUtil.isBlank(userInfoBO.getRealName()) || ObjectUtil.isBlank(userInfoBO.getIdCard())) {
            userResultBO.setStatus(UserConstants.IS_TRUE);
            return ResultBO.err(MessageCodeConstants.ACCOUNT_NOT_REALNAME_AUTHENTICTION_SERVICE, userResultBO, null);
        }
        return ResultBO.ok(userInfoBO);
    }

    @Override
    public ResultBO<?> findUserList(UserActivityVO userActivityVO) {
        if (ObjectUtil.isBlank(userActivityVO)) {
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        if (!ObjectUtil.isBlank(userActivityVO.getQueryField()) && userActivityVO.getType().equals(UserConstants.SendTypeEnum.MOBLE.getKey())) {
            userInfoVO.setMobile(userActivityVO.getQueryField());
        } else if (!ObjectUtil.isBlank(userActivityVO.getQueryField()) && userActivityVO.getType().equals(UserConstants.SendTypeEnum.ID_CARD.getKey())) {
            userInfoVO.setIdCard(userActivityVO.getQueryField());
        } else if (!ObjectUtil.isBlank(userActivityVO.getQueryField()) && userActivityVO.getType().equals(UserConstants.SendTypeEnum.REAL_NAME.getKey())) {
            userInfoVO.setRealName(userActivityVO.getQueryField());
        } else {
            return ResultBO.err(MessageCodeConstants.SEND_TYPE_ERROR);
        }
        List<Integer> userList = userInfoDaoMapper.findUserIdList(userInfoVO);
        return ResultBO.ok(userList);
    }

    @Override
    public ResultBO<?> checkAccount(PassportVO passportVO) {
        if (ObjectUtil.isBlank(passportVO)) {
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        }
        ResultBO<?> validateAccount = ValidateUtil.validateAccount(passportVO.getAccount());
        if (validateAccount.isError()) {
            return validateAccount;
        }
        ResultBO<?> checkKeyword = publicMethod.checkKeyword(passportVO.getAccount());
        if (checkKeyword.isError()) {
            return checkKeyword;
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setNickname(passportVO.getAccount());
        userInfoVO.setAccount(passportVO.getAccount());
        int userCount = userInfoDaoMapper.findUserInfoByAccount(userInfoVO);
        if (userCount > UserConstants.ZERO_INTEGER) {
            return ResultBO.err(MessageCodeConstants.USERNAME_IS_REGISTERED_SERVICE);
        }
        return ResultBO.ok();
    }
}
