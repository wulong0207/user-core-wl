package com.hhly.usercore.remote.member.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserInfoVO;
import com.hhly.skeleton.user.vo.UserMsgConfigVO;
import com.hhly.skeleton.user.vo.UserMsgInfoVO;

/**
 * @author zhouyang
 * @version 1.0
 * @desc 用户消息中心接口
 * @date 2017/11/8
 * @company 益彩网络科技公司
 */
public interface IMemberMessageService {

    /**
     * 查询消息菜单栏
     * @param vo
     * @return
     */
    ResultBO<?> findMsgMenu(UserMsgInfoVO vo);

    /**
     * 查询消息
     * @param vo
     * @return
     */
    ResultBO<?> findList(UserMsgInfoVO vo);

    /**
     * 查询开关
     * @param vo
     * @return
     */
    ResultBO<?> findMsgSwitch(UserMsgInfoVO vo);

    /**
     * 查询app彩种开关
     * @param vo
     * @return
     */
    ResultBO<?> findMsgNodeSwitch(UserMsgInfoVO vo);

    /**
     * 修改消息状态
     * @param vo
     * @return
     */
    ResultBO<?> updateMsgInfoStatus(UserMsgInfoVO vo);

    /**
     * 修改消息接收开关状态
     * @param vo
     * @return
     */
    ResultBO<?> updateSwitchStatus(UserMsgConfigVO vo);

    /**
     * 修改彩种开关状态
     * @param vo
     * @return
     */
    ResultBO<?> updateLotterySwitchStatus(UserMsgConfigVO vo);

    /**
     * 恢复默认
     * @param vo
     * @return
     */
    ResultBO<?> recoverDefault(UserMsgConfigVO vo);

    /**
     * 删除消息
     * @param vo
     * @return
     */
    ResultBO<?> deleteMsgInfoById(UserMsgInfoVO vo);

    /**
     * 清空列表
     * @param vo
     * @return
     */
    ResultBO<?> deleteMsgInfo(UserMsgInfoVO vo);

    /**
     * 消息设置-勿扰模式，设置勿扰时间段
     * @param passportVO
     * @return object
     * @throws Exception 异常
     */
    ResultBO<?> doNotDisturb(PassportVO passportVO);

    /**
     * 根据用户ID获取用户设置的勿扰模式
     * @param token
     * @return
     * @throws Exception
     */
    ResultBO<?> getDisturb (String token) throws Exception;
}
