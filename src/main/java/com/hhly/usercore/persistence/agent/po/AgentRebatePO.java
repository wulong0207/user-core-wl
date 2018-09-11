package com.hhly.usercore.persistence.agent.po;

import java.util.Date;

public class AgentRebatePO {
    /**
     * 
     */
    private Integer id;

    /**
     * 代理id
     */
    private Integer agentId;

    /**
     * 添加时间
     */
    private Date addTime;

    /**
     * 默认标识0-否，1-是;用于标识默认配置项；默认配置仅有一条，不挂钩代理，不产生变更记录
     */
    private Short defaultFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

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
     * 代理id
     * @return agent_id 代理id
     */
    public Integer getAgentId() {
        return agentId;
    }

    /**
     * 代理id
     * @param agentId 代理id
     */
    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    /**
     * 添加时间
     * @return add_time 添加时间
     */
    public Date getAddTime() {
        return addTime;
    }

    /**
     * 添加时间
     * @param addTime 添加时间
     */
    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    /**
     * 默认标识0-否，1-是;用于标识默认配置项；默认配置仅有一条，不挂钩代理，不产生变更记录
     * @return default_flag 默认标识0-否，1-是;用于标识默认配置项；默认配置仅有一条，不挂钩代理，不产生变更记录
     */
    public Short getDefaultFlag() {
        return defaultFlag;
    }

    /**
     * 默认标识0-否，1-是;用于标识默认配置项；默认配置仅有一条，不挂钩代理，不产生变更记录
     * @param defaultFlag 默认标识0-否，1-是;用于标识默认配置项；默认配置仅有一条，不挂钩代理，不产生变更记录
     */
    public void setDefaultFlag(Short defaultFlag) {
        this.defaultFlag = defaultFlag;
    }

    /**
     * 创建人
     * @return create_by 创建人
     */
    public String getCreateBy() {
        return createBy;
    }

    /**
     * 创建人
     * @param createBy 创建人
     */
    public void setCreateBy(String createBy) {
        this.createBy = createBy == null ? null : createBy.trim();
    }

    /**
     * 创建时间
     * @return create_time 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 修改人
     * @return update_by 修改人
     */
    public String getUpdateBy() {
        return updateBy;
    }

    /**
     * 修改人
     * @param updateBy 修改人
     */
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy == null ? null : updateBy.trim();
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