
package com.hhly.usercore.local.member.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.usercore.local.member.service.MemberSecurityService;
import com.hhly.usercore.persistence.member.dao.UserIpDaoMapper;
import com.hhly.usercore.persistence.member.po.UserIpPO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月26日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Service
public class MemberSecurityServiceImpl implements MemberSecurityService {

	@Autowired
	UserIpDaoMapper userIpDaoMapper;
	
	@Override
	public String findAreaByIp(String ip) {
        String str[] = ip.split("\\.");
        List<UserIpPO> list = userIpDaoMapper.findAreaByIpLike(str[0] + "." + str[1]);
        if(!ObjectUtil.isBlank(list)) {
            Long ipLong = Long.parseLong(str[0]) * 256 * 256 * 256 + Long.parseLong(str[1]) * 256 * 256 + Long.parseLong(str[2]) * 256 + Long.parseLong(str[3]);
            for (UserIpPO po : list) {
                if(new Long(po.getStartipnum()) <= ipLong && new Long(po.getEndipnum()) >= ipLong) {
                    return po.getCountry();
                }
            }
        }
        return null;		
	}
	
	public String findAreaByIp(String ip, String result) {
        Map<String, String> params = new HashMap<>();
        params.put("ip", ip);
        params.put("result", result);
        return userIpDaoMapper.findAreaByIp(params);		
	}	
	
	
	

}
