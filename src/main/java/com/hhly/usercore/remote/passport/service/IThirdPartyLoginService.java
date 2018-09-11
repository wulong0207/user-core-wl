package com.hhly.usercore.remote.passport.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.TPInfoVO;

/**
 * @author  zhouyang
 * @version  1.0
 * @desc  第三方登录接口
 * @date  2017/5/4
 * @company  益彩网络科技公司
 */
public interface IThirdPartyLoginService {

    /**
     * 第三方-QQ
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> tpQQLogin(TPInfoVO tpInfoVO);

    /**
     * 第三方-微信
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> tpWXLogin(TPInfoVO tpInfoVO);
    
    /**
     * 第三方 -微信
     * @param tpInfoVO
     * @return
     * @throws Exception
     * @version 1.1
     */
    ResultBO<?> tpV11WXLogin(TPInfoVO tpInfoVO);    

    /**
     * 第三方-新浪微博
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> tpWBLogin(TPInfoVO tpInfoVO);

    /**
     * 第三方-渠道登录
     * @param tpInfoVO
     * @return
     * @throws Exception
     * @date 2017年7月8日下午5:17:45
     * @author cheng.chen
     */
    ResultBO<?> tpChannelLogin(TPInfoVO tpInfoVO);

    /**
     * 绑定QQ
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> bindQQ(TPInfoVO tpInfoVO);

    /**
     * 绑定微信
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> bindWX(TPInfoVO tpInfoVO);

    /**
     * 绑定新浪微博
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> bindWB(TPInfoVO tpInfoVO);

    /**
     *
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> removeQQ(TPInfoVO tpInfoVO);

    /**
     *
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> removeWX(TPInfoVO tpInfoVO);

    /**
     * 解除微博绑定
     * @param tpInfoVO
     * @return
     * @throws Exception
     */
    ResultBO<?> removeWB(TPInfoVO tpInfoVO);

}
