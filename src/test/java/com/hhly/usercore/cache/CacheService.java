
package com.hhly.usercore.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年2月27日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CacheService {

	@Autowired
	RedisUtil redisUtil;
	
	@Autowired
	UserUtil userUtil;
	
	@Test
	public void testCache(){
//		String token = TokenUtil.createTokenStr();
//		System.err.println("token : " + token);
//		Integer a = 15;
//		redisUtil.addObj(token, a, CacheConstants.THIRTY_MINUTES);
//		redisUtil.addObj(a.toString(), "abc", CacheConstants.THIRTY_MINUTES);
//		String b = redisUtil.getString(token);
//		System.err.println(b);
//		
//		Object c = redisUtil.getObj(token);
//		System.err.println(c.toString());
//		
//		Object d = redisUtil.getObj(a.toString());
//		System.err.println(d.toString());
		
//		redisUtil.addObj("81029db700ed44d78ba4fbd6e5296693", 1, CacheConstants.THIRTY_MINUTES);
//		Integer userId = redisUtil.getObj("81029db700ed44d78ba4fbd6e5296693", Integer.class);
//		System.err.println(userId);
		UserInfoBO userBo = userUtil.getUserByToken("4adcf4d7c61de41e38a6b6ef707bb1280");
		System.err.println(userBo.getMobile());
		
	}
}
