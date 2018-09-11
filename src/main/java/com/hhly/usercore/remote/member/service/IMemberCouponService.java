package com.hhly.usercore.remote.member.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.pay.vo.OperateCouponVO;

public interface IMemberCouponService {
    
	/**
	 * 通过红包类型获取红包状态统计数
	 * @param token
	 * @param vo
	 * @return
	 * @date 2017年7月14日下午6:09:02
	 * @author cheng.chen
	 */
	ResultBO<?> getCouponStatusGroup(OperateCouponVO vo);    


    /**
     * 通过红包状态查询红包类型统计数
     * @param token
     * @return
     * @date 2017年7月17日下午3:56:00
     * @author cheng.chen
     */
    ResultBO<?> getCouponTypeGroup(OperateCouponVO vo);
    
    /**
     * 获取用户红包列表
     *
     * @param token
     * @return
     */
    ResultBO<?> findCoupone(OperateCouponVO vo);
    

    /**
     * 获取用户红包状态数量，红包类别数量，红包余额
     *
     * @param token
     * @return
     */
    ResultBO<?> findCouponGroup(OperateCouponVO vo);
}
