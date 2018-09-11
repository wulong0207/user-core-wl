package com.hhly.usercore.persistence.agent.po;

import java.util.Date;

public class AgentRebateConfigPO {
    /**
     * 
     */
    private Integer id;

    /**
     * 返佣配置主表id
     */
    private Integer rebateId;

    /**
     * 直属总额最小值
     */
    private Float directMinMoney;

    /**
     * 直属总额最大值
     */
	private Float directMaxMoney;

    /**
     * 直属返佣比例
     */
    private Float directRatio;

    /**
     * 代理总额最小值
     */
    private Float agentMinMoney;

    /**
     * 代理总额最大值
     */
    private Float agentMaxMoney;

    /**
     * 代理返佣比例
     */
    private Float agentRatio;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

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
     * 返佣配置主表id
     * @return rebate_id 返佣配置主表id
     */
    public Integer getRebateId() {
        return rebateId;
    }

    /**
     * 返佣配置主表id
     * @param rebateId 返佣配置主表id
     */
    public void setRebateId(Integer rebateId) {
        this.rebateId = rebateId;
    }

    /**
     * 直属总额最小值
     * @return direct_min_money 直属总额最小值
     */
    public Float getDirectMinMoney() {
        return directMinMoney;
    }

    /**
     * 直属总额最小值
     * @param directMinMoney 直属总额最小值
     */
    public void setDirectMinMoney(Float directMinMoney) {
        this.directMinMoney = directMinMoney;
    }

    /**
     * 直属总额最大值
     * @return direct_max_money 直属总额最大值
     */
    public Float getDirectMaxMoney() {
        return directMaxMoney;
    }

    /**
     * 直属总额最大值
     * @param directMaxMoney 直属总额最大值
     */
    public void setDirectMaxMoney(Float directMaxMoney) {
        this.directMaxMoney = directMaxMoney;
    }

    /**
     * 直属返佣比例
     * @return direct_ratio 直属返佣比例
     */
    public Float getDirectRatio() {
        return directRatio;
    }

    /**
     * 直属返佣比例
     * @param directRatio 直属返佣比例
     */
    public void setDirectRatio(Float directRatio) {
        this.directRatio = directRatio;
    }

    /**
     * 代理总额最小值
     * @return agent_min_money 代理总额最小值
     */
    public Float getAgentMinMoney() {
        return agentMinMoney;
    }

    /**
     * 代理总额最小值
     * @param agentMinMoney 代理总额最小值
     */
    public void setAgentMinMoney(Float agentMinMoney) {
        this.agentMinMoney = agentMinMoney;
    }

    /**
     * 代理总额最大值
     * @return agent_max_money 代理总额最大值
     */
    public Float getAgentMaxMoney() {
        return agentMaxMoney;
    }

    /**
     * 代理总额最大值
     * @param agentMaxMoney 代理总额最大值
     */
    public void setAgentMaxMoney(Float agentMaxMoney) {
        this.agentMaxMoney = agentMaxMoney;
    }

    /**
     * 代理返佣比例
     * @return agent_ratio 代理返佣比例
     */
    public Float getAgentRatio() {
        return agentRatio;
    }

    /**
     * 代理返佣比例
     * @param agentRatio 代理返佣比例
     */
    public void setAgentRatio(Float agentRatio) {
        this.agentRatio = agentRatio;
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
}