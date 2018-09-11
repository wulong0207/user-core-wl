package com.hhly.usercore.persistence.member.po;

import java.util.Date;


/**
 * 用户信息（对表）
 * @desc
 * @author zhouyang
 * @date 2017年2月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public class UserInfoPO {
	
	/**
	 * 自增id（即帐号id）
	 */
	private Integer id;
	
	/**
	 * 账号综合平台id
	 */
	private String accountId;
	
	/**
	 * 帐户名
	 */
	private String account;
	
	/**
	 * 密码
	 */
	private String password;
	
	/**
	 * 密码随机码
	 */
	private String rCode;
	
    /**
     * 头像是否审核:0未审核;1已经审核
     */
    private Short headCheck;
	
	/**
	 * 用户昵称
	 */
	private String nickName;
	
	/**
	 * 头像URL
	 */
	private String headUrl;
	
	/**
	 * 头像状态	0：禁用，1：启用
	 */
	private Short headStatus;
	
	/**
	 * 手机号
	 */
	private String mobile;
	
	/**
	 * 手机号是否认证	0：未认证，1：已认证
	 */
	private Short mobileStatus;
	
	/**
	 * 是否开启手机号登录	0：禁用，1：启用
	 */
	private Short isMobileLogin;
	
	/**
	 * 电子邮箱
	 */
	private String email;

	/**
	 * 邮箱是否认证	0：未认证，1：已认证
	 */
	private Short emailStatus;
	
	/**
	 * 是否开启邮箱登录	0：禁用，1：启用
	 */
	private Short isEmailLogin;
	
	/**
	 * 账户状态
	 */
	private Short accountStatus;

	/**
	 * 禁用截止时间
	 */
	private Date forbitEndTime;
	
	/**
	 * 姓名
	 */
	private String realName;
	
	/**
	 * 身份证号码
	 */
	private String idCard;
	
	/**
	 * 居住地址
	 */
	private String address;
	
    /**
     * 1：低级；2：中级；3：高级
     */
    private Short passwordGrade;
	
	/**
	 * 性别
	 */
	private Short sex;
	
	/**
	 * 注册时间
	 */
	private Date registerTime;
	
	/**
	 * 登录时间
	 */
	private Date lastLoginTime;

	/**
	 * 账户注册渠道id
	 */
	private String channelId;
	
	/**
	 * ip地址
	 */
	private String ip;
	
	/**
	 * 账号和昵称是否修改过 0：否，1：账号修改过; 2:昵称修改过;3:全修改过
	 */
	private Short accountModify;
	
	/**
	 * 支付id
	 */
	private Integer userPayId;

	/**
	 * qqopenId
	 */
	private String qqOpenID;

	/**
	 * 新浪openId
	 */
	private String sinaBlogOpenID;

	/**
	 * 百度openId
	 */
	private String baiduOpenID;

	/**
	 * 微信openId
	 */
	private String wechatOpenID;

	/**
	 * 支付宝openId
	 */
	private String alipayOpenID;

	/**
	 * 京东openId
	 */
	private String jdOpenID;
	
	/**
	 * 渠道openId
	 */
	private String channelOpenID;

	/**
	 * QQ昵称
	 */
	private String qqName;

	/**
	 * 微信昵称
	 */
	private String wechatName;

	/**
	 * 新浪微博昵称
	 */
	private String sinaName;

	/**APP免打扰时间段,例如：23:00-09:00*/
	private String appNotDisturb;
	
    /**APP推送:0不接收;1接收*/
	private Integer msgApp;

	/**
	 * 注册平台 1.web, 2.wap, 3.android, 4.ios
	 */
	private Short platform;
	
	/**
	 * 代理系统使用，代理编码
	 */
	private String agentCode;
	
	/**
	 * 用户类型 0:益彩用户 1:代理用户
	 */
	private Short userType;
	
	/**
	 * 用户最后使用的银行卡号
	 */
	private String userPayCardcode;
   
    public Integer getMsgApp () {
        return msgApp;
    }
    
    public void setMsgApp (Integer msgApp) {
        this.msgApp = msgApp;
    }
    
    public String getAppNotDisturb () {
        return appNotDisturb;
    }
    
    public void setAppNotDisturb (String appNotDisturb) {
        this.appNotDisturb = appNotDisturb;
    }
    
    public UserInfoPO() {
		super();
	}

	public UserInfoPO(Short mobileStatus, Short isMobileLogin, Short emailStatus, Short isEmailLogin, Short accountStatus, Short accountModify) {
		this.mobileStatus = mobileStatus;
		this.isMobileLogin = isMobileLogin;
		this.emailStatus = emailStatus;
		this.isEmailLogin = isEmailLogin;
		this.accountStatus = accountStatus;
		this.accountModify = accountModify;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getrCode() {
		return rCode;
	}

	public void setrCode(String rCode) {
		this.rCode = rCode;
	}

	public Short getHeadCheck() {
		return headCheck;
	}

	public void setHeadCheck(Short headCheck) {
		this.headCheck = headCheck;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public Short getHeadStatus() {
		return headStatus;
	}

	public void setHeadStatus(Short headStatus) {
		this.headStatus = headStatus;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Short getMobileStatus() {
		return mobileStatus;
	}

	public void setMobileStatus(Short mobileStatus) {
		this.mobileStatus = mobileStatus;
	}

	public Short getIsMobileLogin() {
		return isMobileLogin;
	}

	public void setIsMobileLogin(Short isMobileLogin) {
		this.isMobileLogin = isMobileLogin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Short getEmailStatus() {
		return emailStatus;
	}

	public void setEmailStatus(Short emailStatus) {
		this.emailStatus = emailStatus;
	}

	public Short getIsEmailLogin() {
		return isEmailLogin;
	}

	public void setIsEmailLogin(Short isEmailLogin) {
		this.isEmailLogin = isEmailLogin;
	}

	public Short getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(Short accountStatus) {
		this.accountStatus = accountStatus;
	}

	public Date getForbitEndTime() {
		return forbitEndTime;
	}

	public void setForbitEndTime(Date forbitEndTime) {
		this.forbitEndTime = forbitEndTime;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public Short getPasswordGrade() {
		return passwordGrade;
	}

	public void setPasswordGrade(Short passwordGrade) {
		this.passwordGrade = passwordGrade;
	}

	public Short getSex() {
		return sex;
	}

	public void setSex(Short sex) {
		this.sex = sex;
	}

	public Date getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Short getAccountModify() {
		return accountModify;
	}

	public void setAccountModify(Short accountModify) {
		this.accountModify = accountModify;
	}

	public Integer getUserPayId() { return userPayId; }

	public void setUserPayId(Integer userPayId) {
		this.userPayId = userPayId;
	}

	public String getQqOpenID() {
		return qqOpenID;
	}

	public void setQqOpenID(String qqOpenID) {
		this.qqOpenID = qqOpenID;
	}

	public String getSinaBlogOpenID() {
		return sinaBlogOpenID;
	}

	public void setSinaBlogOpenID(String sinaBlogOpenID) {
		this.sinaBlogOpenID = sinaBlogOpenID;
	}

	public String getBaiduOpenID() {
		return baiduOpenID;
	}

	public void setBaiduOpenID(String baiduOpenID) {
		this.baiduOpenID = baiduOpenID;
	}

	public String getWechatOpenID() {
		return wechatOpenID;
	}

	public void setWechatOpenID(String wechatOpenID) {
		this.wechatOpenID = wechatOpenID;
	}

	public String getAlipayOpenID() {
		return alipayOpenID;
	}

	public void setAlipayOpenID(String alipayOpenID) {
		this.alipayOpenID = alipayOpenID;
	}

	public String getJdOpenID() {
		return jdOpenID;
	}

	public void setJdOpenID(String jdOpenID) {
		this.jdOpenID = jdOpenID;
	}
	
	public String getChannelOpenID() {
		return channelOpenID;
	}

	public void setChannelOpenID(String channelOpenID) {
		this.channelOpenID = channelOpenID;
	}

	public String getQqName() {
		return qqName;
	}

	public void setQqName(String qqName) {
		this.qqName = qqName;
	}

	public String getWechatName() {
		return wechatName;
	}

	public void setWechatName(String wechatName) {
		this.wechatName = wechatName;
	}

	public String getSinaName() {
		return sinaName;
	}

	public void setSinaName(String sinaName) {
		this.sinaName = sinaName;
	}

	public Short getPlatform() {
		return platform;
	}

	public void setPlatform(Short platform) {
		this.platform = platform;
	}

	public String getAgentCode() {
		return agentCode;
	}

	public void setAgentCode(String agentCode) {
		this.agentCode = agentCode;
	}

	public Short getUserType() {
		return userType;
	}

	public void setUserType(Short userType) {
		this.userType = userType;
	}

	public String getUserPayCardcode() {
		return userPayCardcode;
	}

	public void setUserPayCardcode(String userPayCardcode) {
		this.userPayCardcode = userPayCardcode;
	}
}
