package com.hhly.usercore.persistence.member.dao;

import com.hhly.skeleton.pay.bo.PayBankCardH5BO;
import com.hhly.skeleton.pay.bo.PayBankcardBO;
import com.hhly.skeleton.pay.vo.PayBankcardVO;
import com.hhly.skeleton.user.bo.BankCardDetailBO;
import com.hhly.usercore.persistence.pay.po.PayBankcardPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @auth chenkangning
 * @date 2017/3/2
 * @desc 用户银行卡mapper接口
 * @compay 益彩网络科技有限公司
 * @version 1.0
 */
public interface BankcardMapper {

	/**
	 * 添加银行卡
	 * @param payBankcardPO
	 * @return
	 */
	int addBankCard(PayBankcardPO payBankcardPO);
    
    /**
     * 设置默认银行卡
     * @param payBankcardPO 参数
     * @return 影响行数
     */
	int updateDefault(PayBankcardPO payBankcardPO);
	
    /**
     * 取消其它银行卡为默认卡
     * @param payBankcardPO 参数
     * @return 影响行数
     */
	int updateDisableDefault(PayBankcardPO payBankcardPO);
    
    /**
     * 修改银行预留手机号
     *
     * @param payBankcardPO 参数及筛选条件
     * @return 影响行数
     */
    int updateOpenMobile(PayBankcardPO payBankcardPO);

	/**
	 * 查询用户银行卡信息
	 * @param payBankcardVO 数据对象
	 * @return
	 */
	List<PayBankcardBO> selectBankCard(PayBankcardVO payBankcardVO);


	List<BankCardDetailBO> findBankList(@Param("userId") Integer userId);

	/**  
	* 方法说明: 根据银行卡ID获取用户银行卡信息
	* @auth: xiongJinGang
	* @param userId
	* @param bankCardId
	* @time: 2017年4月8日 下午4:39:42
	* @return: PayBankcardBO 
	*/
	PayBankcardBO getUserBankById(@Param("userId") Integer userId, @Param("id") Integer bankCardId);
    
    /**
     * 根据id删除银行卡,逻辑删除，修改状态为0
     * @param id 银行卡id
     * @param userId 用户id
     * @return
     */
	int deleteByBankCardId(@Param("id") Integer id, @Param("userId") Integer userId);

	/**
	 * 更新银行卡信息
	 * @param payBankcardPO
	 * @return
	 */
	int updateByBankCardId(@Param("record") PayBankcardPO payBankcardPO);
    
	/**  
	* 方法说明: 更新银行名称
	* @auth: xiongJinGang
	* @param payBankcardPO
	* @time: 2017年5月4日 下午6:39:01
	* @return: int 
	*/
	int updateBankName(PayBankcardPO payBankcardPO);
    /**
     * 关闭或打开快捷支付
     * @param payBankcardPO 数据对象
     * @return 影响条数
     */
	int closeOrOpenQuickPayment(PayBankcardPO payBankcardPO);

	/**
	 * 根据用户ID和卡号，校验是不是已经添加过此张卡
	 * @param userId
	 * @param cardCode
	 * @return
	 */
	List<PayBankcardBO> selectByUserIdAndCardCodeIsExist(@Param("userId") String userId, @Param("cardCode") String cardCode);

	List<PayBankcardBO> selectAll();

	List<PayBankcardBO> findPayBankCardByUserId(@Param("userId") Integer userId, @Param("orderBy") String orderBy);
    
    /**
     * 查询用户银行卡信息
     * @param payBankcardVO 数据对象
     * @return
     */
    List<PayBankcardBO> selectBankCardForMobile(PayBankcardVO payBankcardVO);
	
	/**
	 * 获取账户、红包余额及，红包过期数
	 * @param userId
	 * @return
	 */
	List<PayBankCardH5BO> selectForMobileBlance(@Param("userId") Integer userId);
    
    /**
     * 获取银行小log
     *
     * @param bankId
     * @return
     */
    String selectPaybank(@Param("id") Integer bankId);
    
    /**
     * 完善资料送: 统计与用户相同银行卡的卡数
     * @param userId
     * @return
     */
	int countUserSameBankCard(@Param("userId") Integer userId);

}
