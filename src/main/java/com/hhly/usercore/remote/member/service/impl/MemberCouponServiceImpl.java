package com.hhly.usercore.remote.member.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.PagingBO;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.common.OperateCouponEnum.RedStatusEnum;
import com.hhly.skeleton.base.common.OperateCouponEnum.RedTypeEnum;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.page.IPageService;
import com.hhly.skeleton.base.page.ISimplePage;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.pay.bo.DicOperateCouponOptionBO;
import com.hhly.skeleton.pay.bo.OperateCouponBO;
import com.hhly.skeleton.pay.vo.OperateCouponVO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.persistence.pay.dao.OperateCouponMapper;
import com.hhly.usercore.remote.member.service.IMemberCouponService;

@Service("iMemberCouponService")
public class MemberCouponServiceImpl implements IMemberCouponService {
	
    private static final Logger logger = Logger.getLogger(MemberCouponServiceImpl.class);

    @Resource
    private OperateCouponMapper operateCouponMapper;

    @Autowired
    private IPageService pageService;

	@Autowired
	private UserUtil userUtil;

    /**
     * 获取用户红包列表
     *
     * @param token
     * @return
     */
    @Override
    public ResultBO<?> findCoupone(final OperateCouponVO vo) {
        UserInfoBO userInfo = userUtil.getUserByToken(vo.getToken());
        if (!ObjectUtil.isBlank(vo.getRedType()) && vo.getRedType() == 0) {
            vo.setRedType(null);
        }
        if (ObjectUtil.isBlank(userInfo) || ObjectUtil.isBlank(userInfo.getId())) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }

        vo.setUserId(userInfo.getId());
        // 默认第一页
        if (ObjectUtil.isBlank(vo.getPageIndex())) {
            vo.setPageIndex(0);
        }
        // 默认每页9条数据
        if (ObjectUtil.isBlank(vo.getPageSize())) {
            vo.setPageSize(9);
        }

        if (!ObjectUtil.isBlank(vo.getRedClass())) {
        	if(vo.getRedClass().equals("1")){
				vo.setSortField("VALUEORDER");
				vo.setSortOrder("DESC");
        	}else if(vo.getRedClass().equals("2")){
    			vo.setSortField("OBTAIN_TIME");
				vo.setSortOrder("DESC");
        	}
        }

        PagingBO<OperateCouponBO> bo = pageService.getPageData(vo, new ISimplePage<OperateCouponBO>() {
            @Override
            public int getTotal() {
                return operateCouponMapper.getCouponeCount(vo);
            }

            @Override
            public List<OperateCouponBO> getData() {
                return operateCouponMapper.getUserCoupone(vo);
            }
        });
        return ResultBO.ok(bo);
    }
    
    @Override
    public ResultBO<?> findCouponGroup(OperateCouponVO vo) {
        UserInfoBO userInfo = userUtil.getUserByToken(vo.getToken());
        if (!ObjectUtil.isBlank(vo.getRedType()) && vo.getRedType() == 0) {
            vo.setRedType(null);
        }
        if (ObjectUtil.isBlank(userInfo) || ObjectUtil.isBlank(userInfo.getId())) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        vo.setUserId(userInfo.getId());
        Map<String, Object> result = new HashMap<>();
        List<DicOperateCouponOptionBO> statues = operateCouponMapper.findCouponCountStatus(vo);
        
        vo.setRedStatus(String.valueOf(RedStatusEnum.AVAILABLE.getValue()));
        List<DicOperateCouponOptionBO> redTypes = operateCouponMapper.findCouponCountRedType(vo);
        
        int total = 0;
        for (DicOperateCouponOptionBO type : redTypes) {
        	total += type.getTotal();
		}
        RedTypeEnum.ALL_RED.setTotal(total);
        DicOperateCouponOptionBO bo = new DicOperateCouponOptionBO(RedTypeEnum.ALL_RED);
        redTypes.add(bo);

        result.put("statues", statues);
        result.put("redTypes", redTypes);
        result.put("redBalance", operateCouponMapper.getUserRedBalance(vo));
        return ResultBO.ok(result);
    }    
    
    
    @Override
	public ResultBO<?> getCouponTypeGroup(OperateCouponVO vo) {
        UserInfoBO userInfo = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfo) || ObjectUtil.isBlank(userInfo.getId())) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        
        vo.setUserId(userInfo.getId());
        
        List<DicOperateCouponOptionBO> redTypes = operateCouponMapper.findCouponCountRedType(vo);
        
        int total = 0;
        for (DicOperateCouponOptionBO type : redTypes) {
        	total += type.getTotal();
		}
        RedTypeEnum.ALL_RED.setTotal(total);
        DicOperateCouponOptionBO bo = new DicOperateCouponOptionBO(RedTypeEnum.ALL_RED);
        redTypes.add(bo);
        
		return ResultBO.ok(redTypes);
	}


	@Override
    public ResultBO<?> getCouponStatusGroup(OperateCouponVO vo){
        UserInfoBO userInfo = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfo) || ObjectUtil.isBlank(userInfo.getId())) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        
        if (!ObjectUtil.isBlank(vo) && vo.getRedType() == 0) {
            vo.setRedType(null);
        }
        vo.setUserId(userInfo.getId());
        List<DicOperateCouponOptionBO> redTypes = operateCouponMapper.findCouponCountRedType(vo);
        
        return ResultBO.ok(redTypes);
    }

    private Date getOverTime(Date overTime) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(overTime);
        calendar.add(Calendar.DAY_OF_MONTH,-3);
        overTime = calendar.getTime();
        return overTime;
    }
}
