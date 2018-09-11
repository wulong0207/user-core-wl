package com.hhly.usercore.base.utils;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.util.DateUtil;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.RegularValidateUtil;
import com.hhly.skeleton.base.util.SpringUtil;
import com.hhly.skeleton.base.util.StringUtil;

/**
 * 用户信息格式验证
 * @desc
 * @author zhouyang
 * @date 2017年3月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public class ValidateUtil {

	public static final Logger logger = Logger.getLogger(ValidateUtil.class);

	/**
	 * 身份证号验证
	 * @param idCard 身份证号码
	 * @return
	 */
	public static ResultBO<?> validateIdCard(String idCard) throws Exception {
		//身份证号非空验证5
		if (ObjectUtil.isBlank(idCard)) {
			return ResultBO.err(MessageCodeConstants.IDCARD_IS_NULL_FIELD);
		}
		//身份证号格式验证
		if (!ObjectUtil.isBlank(IDCardUtil.IDCardValidate(StringUtil.getStringLowerCase(idCard)))) {
			return ResultBO.err(MessageCodeConstants.IDCARD_FORMAT_ERROR_FIELD);
		}
		String sub = idCard.substring(6,14);
		StringBuilder stringBuilder = new StringBuilder(sub);
		stringBuilder.insert(4,"-");
		stringBuilder.insert(7,"-");
		String [] str1 = stringBuilder.toString().split("-");
		Date date2 = DateUtil.getNowDate("yyyy-MM-dd");
		String strd = DateUtil.convertDateToStr(date2, "yyyy-MM-dd");
		String [] str2 = strd.split("-");
		Integer oldYear = Integer.valueOf(str2[0]) - Integer.valueOf(str1[0]);
		Integer month = Integer.valueOf(str2[1]) - Integer.valueOf(str1[1]);
		Integer day = Integer.valueOf(str2[2]) - Integer.valueOf(str1[2]);
		if (oldYear < 18) {
			return ResultBO.err(MessageCodeConstants.YOU_ARE_UNDER_AGE);
		} else if (oldYear == 18) {
			if (month < 0) {
				return ResultBO.err(MessageCodeConstants.YOU_ARE_UNDER_AGE);
			} else if (month == 0) {
				if (day <0 ) {
					return ResultBO.err(MessageCodeConstants.YOU_ARE_UNDER_AGE);
				}
			}
		}
		return ResultBO.ok();
	}
	
	/**
	 * 手机号验证
	 * @param mobile 手机号
	 * @return
	 */
	public static ResultBO<?> validateMobile(String mobile) {
		boolean truth = false;
		//手机号非空验证
		if (ObjectUtil.isBlank(mobile)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_IS_NULL_FIELD);
		}
		//手机号格式验证
		if (!mobile.matches(RegularValidateUtil.REGULAR_MOBILE)) {
			return ResultBO.err(MessageCodeConstants.MOBILE_FORMAT_ERROR_FIELD);
		}
		//手机号前三位
		//
		RedisUtil redisUtil =  SpringUtil.getBean(RedisUtil.class);
		List<String> numList = redisUtil.getObj(CacheConstants.C_CORE_MEMBER_MOBILE_NUM_SEGMENT, new ArrayList<String>());
		for (String numStr : numList) {
			if (mobile.startsWith(numStr, UserConstants.ZERO_INTEGER)) {
				truth = true;
			}
		}
		if (!truth) {
			return ResultBO.err(MessageCodeConstants.MOBILE_FORMAT_ERROR_FIELD);
		}
		return ResultBO.ok();
	}
	
	/**
	 * 邮箱验证
	 * @param email 邮箱
	 * @return
	 */
	public static ResultBO<?> validateEmail(String email) {
		//邮箱地址非空验证
		if (ObjectUtil.isBlank(email)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_IS_NULL_FIELD);
		}
		//邮箱地址格式验证
		if (!email.matches(RegularValidateUtil.REGULAR_EMAIL)) {
			return ResultBO.err(MessageCodeConstants.EMAIL_FORMAT_ERROR_FIELD);
		}
		return ResultBO.ok();
	}
	
	/**
	 * 用户名验证
	 * @param account 帐户名
	 * @return
	 */
	public static ResultBO<?> validateAccount(String account) {
		if (ObjectUtil.isBlank(account)) {
			return ResultBO.err(MessageCodeConstants.USERNAME_IS_NULL_FIELD);
		}
		if (getAccLength(account) < UserConstants.ACCOUNT_MIN || getAccLength(account) > UserConstants.ACCOUNT_MAX) {
			return ResultBO.err(MessageCodeConstants.THE_ACCOUNT_LENGTH_JUST_BETWEEN_FOUR_AND_TWENTY);
		}
		//帐户名不能为纯符号
		if (account.matches(RegularValidateUtil.REGULAR_ACCOUNT3)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NOT_ALL_SYMBOLS);
		}
		if (account.matches(RegularValidateUtil.REGULAR_ACCOUNT2)) {
			if (getAccLength(account) <= 9) {
				return ResultBO.ok();
			} else {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NUMBER_CON_NOT_OUT_OF_NINE);
			}
		} else {
			if (account.matches(RegularValidateUtil.REGULAR_NICKNAME)) {
				return ResultBO.ok();
			} else {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_FORMAT_ERROR_FIELD);
			}
		}
	}

	/**
	 * 用户名验证
	 * @param nickname 帐户名
	 * @return
	 */
	public static ResultBO<?> validateNickname(String nickname) {
		if (ObjectUtil.isBlank(nickname)) {
			return ResultBO.err(MessageCodeConstants.NICKNAME_IS_NOT_NULL);
		}
		if (getAccLength(nickname) < UserConstants.ACCOUNT_MIN || getAccLength(nickname) > UserConstants.ACCOUNT_MAX) {
			return ResultBO.err(MessageCodeConstants.NICKNAME_LENGTH_UNMATCHED);
		}
		//帐户名不能为纯符号
		if (nickname.matches(RegularValidateUtil.REGULAR_ACCOUNT3)) {
			return ResultBO.err(MessageCodeConstants.NICKNAME_IS_NOT_ALL_SYMBOL);
		}
		if (nickname.matches(RegularValidateUtil.REGULAR_ACCOUNT2)) {
			if (getAccLength(nickname) <= 9) {
				return ResultBO.ok();
			} else {
				return ResultBO.err(MessageCodeConstants.NICKNAME_NOT_OUT_OF_NINE);
			}
		} else {
			if (nickname.matches(RegularValidateUtil.REGULAR_NICKNAME)) {
				return ResultBO.ok();
			} else {
				return ResultBO.err(MessageCodeConstants.NICKNAME_FORMAT_ERROR);
			}
		}
	}

	public static  Integer getAccLength(String account) {
		Integer len = 0;
		char [] chars = account.toCharArray();
		for (int i = 0; i< account.length(); i++) {
			String str = String.valueOf(chars[i]);
			if (str.matches(RegularValidateUtil.CHINESE)) {
				len+=2;
			}
			if (str.matches(RegularValidateUtil.SYMBOL)) {
				len++;
			}
		}
		return len;
	}
	
	/**
	 * 帐号验证
	 * @param userName 帐号
	 * @return
	 */
	public static ResultBO<?> validateUserName(String userName) {
		//验证帐号是否为空
		if (ObjectUtil.isBlank(userName)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NULL_FIELD);
		}
		//验证帐号格式是否合理
		if (!userName.matches(RegularValidateUtil.REGULAR_EMAIL) && !userName.matches(RegularValidateUtil.REGULAR_MOBILE)
				&& !userName.matches((RegularValidateUtil.REGULAR_ACCOUNT))
				&& ValidateUtil.getAccLength(userName) >= 4 && ValidateUtil.getAccLength(userName) <=20) {
			ResultBO<?> resultBO = ValidateUtil.validateAccount(userName);
			if (resultBO.isError()) {
				return ResultBO.err(MessageCodeConstants.ACCOUNT_FORMAT_ERROR_FILED);
			}
		}
		return ResultBO.ok();
	}
	
	/**
	 * 验证手机号码或邮箱号
	 * @param userName
	 * @return
	 */
	public static ResultBO<?> validateUserName2(String userName) {
		if (ObjectUtil.isBlank(userName)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_IS_NULL_FIELD);
		}
		if (!userName.matches(RegularValidateUtil.REGULAR_EMAIL) && !userName.matches(RegularValidateUtil.REGULAR_MOBILE)) {
			return ResultBO.err(MessageCodeConstants.ACCOUNT_FORMAT_ERROR_FILED);
		}
		return ResultBO.ok();
	}
	
	/**
	 * 密码验证
	 * @param password 密码
	 * @return
	 */
	public static ResultBO<?> validatePassword(String password) {
		//验证密码是否为空
		if (ObjectUtil.isBlank(password)) {
			return ResultBO.err(MessageCodeConstants.PASSWORD_IS_NULL_FIELD);
		}
		if (!password.matches(RegularValidateUtil.REGULAR_PASSWORD)) {
			return ResultBO.err(MessageCodeConstants.PASSWORD_FORMAT_ERROR_FIELD);
		}
		return ResultBO.ok();
	}
	
	/**
	 * 验证真实姓名
	 * @param realName 真实姓名
	 * @return
	 */
	public static ResultBO<?> validateRealName(String realName) {
		//验证真实姓名是否为空
		if (ObjectUtil.isBlank(realName)) {
			return ResultBO.err(MessageCodeConstants.REALNAME_IS_NULL_FIELD);
		}
		if (realName.length() > UserConstants.TWENTYFIVE) {
			return ResultBO.err(MessageCodeConstants.YOUR_NAME_OUT_OF_SYSTEM_LENGTH);
		}
		//验证真实姓名格式
		if (!realName.matches(RegularValidateUtil.REGULAR_REALNAME)) {
			return ResultBO.err(MessageCodeConstants.REALNAME_FORMAT_ERROR_FIELD);
		}
		return ResultBO.ok();
	}

	public static ResultBO<?> validateCardNum(String cardNum) {
		if (ObjectUtil.isBlank(cardNum)) {
			return ResultBO.err(MessageCodeConstants.BANKCARD_IS_NULL);
		}
		if (!cardNum.matches(RegularValidateUtil.REGULAR_BANKCARD)) {
			return ResultBO.err(MessageCodeConstants.BANKCARD_FORMAT_ERROR);
		}
		return ResultBO.ok();
	}

	public  static ResultBO<?> validateIDCard(String idCard) {
		if (ObjectUtil.isBlank(idCard)) {
			return ResultBO.err(MessageCodeConstants.IDCARD_IS_NULL_FIELD);
		}
		if (!idCard.matches(RegularValidateUtil.REGULAR_BANKCARD)) {
			return ResultBO.err(MessageCodeConstants.IDCARD_FORMAT_ERROR_FIELD);
		}
		return ResultBO.ok();
	}
	
	/**
	 * 验证token
	 * @param token 缓存key
	 * @return
	 */
	public static ResultBO<?> validateToken(String token) {
		//验证token是否为空
		if (ObjectUtil.isBlank(token)) {
			return ResultBO.err(MessageCodeConstants.TOKEN_IS_NULL_FIELD);
		}
		return ResultBO.ok();
	}

}
