package com.hhly.usercore.remote.member.service.impl;

import com.github.pagehelper.PageHelper;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserModifyLogBO;
import com.hhly.skeleton.user.vo.UserModifyLogVO;
import com.hhly.usercore.base.common.PublicMethod;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.security.dao.UserSecurityDaoMapper;
import com.hhly.usercore.persistence.security.po.UserModifyLogPO;
import com.hhly.usercore.remote.member.service.IMemberSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户操作日志实现类
 *
 * @author zhouyang
 * @version 1.0
 * @desc
 * @date 2017年2月20日
 * @company 益彩网络科技公司
 */
@Service("iMemberSecurityService")
public class MemberSecurityServiceImpl implements IMemberSecurityService {
    
    @Autowired
    private UserSecurityDaoMapper userSecurityDaoMapper;
    
    @Autowired
    private UserInfoDaoMapper userInfoDaoMapper;

    @Resource
    private PublicMethod publicMethod;
    
    @Resource
    private RedisUtil redisUtil;

    @Value("${before_file_url}")
    private String before_file_url;
    
	@Autowired
	private UserUtil userUtil;
    
    @Override
    public int addModifyLog (UserModifyLogPO userModifyLogPO) {
        return userSecurityDaoMapper.addModifyLog(userModifyLogPO);
    }
    
    /**
     * 根据用户id查询操作日志
     *
     * @return
     */
    @Override
    public ResultBO<?> selectByUserId (UserModifyLogVO vo) {
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if(userInfoBO == null) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        vo.setUserId(userInfoBO.getId());
        PageHelper.startPage(vo.getCurrentPage(), 20, false);
        List<UserModifyLogBO> userModifyLogBOList = userSecurityDaoMapper.selectPage(vo);
        return ResultBO.ok(userModifyLogBOList);
    }
    
    @Override
    public ResultBO<?> findUserModifyLogByUserId (UserModifyLogVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if(validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO tokenInfo = userUtil.getUserByToken(vo.getToken());
        if(ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        UserInfoBO userSafeCenterBO = userSecurityDaoMapper.findUserInfoByUserId(tokenInfo.getId());
        if(ObjectUtil.isBlank(userSafeCenterBO)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
        userModifyLogVO.setUserId(tokenInfo.getId());
        PageHelper.startPage(vo.getCurrentPage(), 5, false);
        List<UserModifyLogBO> modifyLogList = userSecurityDaoMapper.findModifyLog(userModifyLogVO);
        if(!ObjectUtil.isBlank(modifyLogList)) {
            userSafeCenterBO.setModifyLogList(modifyLogList);
        }
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
        if(ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_FOUND_SERVICE);
        }
        if(!ObjectUtil.isBlank(userInfoBO.getPassword())) {
            userSafeCenterBO.setIsSetPassword(UserConstants.IS_TRUE);
        } else {
            userSafeCenterBO.setIsSetPassword(UserConstants.IS_FALSE);
        }
        userSafeCenterBO.setSafeIntegral(publicMethod.getSafeIntegration(userInfoBO));
        userSafeCenterBO.setLastLoginTime(tokenInfo.getLastLoginTime());
        userSafeCenterBO.setIp(tokenInfo.getIp());
        //将头像相对路径拼接成绝对路径
        publicMethod.setHeadUrl(userInfoBO, userSafeCenterBO);
		//更新缓存
        publicMethod.modifyCache(vo.getToken(), tokenInfo, tokenInfo.getLoginPlatform());
        return ResultBO.ok(userSafeCenterBO);
    }

    @Override
    public ResultBO<?> showUserLoginData(UserModifyLogVO vo) {
        //验证token
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO tokenInfo = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        UserModifyLogVO userModifyLogVO = new UserModifyLogVO();
        userModifyLogVO.setUserId(tokenInfo.getId());
        userModifyLogVO.setUserAction(UserConstants.UserOperationEnum.LOGIN_SUCCESS.getKey());
        List<UserModifyLogBO> userModifyLogBO = userSecurityDaoMapper.findModifyLog(userModifyLogVO);
        publicMethod.modifyCache(vo.getToken(), tokenInfo, (short)1);
        return ResultBO.ok(userModifyLogBO);
    }
    
    
}
