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
public class UserModifyLogPO {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户操作类型或者栏目，例如手机号修改 1注册,2登录,3修改信息,4找回,9其它
     */
    private Short userAction;

    /**
     * 操作状态 1成功 0失败 2操作中
     */
    private Short operationStatus;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 修改前的数据
     */
    private String logBefore;

    /**
     * 修改后的数据
     */
    private String logAfter;

    /**
     * 修改时间
     */
    private Date createTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 省份
     */
    private String province;
    
    
    
	public UserModifyLogPO() {
	}

	public UserModifyLogPO(Integer userId, Short userAction, Short operationStatus, String userIp,
			String logBefore, String logAfter, String remark) {
		this.userId = userId;
		this.userAction = userAction;
		this.operationStatus = operationStatus;
		this.userIp = userIp;
		this.logBefore = logBefore;
		this.logAfter = logAfter;
		this.remark = remark;
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
     * 用户操作类型或者栏目，例如手机号修改 1注册,2登录,3修改信息,4找回,9其它
     * @return user_action 用户操作类型或者栏目，例如手机号修改 1注册,2登录,3修改信息,4找回,9其它
     */
    public Short getUserAction() {
        return userAction;
    }

    /**
     * 用户操作类型或者栏目，例如手机号修改 1注册,2登录,3修改信息,4找回,9其它
     * @param userAction 用户操作类型或者栏目，例如手机号修改 1注册,2登录,3修改信息,4找回,9其它
     */
    public void setUserAction(Short userAction) {
        this.userAction = userAction;
    }

    /**
     * 操作状态 1成功 0失败 2操作中
     * @return operation_status 操作状态 1成功 0失败 2操作中
     */
    public Short getOperationStatus() {
        return operationStatus;
    }

    /**
     * 操作状态 1成功 0失败 2操作中
     * @param operationStatus 操作状态 1成功 0失败 2操作中
     */
    public void setOperationStatus(Short operationStatus) {
        this.operationStatus = operationStatus;
    }

    /**
     * 用户 IP
     * @return user_ip 用户 IP
     */
    public String getUserIp() {
        return userIp;
    }

    /**
     * 用户 IP
     * @param userIp 用户 IP
     */
    public void setUserIp(String userIp) {
        this.userIp = userIp == null ? null : userIp.trim();
    }

    /**
     * 修改前的数据
     * @return log_before 修改前的数据
     */
    public String getLogBefore() {
        return logBefore;
    }

    /**
     * 修改前的数据
     * @param logBefore 修改前的数据
     */
    public void setLogBefore(String logBefore) {
        this.logBefore = logBefore == null ? null : logBefore.trim();
    }

    /**
     * 修改后的数据
     * @return log_after 修改后的数据
     */
    public String getLogAfter() {
        return logAfter;
    }

    /**
     * 修改后的数据
     * @param logAfter 修改后的数据
     */
    public void setLogAfter(String logAfter) {
        this.logAfter = logAfter == null ? null : logAfter.trim();
    }

    /**
     * 修改时间
     * @return create_time 修改时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 修改时间
     * @param createTime 修改时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 备注
     * @return remark 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 备注
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    /**
     * 省份
     * @return province 省份
     */
    public String getProvince() {
        return province;
    }

    /**
     * 省份
     * @param province 省份
     */
    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }
}