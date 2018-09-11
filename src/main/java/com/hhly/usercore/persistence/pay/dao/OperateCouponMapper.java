package com.hhly.usercore.persistence.pay.dao;

import java.util.List;

import com.hhly.skeleton.pay.bo.DicOperateCouponOptionBO;
import com.hhly.skeleton.pay.bo.OperateCouponBO;
import com.hhly.skeleton.pay.vo.OperateCouponVO;

/**
 * @desc 运营管理的优惠券详情Mapper
 * @author xiongJinGang
 * @date 2017年3月22日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface OperateCouponMapper {
	
	/**
	 * 获取用户红包余额
	 * @param vo
	 * @return
	 * @date 2017年11月10日上午10:57:34
	 * @author cheng.chen
	 */
	Double getUserRedBalance(OperateCouponVO vo);

    int getCouponeCount(OperateCouponVO vo);

	List<OperateCouponBO> getUserCoupone(OperateCouponVO vo);
	
	List<DicOperateCouponOptionBO> findCouponCountStatus(OperateCouponVO vo);

	List<DicOperateCouponOptionBO> findCouponCountRedType(OperateCouponVO vo);

}
