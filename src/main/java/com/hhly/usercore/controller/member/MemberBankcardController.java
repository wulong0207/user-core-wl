package com.hhly.usercore.controller.member;

import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.MessageCodeConstants;

import com.hhly.skeleton.pay.vo.PayBankcardVO;
import com.hhly.usercore.remote.member.service.IMemberBankcardV11Service;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouyang
 * @version 1.1
 * @desc
 * @date 2018/6/23
 * @company 益彩网络科技公司
 */
@RestController
@RequestMapping("/member/bankcard")
public class MemberBankcardController {

    private static final Logger logger = Logger.getLogger(MemberBankcardController.class);

    @Autowired
    private IMemberBankcardV11Service memberBankcardV11Service;

    @RequestMapping("/detail")
    public Object getBankcardDetail(@RequestBody PayBankcardVO vo) {
        return memberBankcardV11Service.getBankName(vo);
    }


    @RequestMapping("/get/code")
    public Object getMobileCode(@RequestBody PayBankcardVO vo) throws Exception {
        return memberBankcardV11Service.getValidateCode(vo);
    }

    @RequestMapping("/add/bankcard")
    public Object addBankcard(@RequestBody PayBankcardVO vo) throws Exception {
        return memberBankcardV11Service.addBankCard(vo);
    }
}
