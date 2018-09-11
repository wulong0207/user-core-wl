package com.hhly.usercore.remote.passport.service.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.hhly.skeleton.base.constants.CacheConstants;
import org.apache.log4j.Logger;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.constants.UserConstants.LoginTypeEnum;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.TokenUtil;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.vo.TPInfoVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.local.channel.service.ChannelInfoService;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.operate.dao.OperateMarketChannelDaoMapper;
import com.hhly.usercore.persistence.pay.dao.UserWalletMapper;
import com.hhly.usercore.persistence.pay.po.UserWalletPO;
import com.hhly.usercore.remote.passport.service.IThirdPartyLoginService;

/**
 * @author zhouyang
 * @version 1.0
 * @desc 第三方登录接口实现类
 * @date 2017/5/4
 * @company 益彩网络科技公司
 */
@Service("iThirdPartyLoginService")
public class ThirdPartyLoginServiceImpl implements IThirdPartyLoginService {

    private static final Logger logger = Logger.getLogger(ThirdPartyLoginServiceImpl.class);

    @Autowired
    private UserInfoDaoMapper userInfoDaoMapper;

    @Autowired
    private UserWalletMapper userWalletMapper;
    
    @Autowired
    private OperateMarketChannelDaoMapper operateMarketChannelDaoMapper;

    @Resource
    private PublicMethod publicMethod;

	@Autowired
	private UserUtil userUtil;

	@Autowired
    private RedissonClient redissonClient;
	
	@Autowired
	ChannelInfoService channelInfoService;

    @Override
    public ResultBO<?> tpQQLogin(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        RLock openIdExchangelLock = null;
        try {
            openIdExchangelLock = redissonClient.getLock(getWechatOpenIdlLock(tpInfoVO.getOpenid()));
            lock(openIdExchangelLock);

            tpInfoVO.setHeadimgurl(tpInfoVO.getFigureurl_qq_2());
            UserInfoVO userInfoVO1 = new UserInfoVO();
            userInfoVO1.setQqOpenID(tpInfoVO.getOpenid());
            UserInfoPO userInfoPO = new UserInfoPO(UserConstants.IS_FALSE, UserConstants.IS_FALSE,
                    UserConstants.IS_FALSE, UserConstants.IS_FALSE, UserConstants.IS_TRUE, UserConstants.ModifyTypeEnum.NO_MODIFY.getKey());
            userInfoPO.setQqOpenID(tpInfoVO.getOpenid());
            userInfoPO.setQqName(filterEmoji(tpInfoVO.getNickname()));
            userInfoPO.setHeadUrl(tpInfoVO.getHeadimgurl());
            //生成正确的昵称
            String nickName = publicMethod.getNickname(tpInfoVO.getNickname());
            userInfoPO.setNickName(nickName);
            //快速注册用户
            bindUserInfo(userInfoPO, userInfoVO1, tpInfoVO, tpInfoVO.getIp());
            //自动登录
            return toCache(tpInfoVO, userInfoVO1, tpInfoVO.getIp());
        } finally {
            unlock(openIdExchangelLock);
        }
    }

    @Override
    public ResultBO<?> tpWXLogin(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        RLock openIdExchangelLock = null;
        try {
            openIdExchangelLock = redissonClient.getLock(getWechatOpenIdlLock(tpInfoVO.getOpenid()));
            lock(openIdExchangelLock);

            UserInfoVO userInfoVO1 = new UserInfoVO();
            userInfoVO1.setWechatOpenID(tpInfoVO.getOpenid());
            UserInfoPO userInfoPO = new UserInfoPO(UserConstants.IS_FALSE, UserConstants.IS_FALSE,
                    UserConstants.IS_FALSE, UserConstants.IS_FALSE, UserConstants.IS_TRUE, UserConstants.ModifyTypeEnum.NO_MODIFY.getKey());
            userInfoPO.setWechatOpenID(tpInfoVO.getOpenid());
            userInfoPO.setWechatName(filterEmoji(tpInfoVO.getNickname()));
            userInfoPO.setHeadUrl(tpInfoVO.getHeadimgurl());
            //生成正确的昵称
            String nickName = publicMethod.getNickname(tpInfoVO.getNickname());
            userInfoPO.setNickName(nickName);
            //快速注册用户
            bindUserInfo(userInfoPO, userInfoVO1, tpInfoVO, tpInfoVO.getIp());
            //自动登录
            return toCache(tpInfoVO, userInfoVO1, tpInfoVO.getIp());
        } finally {
            unlock(openIdExchangelLock);
        }
    }
    
    @Override
    public ResultBO<?> tpV11WXLogin(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        RLock openIdExchangelLock = null;
        try {
            openIdExchangelLock = redissonClient.getLock(getWechatOpenIdlLock(tpInfoVO.getUnionid()));
            lock(openIdExchangelLock);
            UserInfoVO userInfoVO1 = new UserInfoVO();
            userInfoVO1.setWechatOpenID(tpInfoVO.getUnionid());
            UserInfoPO userInfoPO = new UserInfoPO(UserConstants.IS_FALSE, UserConstants.IS_FALSE,
                    UserConstants.IS_FALSE, UserConstants.IS_FALSE, UserConstants.IS_TRUE, UserConstants.ModifyTypeEnum.NO_MODIFY.getKey());
            userInfoPO.setWechatOpenID(tpInfoVO.getUnionid());
            userInfoPO.setWechatName(filterEmoji(tpInfoVO.getNickname()));
            userInfoPO.setHeadUrl(tpInfoVO.getHeadimgurl());
            //生成正确的昵称
            String nickName = publicMethod.getNickname(tpInfoVO.getNickname());
            userInfoPO.setNickName(nickName);
            //快速注册用户
            bindUserInfo(userInfoPO, userInfoVO1, tpInfoVO, tpInfoVO.getIp());
            //添加至缓存，自动登录
            UserInfoBO userInfoToCache = userInfoDaoMapper.findUserInfo(userInfoVO1);
            String token = TokenUtil.createTokenStr(tpInfoVO.getPlatform());
            //添加token至对象中
            userInfoToCache.setToken(token);
            // add by cheng.chen 用于H5公众号登录
            userInfoToCache.setWechatOpenID(tpInfoVO.getOpenid());
            getUserIndexBO(userInfoToCache);
            userInfoToCache.setSafeIntegral(publicMethod.getSafeIntegration(userInfoToCache));
//        userInfoToCache.setLoginType(LoginTypeEnum.QQ.getKey());
            userInfoToCache.setFristRegister(userInfoVO1.getFristRegister());
            publicMethod.setHeadUrl(userInfoToCache, userInfoToCache);
            publicMethod.lastLogTime(userInfoToCache);
            if (!ObjectUtil.isBlank(userInfoToCache.getRealName()) && !ObjectUtil.isBlank(userInfoToCache.getIdCard())) {
                userInfoToCache.setAttestationRealName(UserConstants.IS_TRUE);
            } else {
                userInfoToCache.setAttestationRealName(UserConstants.IS_FALSE);
            }
            userInfoToCache.setLoginPlatform(tpInfoVO.getPlatform());
            publicMethod.modifyCache(token, userInfoToCache, tpInfoVO.getPlatform());

            //添加日志，登录成功
            publicMethod.inserLoginSuccessLog(tpInfoVO.getIp(), userInfoToCache);
            publicMethod.encryption(userInfoToCache, userInfoToCache);
            return ResultBO.ok(userInfoToCache);
        } finally {
            unlock(openIdExchangelLock);
        }
    }    

    @Override
    public ResultBO<?> tpWBLogin(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        RLock openIdExchangelLock = null;
        try {
            openIdExchangelLock = redissonClient.getLock(getWechatOpenIdlLock(tpInfoVO.getOpenid()));
            lock(openIdExchangelLock);

            UserInfoVO userInfoVO1 = new UserInfoVO();
            userInfoVO1.setSinaBlogOpenID(tpInfoVO.getOpenid());
            UserInfoPO userInfoPO = new UserInfoPO(UserConstants.IS_FALSE, UserConstants.IS_FALSE,
                    UserConstants.IS_FALSE, UserConstants.IS_FALSE, UserConstants.IS_TRUE, UserConstants.ModifyTypeEnum.NO_MODIFY.getKey());
            userInfoPO.setSinaBlogOpenID(tpInfoVO.getOpenid());
            userInfoPO.setSinaName(filterEmoji(tpInfoVO.getNickname()));
            userInfoPO.setHeadUrl(tpInfoVO.getHeadimgurl());
            //生成正确的昵称
            String nickName = publicMethod.getNickname(tpInfoVO.getNickname());
            userInfoPO.setNickName(nickName);
            //快速注册用户
            bindUserInfo(userInfoPO, userInfoVO1, tpInfoVO, tpInfoVO.getIp());
            //自动登录
            return toCache(tpInfoVO, userInfoVO1, tpInfoVO.getIp());
        } finally {
            unlock(openIdExchangelLock);
        }
    }

    @Override
    public ResultBO<?> tpChannelLogin(TPInfoVO tpInfoVO) {

        String channelOpenID = channelInfoService.getChannelOpenID(tpInfoVO.getChannelId(), tpInfoVO.getOpenid(), tpInfoVO.getOrderNum());
        tpInfoVO.setOpenid(channelOpenID);

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setChannelOpenID(channelOpenID);

        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO);
        if (ObjectUtil.isBlank(userInfoBO)) {
            Integer userId = addUser(tpInfoVO);
            userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.id(userId));
            userInfoBO.setFristRegister(UserConstants.IS_TRUE);
        }else{
        	userInfoBO.setFristRegister(UserConstants.IS_FALSE);
        }
        //设置会员其他属性
        getResult(userInfoBO, tpInfoVO);
        return ResultBO.ok(userInfoBO);
    }
    
    

    /**
     * 处理emoji表情，替换为空字符串
     *
     * @param source
     * @return
     */
    private static String filterEmoji(String source) {
        if (source != null) {
            Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                source = emojiMatcher.replaceAll("");
                return source;
            }
            return source;
        }
        return source;
    }

    @Override
    public ResultBO<?> bindQQ(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO))
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        ResultBO<?> validateToken = ValidateUtil.validateToken(tpInfoVO.getToken());
        if (validateToken.isError())
            return validateToken;
        UserInfoBO tokenInfo = userUtil.getUserByToken(tpInfoVO.getToken());
        if (ObjectUtil.isBlank(tokenInfo))
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        //判断帐号是否已绑定了QQ
        if (!ObjectUtil.isBlank(tokenInfo.getQqOpenID()))
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_BIND_QQ);
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setQqOpenID(tpInfoVO.getOpenid());
        UserInfoBO findOpenID = userInfoDaoMapper.findUserInfo(userInfoVO);
        if (!ObjectUtil.isBlank(findOpenID)) {
            return ResultBO.err(MessageCodeConstants.QQ_IS_BIND_ACCOUNT);
        }
        UserInfoPO userInfoPO = new UserInfoPO();
        //绑定openId
        userInfoPO.setId(tokenInfo.getId());
        userInfoPO.setQqOpenID(tpInfoVO.getOpenid());
        userInfoPO.setQqName(tpInfoVO.getNickname());
        userInfoDaoMapper.updateUserInfo(userInfoPO);
        // 添加日志
        publicMethod.insertOperateLog(tokenInfo.getId(), UserConstants.UserOperationEnum.BIND_QQOPENID_SUCCESS.getKey(),
                UserConstants.IS_TRUE, tpInfoVO.getIp(), null, tpInfoVO.getOpenid(), UserConstants.UserOperationEnum.BIND_QQOPENID_SUCCESS.getValue());
        tokenInfo.setQqOpenID(tpInfoVO.getOpenid());
        tokenInfo.setQqName(tpInfoVO.getNickname());
        publicMethod.modifyCache(tpInfoVO.getToken(), tokenInfo, tokenInfo.getPlatform());
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> bindWX(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO))
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        ResultBO<?> validateToken = ValidateUtil.validateToken(tpInfoVO.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO tokenInfo = userUtil.getUserByToken(tpInfoVO.getToken());
        if (ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        //判断帐号是否绑定了微信
        if (!ObjectUtil.isBlank(tokenInfo.getWechatUnionID())) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_BIND_WECHAT);
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setWechatOpenID(tpInfoVO.getOpenid());
        UserInfoBO findOpenID = userInfoDaoMapper.findUserInfo(userInfoVO);
        if (!ObjectUtil.isBlank(findOpenID)) {
            return ResultBO.err(MessageCodeConstants.WECHAT_IS_BIND_ACCOUNT);
        }
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setId(tokenInfo.getId());
        userInfoPO.setWechatOpenID(tpInfoVO.getOpenid());
        userInfoPO.setWechatName(tpInfoVO.getNickname());
        userInfoDaoMapper.updateUserInfo(userInfoPO);
        // 添加日志
        publicMethod.insertOperateLog(tokenInfo.getId(), UserConstants.UserOperationEnum.BIND_WECHATOPENID_SUCCESS.getKey(),
                UserConstants.IS_TRUE, tpInfoVO.getIp(), null, tpInfoVO.getOpenid(), UserConstants.UserOperationEnum.BIND_WECHATOPENID_SUCCESS.getValue());
        tokenInfo.setWechatUnionID(tpInfoVO.getOpenid());
        tokenInfo.setWechatName(tpInfoVO.getNickname());
        publicMethod.modifyCache(tpInfoVO.getToken(), tokenInfo, tokenInfo.getPlatform());
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> bindWB(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO))
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        ResultBO<?> validateToken = ValidateUtil.validateToken(tpInfoVO.getToken());
        if (validateToken.isError())
            return validateToken;
        UserInfoBO tokenInfo = userUtil.getUserByToken(tpInfoVO.getToken());
        if (ObjectUtil.isBlank(tokenInfo))
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        /*//判断帐号是否绑定了微博
        if (!ObjectUtil.isBlank(tokenInfo.getSinaBlogOpenID()))
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_BIND_WECHAT);*/
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setSinaBlogOpenID(tpInfoVO.getOpenid());
        UserInfoBO findOpenID = userInfoDaoMapper.findUserInfo(userInfoVO);
        if (!ObjectUtil.isBlank(findOpenID)) {
            return ResultBO.err(MessageCodeConstants.WEIBO_IS_BIND_ACCOUNT);
        }
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setId(tokenInfo.getId());
        userInfoPO.setSinaBlogOpenID(tpInfoVO.getOpenid());
        userInfoPO.setSinaName(tpInfoVO.getNickname());
        userInfoDaoMapper.updateUserInfo(userInfoPO);
        // 添加日志
        publicMethod.insertOperateLog(tokenInfo.getId(), UserConstants.UserOperationEnum.BIND_SINA_BLOG_OPENID_SUCCESS.getKey(),
                UserConstants.IS_TRUE, tpInfoVO.getIp(), null, tpInfoVO.getOpenid(), UserConstants.UserOperationEnum.BIND_SINA_BLOG_OPENID_SUCCESS.getValue());
        tokenInfo.setSinaBlogOpenID(tpInfoVO.getOpenid());
        tokenInfo.setSinaName(tpInfoVO.getNickname());
        publicMethod.modifyCache(tpInfoVO.getToken(), tokenInfo, tokenInfo.getPlatform());
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> removeQQ(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO))
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        ResultBO<?> validateToken = ValidateUtil.validateToken(tpInfoVO.getToken());
        if (validateToken.isError())
            return validateToken;
        UserInfoBO tokenInfo = userUtil.getUserByToken(tpInfoVO.getToken());
        if (ObjectUtil.isBlank(tokenInfo))
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        //判断帐号是否已绑定了QQ
        if (ObjectUtil.isBlank(tokenInfo.getQqOpenID())) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_BIND_QQ);
        }
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.id(tokenInfo.getId()));
        if (ObjectUtil.isBlank(userInfoBO.getPassword())
                && ObjectUtil.isBlank(userInfoBO.getWechatOpenID())
                && ObjectUtil.isBlank(userInfoBO.getSinaBlogOpenID())
                && userInfoBO.getIsEmailLogin().equals(UserConstants.IS_FALSE)
                && userInfoBO.getIsMobileLogin().equals(UserConstants.IS_FALSE)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_HVAE_NOT_NOTHING_LOGIN_METHOD);
        }
        UserInfoPO userInfoPO = new UserInfoPO();
        //绑定openId
        userInfoPO.setId(tokenInfo.getId());
        userInfoPO.setQqOpenID("");
        userInfoPO.setQqName("");
        userInfoDaoMapper.updateUserInfo(userInfoPO);
        // 添加日志
        publicMethod.insertOperateLog(tokenInfo.getId(), UserConstants.UserOperationEnum.REMOVE_QQ_BIND_SUCCESS.getKey(),
                UserConstants.IS_TRUE, tpInfoVO.getIp(), tokenInfo.getQqOpenID(), null, UserConstants.UserOperationEnum.REMOVE_QQ_BIND_SUCCESS.getValue());
        tokenInfo.setQqOpenID(null);
        tokenInfo.setQqName(null);
        publicMethod.modifyCache(tpInfoVO.getToken(), tokenInfo, tokenInfo.getPlatform());
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> removeWX(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO))
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        ResultBO<?> validateToken = ValidateUtil.validateToken(tpInfoVO.getToken());
        if (validateToken.isError())
            return validateToken;
        UserInfoBO tokenInfo = userUtil.getUserByToken(tpInfoVO.getToken());
        if (ObjectUtil.isBlank(tokenInfo))
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        //判断帐号是否绑定了微信
        if (ObjectUtil.isBlank(tokenInfo.getWechatUnionID())) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_BIND_WECHAT);
        }
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.id(tokenInfo.getId()));
        if (ObjectUtil.isBlank(userInfoBO.getPassword())
                && ObjectUtil.isBlank(userInfoBO.getQqOpenID())
                && ObjectUtil.isBlank(userInfoBO.getSinaBlogOpenID())
                && userInfoBO.getIsEmailLogin().equals(UserConstants.IS_FALSE)
                && userInfoBO.getIsMobileLogin().equals(UserConstants.IS_FALSE)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_HVAE_NOT_NOTHING_LOGIN_METHOD);
        }
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setId(tokenInfo.getId());
        userInfoPO.setWechatOpenID("");
        userInfoPO.setWechatName("");
        userInfoDaoMapper.updateUserInfo(userInfoPO);
        // 添加日志
        publicMethod.insertOperateLog(tokenInfo.getId(), UserConstants.UserOperationEnum.REMOVE_WECHAT_BIND_SUCCESS.getKey(),
                UserConstants.IS_TRUE, tpInfoVO.getIp(), tokenInfo.getWechatOpenID(), null, UserConstants.UserOperationEnum.REMOVE_WECHAT_BIND_SUCCESS.getValue());
        tokenInfo.setWechatUnionID(null);
        tokenInfo.setWechatName(null);
        publicMethod.modifyCache(tpInfoVO.getToken(), tokenInfo, tokenInfo.getPlatform());
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> removeWB(TPInfoVO tpInfoVO) {
        if (ObjectUtil.isBlank(tpInfoVO))
            return ResultBO.err(MessageCodeConstants.OBJECT_IS_NULL);
        ResultBO<?> validateToken = ValidateUtil.validateToken(tpInfoVO.getToken());
        if (validateToken.isError())
            return validateToken;
        UserInfoBO tokenInfo = userUtil.getUserByToken(tpInfoVO.getToken());
        if (ObjectUtil.isBlank(tokenInfo))
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        //判断帐号是否绑定了微信
        if (ObjectUtil.isBlank(tokenInfo.getSinaBlogOpenID())) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_BIND_WEIBO);
        }
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(publicMethod.id(tokenInfo.getId()));
        if (ObjectUtil.isBlank(userInfoBO.getPassword())
                && ObjectUtil.isBlank(userInfoBO.getQqOpenID())
                && ObjectUtil.isBlank(userInfoBO.getWechatOpenID())
                && userInfoBO.getIsEmailLogin().equals(UserConstants.IS_FALSE)
                && userInfoBO.getIsMobileLogin().equals(UserConstants.IS_FALSE)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_HVAE_NOT_NOTHING_LOGIN_METHOD);
        }
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setId(tokenInfo.getId());
        userInfoPO.setSinaBlogOpenID("");
        userInfoPO.setSinaName("");
        userInfoDaoMapper.updateUserInfo(userInfoPO);
        // 添加日志
        publicMethod.insertOperateLog(tokenInfo.getId(), UserConstants.UserOperationEnum.REMOVE_SINA_BLOG_OPENID_SUCCESS.getKey(),
                UserConstants.IS_TRUE, tpInfoVO.getIp(), tokenInfo.getSinaBlogOpenID(), null, UserConstants.UserOperationEnum.REMOVE_SINA_BLOG_OPENID_SUCCESS.getValue());
        tokenInfo.setSinaBlogOpenID(null);
        tokenInfo.setSinaName(null);
        publicMethod.modifyCache(tpInfoVO.getToken(), tokenInfo, tokenInfo.getPlatform());
        return ResultBO.ok();
    }

    private Integer addUser(TPInfoVO tpInfoVO) {
        UserInfoPO userInfoPO = new UserInfoPO();
        if(!ObjectUtil.isBlank(tpInfoVO.getAccountName()))
			userInfoPO.setAccount(publicMethod.getRadomName(tpInfoVO.getAccountName()));
		else
			userInfoPO.setAccount(publicMethod.getRadomName(null));
        if (!ObjectUtil.isBlank(tpInfoVO.getNickname()))
        	userInfoPO.setNickName(publicMethod.getNickname(tpInfoVO.getNickname()));
        else
        	userInfoPO.setNickName(userInfoPO.getAccount());
        if(!ObjectUtil.isBlank(tpInfoVO.getMobile())){
			UserInfoVO mobileVO = new UserInfoVO();
			mobileVO.setMobile(tpInfoVO.getMobile());
			int mobileCount = userInfoDaoMapper.findBindMECount(mobileVO);
			if(mobileCount == 0)
				userInfoPO.setMobile(tpInfoVO.getMobile());
        }
        if(!ObjectUtil.isBlank(userInfoPO.getMobile())){
	        userInfoPO.setMobileStatus(UserConstants.IS_TRUE);
	        userInfoPO.setIsMobileLogin(UserConstants.IS_TRUE);
        }else{
	        userInfoPO.setMobileStatus(UserConstants.IS_FALSE);
	        userInfoPO.setIsMobileLogin(UserConstants.IS_FALSE);
        }
        if (!ObjectUtil.isBlank(tpInfoVO.getHeadimgurl()))
            userInfoPO.setHeadUrl(tpInfoVO.getHeadimgurl());
        if (!ObjectUtil.isBlank(tpInfoVO.getSex()))
            userInfoPO.setSex(tpInfoVO.getSex());
        if (!ObjectUtil.isBlank(tpInfoVO.getProvince()) || !ObjectUtil.isBlank(tpInfoVO.getCity()))
            userInfoPO.setAddress(tpInfoVO.getProvince() + tpInfoVO.getCity());
        if (!ObjectUtil.isBlank(tpInfoVO.getOpenid())) {
            if (tpInfoVO.getType() == LoginTypeEnum.WECHAT.getKey()) {
                userInfoPO.setWechatOpenID(tpInfoVO.getOpenid());
                userInfoPO.setWechatName(tpInfoVO.getNickname());
            } else if (tpInfoVO.getType() == LoginTypeEnum.QQ.getKey()) {
                userInfoPO.setHeadUrl(tpInfoVO.getFigureurl_qq_2());
                userInfoPO.setQqOpenID(tpInfoVO.getOpenid());
                userInfoPO.setQqName(tpInfoVO.getNickname());
            } else if (tpInfoVO.getType() == LoginTypeEnum.CHANNEL.getKey()) {
                userInfoPO.setChannelOpenID(tpInfoVO.getOpenid());
            }
        }
        if (ObjectUtil.isBlank(tpInfoVO.getChannelId())) {
            userInfoPO.setChannelId(UserConstants.OTHER_CHANNEL);
        } else {
            userInfoPO.setChannelId(tpInfoVO.getChannelId());
        }
        userInfoPO.setAccountStatus(UserConstants.IS_TRUE);
        userInfoPO.setEmailStatus(UserConstants.IS_FALSE);
        userInfoPO.setIsEmailLogin(UserConstants.IS_FALSE);
        userInfoPO.setAccountModify(UserConstants.IS_FALSE);
        userInfoPO.setPlatform(tpInfoVO.getPlatform());
        //添加会员账户
        userInfoDaoMapper.addUser(userInfoPO);
        //用户id
        Integer userId = userInfoPO.getId();
        //添加会员钱包
        UserWalletPO userWalletPO = new UserWalletPO(userId, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.IS_TRUE, UserConstants.ZERO, 1);
        userWalletMapper.addUserWallet(userWalletPO);
        // 添加日志
        publicMethod.insertOperateLog(null, UserConstants.UserOperationEnum.REGISTER_SUCCESS.getKey(), UserConstants.IS_TRUE, tpInfoVO.getIp(), null, tpInfoVO.getOpenid(), UserConstants.UserOperationEnum.REGISTER_SUCCESS.getValue());
        return userId;
    }

    private UserInfoBO getResult(UserInfoBO userInfoBO, TPInfoVO tpInfoVO) {
        //添加至缓存，自动登录
        String token = TokenUtil.createTokenStr(tpInfoVO.getPlatform());
        //添加token至对象中
        userInfoBO.setToken(token);
        getUserIndexBO(userInfoBO);
        userInfoBO.setSafeIntegral(publicMethod.getSafeIntegration(userInfoBO));
        userInfoBO.setLoginType(LoginTypeEnum.WECHAT.getKey());
        if (!ObjectUtil.isBlank(userInfoBO.getRealName()) && !ObjectUtil.isBlank(userInfoBO.getIdCard())) {
            userInfoBO.setAttestationRealName(UserConstants.IS_TRUE);
        } else {
            userInfoBO.setAttestationRealName(UserConstants.IS_FALSE);
        }
        publicMethod.modifyCache(token, userInfoBO, tpInfoVO.getPlatform());
        //添加日志，登录成功
        publicMethod.inserLoginSuccessLog(tpInfoVO.getIp(), userInfoBO);
        return userInfoBO;
    }

    private UserInfoBO getUserIndexBO(UserInfoBO userInfoBO) {
        UserInfoBO userWallet = userInfoDaoMapper.findUserWalletById(userInfoBO.getId());
        if (!ObjectUtil.isBlank(userWallet)) {
            userInfoBO.setRedPackBalance(userWallet.getRedPackBalance());
            userInfoBO.setUserWalletBalance(userWallet.getUserWalletBalance());
            userInfoBO.setExpireCount(userWallet.getExpireCount());
        }
        return userWallet;
    }



    private void bindUserInfo(UserInfoPO userInfoPO, UserInfoVO userInfoVO1, TPInfoVO tpInfoVO, String ip) {
        UserInfoBO findOpenID = userInfoDaoMapper.findUserInfo(userInfoVO1);
        if (ObjectUtil.isBlank(findOpenID)) {
            //验证帐户名是否唯一，生成帐户名
            userInfoPO.setAccount(userInfoPO.getNickName());
            if (!ObjectUtil.isBlank(tpInfoVO.getGender()) && tpInfoVO.getGender().equals("男")) {
                userInfoPO.setSex((short) 1);
            } else if (!ObjectUtil.isBlank(tpInfoVO.getGender()) && tpInfoVO.getGender().equals("女")) {
                userInfoPO.setSex((short) 2);
            } else {
                userInfoPO.setSex(tpInfoVO.getSex());
            }
            userInfoVO1.setFristRegister(UserConstants.IS_TRUE);
            userInfoPO.setAddress(tpInfoVO.getProvince() + tpInfoVO.getCity());
            userInfoPO.setChannelId(tpInfoVO.getChannelId());
            userInfoPO.setPlatform(tpInfoVO.getPlatform());
            // 新增插入 代理系统编码
    		if (!ObjectUtil.isBlank(tpInfoVO.getAgentCode())) {
    			userInfoPO.setAgentCode(tpInfoVO.getAgentCode());
    		}
            userInfoPO.setUserType(UserConstants.UserTypeEnum.USER.getValue());
            userInfoDaoMapper.addUser(userInfoPO);
            UserWalletPO userWalletPO = new UserWalletPO(userInfoPO.getId(), UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.ZERO, UserConstants.IS_TRUE, UserConstants.ZERO, 1);
            userWalletMapper.addUserWallet(userWalletPO);
            // 添加日志
            publicMethod.insertOperateLog(userInfoPO.getId(), UserConstants.UserOperationEnum.REGISTER_SUCCESS.getKey(), UserConstants.IS_TRUE, ip, null, tpInfoVO.getOpenid(), UserConstants.UserOperationEnum.REGISTER_SUCCESS.getValue());
        } else {
            userInfoVO1.setFristRegister(UserConstants.IS_FALSE);
            UserInfoPO po = new UserInfoPO();
            po.setId(findOpenID.getId());
            if (!ObjectUtil.isBlank(userInfoPO.getQqName()) && !ObjectUtil.isBlank(userInfoPO.getHeadUrl())) {
                if (!tpInfoVO.getNickname().equals(findOpenID.getQqName())) {
                    po.setQqName(userInfoPO.getQqName());
                    userInfoDaoMapper.updateUserInfo(po);
                }
            } else if (!ObjectUtil.isBlank(userInfoPO.getWechatName())) {
                if (!userInfoPO.getWechatName().equals(findOpenID.getWechatName())) {
                    po.setWechatName(userInfoPO.getWechatName());
                    userInfoDaoMapper.updateUserInfo(po);
                }
            } else if (!ObjectUtil.isBlank(userInfoPO.getSinaName())) {
                if (!userInfoPO.getSinaName().equals(findOpenID.getSinaName())) {
                    po.setHeadUrl(userInfoPO.getHeadUrl());
                    po.setSinaName(userInfoPO.getSinaName());
                    userInfoDaoMapper.updateUserInfo(po);
                }
            }
        }
    }

    private ResultBO<?> toCache(TPInfoVO tpInfoVO, UserInfoVO userInfoVO1, String ip) {
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfo(userInfoVO1);
        if (userInfoBO.getAccountStatus().equals(UserConstants.IS_FALSE) &&
                (ObjectUtil.isBlank(userInfoBO.getForbitEndTime()) || new Date().after(userInfoBO.getForbitEndTime()))) {
            UserInfoPO infoPO = new UserInfoPO();
            infoPO.setAccountStatus(UserConstants.IS_TRUE);
            infoPO.setId(userInfoBO.getId());
            userInfoDaoMapper.updateUserInfo(infoPO);
        }
        //添加至缓存，自动登录
        UserInfoBO userInfoToCache = userInfoDaoMapper.findUserInfo(userInfoVO1);
        if (userInfoToCache.getAccountStatus().equals(UserConstants.IS_FALSE)
                || (!ObjectUtil.isBlank(userInfoToCache.getForbitEndTime()) && new Date().before(userInfoToCache.getForbitEndTime()))) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_FORBIDDEN_SERVICE);
        }
        String token = TokenUtil.createTokenStr(tpInfoVO.getPlatform());
        //添加token至对象中
        userInfoToCache.setToken(token);
        getUserIndexBO(userInfoToCache);
        userInfoToCache.setSafeIntegral(publicMethod.getSafeIntegration(userInfoToCache));
//        userInfoToCache.setLoginType(LoginTypeEnum.QQ.getKey());
        userInfoToCache.setFristRegister(userInfoVO1.getFristRegister());
        //清除密码
        userInfoToCache.setPassword(null);
        publicMethod.setHeadUrl(userInfoToCache, userInfoToCache);
        publicMethod.lastLogTime(userInfoToCache);
        if (!ObjectUtil.isBlank(userInfoToCache.getRealName()) && !ObjectUtil.isBlank(userInfoToCache.getIdCard())) {
            userInfoToCache.setAttestationRealName(UserConstants.IS_TRUE);
        } else {
            userInfoToCache.setAttestationRealName(UserConstants.IS_FALSE);
        }
        userInfoToCache.setLoginPlatform(tpInfoVO.getPlatform());
        publicMethod.modifyCache(token, userInfoToCache, tpInfoVO.getPlatform());

        //添加日志，登录成功
        publicMethod.inserLoginSuccessLog(ip, userInfoToCache);
        publicMethod.encryption(userInfoToCache, userInfoToCache);
        return ResultBO.ok(userInfoToCache);
    }


    /**
     * 兑换码兑换分布式锁
     *
     * @param openId
     * @return
     */
    private String getWechatOpenIdlLock(String openId) {
        return CacheConstants.C_CORE_WECHAT_OPEN_ID_LOCK + openId;
    }


    /**
     * 加锁
     *
     * @param lock
     * @throws InterruptedException
     */
    private void lock(RLock lock) {
        // 尝试加锁，最多等待30秒，上锁以后10秒自动解锁
        boolean isLock = false;
        try {
            isLock = lock.tryLock(30, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isLock) {
            throw new RuntimeException("加锁失败:" + lock.getName());
        }
    }

    private void unlock(RLock lock) {
        if (lock != null) {
            try {
                lock.unlock();
            } catch (Exception e) {
                logger.error("解锁失败", e);
            }
        }
    }
}
