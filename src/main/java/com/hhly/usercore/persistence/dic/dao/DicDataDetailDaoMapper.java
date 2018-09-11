package com.hhly.usercore.persistence.dic.dao;

import java.util.List;


public interface DicDataDetailDaoMapper {

	/**
	 * 查询手机号码段集合
	 * @return
	 */
	List<String> findMobileNumLimitList();

}