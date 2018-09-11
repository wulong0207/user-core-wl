package com.hhly.usercore.persistence.pay.dao;

import com.hhly.skeleton.user.bo.LotteryTypeBO;
import com.hhly.skeleton.user.vo.LotteryTypeVO;

import java.util.List;

public interface LotteryTypeMapper {
    
    /**
     * 查询开售状态的彩种
     * @param lotteryTypeVO 彩种参数对象
     * @return list
     */
    List<LotteryTypeBO> selectByCondition(LotteryTypeVO lotteryTypeVO);
    
}