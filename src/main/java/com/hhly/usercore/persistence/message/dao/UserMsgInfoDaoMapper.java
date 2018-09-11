package com.hhly.usercore.persistence.message.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.hhly.skeleton.lotto.base.dic.bo.DicDataDetailBO;
import com.hhly.skeleton.lotto.base.order.vo.OrderInfoVO;
import com.hhly.skeleton.user.bo.UserMsgConfigBO;
import com.hhly.skeleton.user.bo.UserMsgInfoBO;
import com.hhly.skeleton.user.vo.UserMsgConfigVO;
import com.hhly.skeleton.user.vo.UserMsgInfoVO;

/**
 * @author zhouyang
 * @version 1.0
 * @desc 消息中心dao层
 * @date 2017/11/7
 * @company 益彩网络科技公司
 */
public interface UserMsgInfoDaoMapper {

    /**
     * 查询消息
     * @param vo
     * @return
     */
    List<UserMsgInfoBO> findMsgInfo(UserMsgInfoVO vo);

    /**
     * 查询消息总记录
     * @param vo
     * @return
     */
    int findMsgInfoTotal(UserMsgInfoVO vo);

    /**
     * 查询消息导航栏
     * @param vo
     * @return
     */
    int findMsgInfoCount(UserMsgInfoVO vo);

    /**
     * 查询消息菜单栏
     * @param vo
     * @return
     */
    List<UserMsgInfoBO> findMsgInfoMenu(UserMsgInfoVO vo);

    /**
     * 查询用户的消息提醒开关状态
     * @param vo
     * @return
     */
    List<UserMsgConfigBO> findMsgSwitch(UserMsgInfoVO vo);

    /**
     * 查询用户app消息提醒子开关状态
     * @param vo
     * @return
     */
    List<UserMsgConfigBO> findMsgLotterySwitch(UserMsgInfoVO vo);

    /**
     * 修改消息状态
     * @param vo
     * @return
     */
    int updateMsgInfoStatus(UserMsgInfoVO vo);

    /**
     * 标记为已读
     * @param vo
     * @return
     */
    int updateAppMsgInfoStatus(UserMsgInfoVO vo);

    /**
     * 添加开关状态数据
     * @param vo
     * @return
     */
    int addSwitch(UserMsgConfigVO vo);

    /**
     * 修改开关状态
     * @param vo
     * @return
     */
    int updateSwitch(UserMsgConfigVO vo);

    /**
     * 恢复默认
     * @return
     */
    int recoverDefault(@Param("list") List<UserMsgConfigVO> list);

    /**
     * 更新彩种开关状态
     * @param list
     * @return
     */
    int updateLotterySwitchStatus(@Param("list") List<UserMsgConfigVO> list);

    /**
     * 删除用户消息
     * @param vo
     * @return
     */
    int deleteMsgInfoById(UserMsgInfoVO vo);

    /**
     * 清空当前列表
     * @param vo
     * @return
     */
    int deleteMsgInfo(UserMsgInfoVO vo);

    /**
     * 查询字典
     * @return
     */
    List<DicDataDetailBO> findMsgDic(@Param("dicCode") String dicCode);

    /**
     * 查询中奖订单信息
     * @param vo
     * @return
     */
    UserMsgInfoBO findOrderInfo(OrderInfoVO vo);
}
