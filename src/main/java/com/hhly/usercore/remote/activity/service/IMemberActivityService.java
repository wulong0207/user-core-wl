package com.hhly.usercore.remote.activity.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserActivityVO;

/**
 * @author zhouyang
 * @version 1.0
 * @desc 用户管理
 * @date 2017/8/8
 * @company 益彩网络科技公司
 */
public interface IMemberActivityService {

    /**
     * 用户注册
     * @param passportVO
     * @return
     */
    ResultBO<?> regFromActivity(PassportVO passportVO);

    /**
     * 是否完善用户信息
     * @param passportVO
     * @return
     * @throws Exception
     */
    ResultBO<?> verifyPerfectInfo(String token);

    /**
     * 查询用户集合
     * @param userActivityVO
     * @return
     * @throws Exception
     */
    ResultBO<?> findUserList(UserActivityVO userActivityVO);

    /**
     * 验证帐户名
     * @param passportVO
     * @return
     */
    ResultBO<?> checkAccount(PassportVO passportVO);
}
