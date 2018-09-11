package com.hhly.usercore.remote.member.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.pay.vo.PayBankcardVO;

/**
 * @author zhouyang
 * @version 1.1
 * @desc
 * @date 2018/6/23
 * @company 益彩网络科技公司
 */
public interface IMemberBankcardV11Service {


    /**
     * 根据银行卡号取到银行卡名称，类型
     *
     * @param vo 银行卡对象
     * @return
     */
    ResultBO<?> getBankName(PayBankcardVO vo);

    /**
     * 获取校验码
     * @param vo 银行卡对象
     * @return
     */
    ResultBO<?> getValidateCode(PayBankcardVO vo) throws Exception;

    /**
     * 绑定银行卡
     * @param vo 银行卡对象
     * @return ResultBO
     */
    ResultBO<?> addBankCard(PayBankcardVO vo) throws Exception;
}
