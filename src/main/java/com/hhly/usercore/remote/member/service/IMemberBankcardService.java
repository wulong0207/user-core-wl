package com.hhly.usercore.remote.member.service;

import java.util.List;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.pay.bo.PayBankcardBO;
import com.hhly.skeleton.pay.bo.UserPayTypeBO;
import com.hhly.skeleton.pay.vo.PayBankcardVO;
import com.hhly.skeleton.pay.vo.TakenReqParamVO;

/**
 * @version 1.0
 * @auth chenkangning
 * @date 2017/3/2
 * @desc 用户银行卡管理接口
 * @compay 益彩网络科技有限公司
 */
public interface IMemberBankcardService {
    
    /**
     * 打开或关闭快捷支付-PC
     *
     * @param token  用户唯一标识
     * @param cardId 银行卡id
     * @return ResultBO
     * @throws Exception 异常
     */
    ResultBO<?> openOrCloseQuickPayment (String token, Integer cardId, Integer status) throws Exception;
    
    /**
     * 关闭快捷支付
     *
     * @param token  用户唯一标识
     * @param cardId 银行卡id
     * @return
     * @throws Exception
     */
    ResultBO<?> closenQuickPayment (String token, Integer cardId, Integer status) throws Exception;
    
    /**
     * 添加
     *
     * @param token    用户唯一标识
     * @param userBank 银行卡对象
     * @return ResultBO
     */
    ResultBO<?> addBankCard (String token, PayBankcardVO userBank) throws Exception;
    
    /**
     * PC添加银行卡
     *
     * @param payBankcardVO 数据对象
     * @return ResultBO
     * @throws Exception 异常
     */
    ResultBO<?> addBandCardPc (PayBankcardVO payBankcardVO) throws Exception;
    
    /**
     * 设置默认银行卡
     *
     * @param payBankcardVO 数据对象
     * @return ResultBO
     * @throws Exception 异常
     */
    ResultBO<?> updateDefault (PayBankcardVO payBankcardVO) throws Exception;
    
    /**
     * 修改银行预留手机号
     *
     * @param payBankcardVO 参数及筛选条件
     * @return ResultBO
     * @throws Exception 异常
     */
    ResultBO<?> updateOpenMobile (PayBankcardVO payBankcardVO) throws Exception;
    
    /**
     * 激活信用卡
     *
     * @param payBankcardVO 参数及筛选条件
     * @return ResultBO
     * @throws Exception 异常
     */
    ResultBO<?> activateCard (PayBankcardVO payBankcardVO) throws Exception;
    
    /**
     * 查询用户银行卡信息
     *
     * @param token 用户唯一标识
     * @return ResultBO
     */
    ResultBO<?> selectBankCard (String token) throws Exception;
    
    /**
     * @param token    用户唯一标识
     * @param bankId   银行id
     * @param bankType 卡类型
     * @return ResultBO
     * @throws Exception 异常
     */
    ResultBO<?> getCardLimitDetail (String token, Short bankId, Short bankType) throws Exception;
    
    /**
     * 根据id删除银行卡,逻辑删除，修改状态为0
     *
     * @param token 用户唯一标识
     * @param id    ID
     * @return ResultBO
     * @throws Exception 异常
     */
    ResultBO<?> deleteByBankCardId (String token, Integer id) throws Exception;
    
    /**
     * 方法说明: 根据用户ID获取银行卡列表
     *
     * @param userId
     * @auth: xiongjingang
     * @time: 2017年3月17日 下午2:17:54
     * @return: List<PayBankcardBO>
     */
    List<PayBankcardBO> findUserBankList (Integer userId);
    
    /**
     * 获取用户支付方式列表
     *
     * @param token
     * @return
     */
    List<UserPayTypeBO> findUserPayTypes (String token, String orderBy);
    
    /**
     * 移动端查询用户银行卡信息
     *
     * @param token 用户唯一标识
     * @return ResultBO
     */
    ResultBO<?> selectCardForModile (String token);
    
    /**
     * 方法说明: 根据银行卡Id获取用户的银行信息
     *
     * @param userId
     * @param bankCardId
     * @auth: xiongJinGang
     * @time: 2017年4月8日 下午4:42:54
     * @return: PayBankcardBO
     */
    PayBankcardBO findUserBankById (Integer userId, Integer bankCardId);
    
    /**
     * 根据银行卡号取到银行卡名称，类型
     *
     * @param token    用户唯一标识
     * @param cardCode 银行卡号
     * @return
     */
    ResultBO<?> getBankName (String token, String cardCode);
    
    /**
     * 方法说明: 获取用户银行卡信息
     *
     * @auth: xiongjingang
     * @time: 2017年3月17日 上午10:11:14
     * @return: ResultBO
     */
    ResultBO<?> findUserBankCardByCardId (Integer userId, Integer bankCardId);
    
    /**
     * 获取校验码
     *
     * @param token  用户唯一标识
     * @param mobile 手机号
     * @return
     */
    ResultBO<?> getValidateCode (String token, String mobile) throws Exception;
    
    /**
     * 方法说明: 根据用户ID获取银行卡号，有默认的取默认的，没有默认的取第一个
     *
     * @param userId
     * @auth: xiongJinGang
     * @time: 2017年4月18日 下午2:55:13
     * @return: String
     */
    List<PayBankcardBO> findUserBankListFromCache (Integer userId);
    
    /**
     * 方法说明: 获取用户默认的银行卡
     *
     * @param userId
     * @auth: xiongJinGang
     * @time: 2017年4月19日 上午10:31:05
     * @return: PayBankcardBO
     */
    PayBankcardBO getSingleBankCard (Integer userId);
    
    /**
     * 方法说明: 获取用户指定的银行卡信息
     *
     * @param cardCode
     * @param userId
     * @auth: xiongJinGang
     * @time: 2017年4月21日 上午10:59:41
     * @return: PayBankcardBO
     */
    PayBankcardBO getBankCardByCardCodeFromCache (String cardCode, Integer userId);
    
    /**
     * 方法说明: 获取用户具体的银行卡信息，对比银行名称，不匹配则更新，匹配则不操作
     *
     * @param userId
     * @param takenReqParamVO
     * @auth: xiongJinGang
     * @time: 2017年5月4日 下午6:06:51
     * @return: void
     */
    void findBankByIdAndCheckName (Integer userId, TakenReqParamVO takenReqParamVO);

	/**  
	* 方法说明: 获取指定类型的银行卡
	* @auth: xiongJinGang
	* @param token
	* @param bankType 1 储蓄卡 2 信用卡
	* @throws Exception
	* @time: 2018年3月2日 下午3:10:18
	* @return: ResultBO<?> 
	*/
	ResultBO<?> selectBankCardByType(String token, Short bankType) throws Exception;
    
}
