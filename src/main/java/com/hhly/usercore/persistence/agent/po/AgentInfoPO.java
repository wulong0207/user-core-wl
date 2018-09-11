package com.hhly.usercore.persistence.agent.po;

import java.math.BigDecimal;
import java.util.Date;

public class AgentInfoPO {
    /**
     * 
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 代理编号
     */
    private String agentCode;

    /**
     * 父代理id
     */
    private Integer parentAgentId;

    /**
     * 代理级别;无上级则为1，有上级则为上级级别+1
     */
    private Short agentLevel;

    /**
     * 代理状态;0：禁用；1：启用
     */
    private Short agentStatus;

    /**
     * 成为代理时间
     */
    private Date agentTime;

    /**
     * 代理钱包
     */
    private BigDecimal agentWallet;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 
     * @return id 
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id 
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 用户id
     * @return user_id 用户id
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 用户id
     * @param userId 用户id
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 代理编号
     * @return agent_code 代理编号
     */
    public String getAgentCode() {
        return agentCode;
    }

    /**
     * 代理编号
     * @param agentCode 代理编号
     */
    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode == null ? null : agentCode.trim();
    }

    /**
     * 父代理id
     * @return parent_agent_id 父代理id
     */
    public Integer getParentAgentId() {
        return parentAgentId;
    }

    /**
     * 父代理id
     * @param parentAgentId 父代理id
     */
    public void setParentAgentId(Integer parentAgentId) {
        this.parentAgentId = parentAgentId;
    }

    /**
     * 代理级别;无上级则为1，有上级则为上级级别+1
     * @return agent_level 代理级别;无上级则为1，有上级则为上级级别+1
     */
    public Short getAgentLevel() {
        return agentLevel;
    }

    /**
     * 代理级别;无上级则为1，有上级则为上级级别+1
     * @param agentLevel 代理级别;无上级则为1，有上级则为上级级别+1
     */
    public void setAgentLevel(Short agentLevel) {
        this.agentLevel = agentLevel;
    }

    /**
     * 代理状态;0：禁用；1：启用
     * @return agent_status 代理状态;0：禁用；1：启用
     */
    public Short getAgentStatus() {
        return agentStatus;
    }

    /**
     * 代理状态;0：禁用；1：启用
     * @param agentStatus 代理状态;0：禁用；1：启用
     */
    public void setAgentStatus(Short agentStatus) {
        this.agentStatus = agentStatus;
    }

    /**
     * 成为代理时间
     * @return agent_time 成为代理时间
     */
    public Date getAgentTime() {
        return agentTime;
    }

    /**
     * 成为代理时间
     * @param agentTime 成为代理时间
     */
    public void setAgentTime(Date agentTime) {
        this.agentTime = agentTime;
    }

    /**
     * 代理钱包
     * @return agent_wallet 代理钱包
     */
    public BigDecimal getAgentWallet() {
        return agentWallet;
    }

    /**
     * 代理钱包
     * @param agentWallet 代理钱包
     */
    public void setAgentWallet(BigDecimal agentWallet) {
        this.agentWallet = agentWallet;
    }

    /**
     * 修改时间
     * @return update_time 修改时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 修改时间
     * @param updateTime 修改时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 备注说明
     * @return remark 备注说明
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 备注说明
     * @param remark 备注说明
     */
    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}