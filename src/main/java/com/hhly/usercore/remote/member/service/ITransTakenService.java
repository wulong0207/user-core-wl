package com.hhly.usercore.remote.member.service;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.pay.vo.TakenValidateTypeVO;

/**
 * @desc 提款交易服务接口
 * @author xiongjingang
 * @date 2017年3月2日 上午10:44:46
 * @company 益彩网络科技公司
 * @version 1.0
 */
public interface ITransTakenService {

	/**  
	* 方法说明: 提款请求
	* @auth: xiongJinGang
	* @param token
	* @throws Exception
	* @time: 2017年4月18日 下午3:39:56
	* @return: ResultBO<?> 
	*/
	ResultBO<?> takenReq(String token) throws Exception;

	/**  
	* 方法说明: 提款请求前验证，通过后提供银行卡列表
	* @auth: xiongJinGang
	* @param takenValidateTypeVO
	* @throws Exception
	* @time: 2017年4月19日 上午10:04:41
	* @return: ResultBO<?> 
	*/
	ResultBO<?> takenReqValidate(TakenValidateTypeVO takenValidateTypeVO) throws Exception;

}
