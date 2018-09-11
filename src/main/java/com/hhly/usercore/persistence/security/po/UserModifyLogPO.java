package com.hhly.usercore.persistence.security.po;

import com.hhly.skeleton.user.vo.UserModifyLogVO;

import java.util.Date;

/**
 * 用户操作日志（对表）
 * @desc
 * @author zhouyang
 * @date 2017年2月9日
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
	 * 用户操作类型或者栏目
	 */
	private Short userAction;
	
	/**
	 * 操作状态	0：失败，1：成功，2：操作中
	 */
	private Short operationStatus;
	
	/**
	 * 用户IP
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
	private Date updateTime;
	
	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 存储过程返回值
	 */
	private Integer result;

	/**
	 * 存储过程拼接字段
	 */
	private String str;
    
    /**
     * 城市
     */
    private String province;
    
    
    public String getProvince () {
        return province;
    }
    
    public void setProvince (String province) {
        this.province = province;
    }
    
    public UserModifyLogPO () {
        super();
	}
	
	public UserModifyLogPO(UserModifyLogVO userModifyLogVO) {
		this.userId = userModifyLogVO.getUserId();
		this.userAction = userModifyLogVO.getUserAction();
		this.userIp = userModifyLogVO.getUserIp();
	}
	

	public UserModifyLogPO(Integer userId, Short userAction, Short operationStatus, String userIp,
			String logBefore, String logAfter, String remark) {
		super();
		this.userId = userId;
		this.userAction = userAction;
		this.operationStatus = operationStatus;
		this.userIp = userIp;
		this.logBefore = logBefore;
		this.logAfter = logAfter;
		this.remark = remark;
	}

	public UserModifyLogPO(Integer userId, Short userAction, String userIp, String str, Integer result) {
		this.userId = userId;
		this.userAction = userAction;
		this.userIp = userIp;
		this.str = str;
		this.result = result;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Short getUserAction() {
		return userAction;
	}

	public void setUserAction(Short userAction) {
		this.userAction = userAction;
	}

	public Short getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(Short operationStatus) {
		this.operationStatus = operationStatus;
	}

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public String getLogBefore() {
		return logBefore;
	}

	public void setLogBefore(String logBefore) {
		this.logBefore = logBefore;
	}

	public String getLogAfter() {
		return logAfter;
	}

	public void setLogAfter(String logAfter) {
		this.logAfter = logAfter;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
}
