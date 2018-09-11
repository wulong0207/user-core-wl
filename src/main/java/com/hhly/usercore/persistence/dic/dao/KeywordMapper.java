package com.hhly.usercore.persistence.dic.dao;

import java.util.List;

import com.hhly.skeleton.user.bo.KeywordBO;


/** 
* @Desc:  敏感词mapper
* @author YiJian 
* @date 2017年4月14日
* @compay 益彩网络科技有限公司
* @version 1.0
*/
public interface KeywordMapper {
	/** 
	* @Title: queryKeywordInfo 
	* @Description: 查询敏感词信息
	*  @return
	* @time 2017年4月14日 上午11:00:03
	*/
	List<KeywordBO> queryKeywordInfo();
}
