package com.hhly.usercore.persistence.member.po;

import java.util.Date;

/**
 * 
 * @desc    
 * @author  cheng chen
 * @date    2018年4月25日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public class UserInfoLastLogPO {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 注册时间
     */
    private Date regTime;

    /**
     * 最后一次登陆时间
     */
    private Date lastLoginTime;

    /**
     * 最后一次充值时间
     */
    private Date lastFillTime;

    /**
     * 最后一次购彩时间
     */
    private Date lastOrderTime;

    /**
     * IP
     */
    private String ip;

    /**
     * 省份城市
     */
    private String province;

    /**
     * 最后一次修改密码时间
     */
    private Date lastPasswordTime;
    
    
	public UserInfoLastLogPO() {
	}  

    /**
     * 主键ID
     * @return id 主键ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 主键ID
     * @param id 主键ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 用户ID
     * @return user_id 用户ID
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 用户ID
     * @param userId 用户ID
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 注册时间
     * @return reg_time 注册时间
     */
    public Date getRegTime() {
        return regTime;
    }

    /**
     * 注册时间
     * @param regTime 注册时间
     */
    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    /**
     * 最后一次登陆时间
     * @return last_login_time 最后一次登陆时间
     */
    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * 最后一次登陆时间
     * @param lastLoginTime 最后一次登陆时间
     */
    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * 最后一次充值时间
     * @return last_fill_time 最后一次充值时间
     */
    public Date getLastFillTime() {
        return lastFillTime;
    }

    /**
     * 最后一次充值时间
     * @param lastFillTime 最后一次充值时间
     */
    public void setLastFillTime(Date lastFillTime) {
        this.lastFillTime = lastFillTime;
    }

    /**
     * 最后一次购彩时间
     * @return last_order_time 最后一次购彩时间
     */
    public Date getLastOrderTime() {
        return lastOrderTime;
    }

    /**
     * 最后一次购彩时间
     * @param lastOrderTime 最后一次购彩时间
     */
    public void setLastOrderTime(Date lastOrderTime) {
        this.lastOrderTime = lastOrderTime;
    }

    /**
     * IP
     * @return ip IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * IP
     * @param ip IP
     */
    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    /**
     * 省份城市
     * @return province 省份城市
     */
    public String getProvince() {
        return province;
    }

    /**
     * 省份城市
     * @param province 省份城市
     */
    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    /**
     * 最后一次修改密码时间
     * @return last_password_time 最后一次修改密码时间
     */
    public Date getLastPasswordTime() {
        return lastPasswordTime;
    }

    /**
     * 最后一次修改密码时间
     * @param lastPasswordTime 最后一次修改密码时间
     */
    public void setLastPasswordTime(Date lastPasswordTime) {
        this.lastPasswordTime = lastPasswordTime;
    }
}