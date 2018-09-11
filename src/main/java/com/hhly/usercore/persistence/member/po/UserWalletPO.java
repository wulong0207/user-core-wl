package com.hhly.usercore.persistence.member.po;

import java.util.Date;

public class UserWalletPO {
    /**
     * 自增长主键ID
     */
    private Integer id;

    /**
     * 用户ID表ID
     */
    private Integer userId;

    /**
     * 现金总余额
     */
    private Double totalCashBalance;

    /**
     * 中奖余额
     */
    private Double winningBalance;

    /**
     * 充值80%余额
     */
    private Double top80Balance;

    /**
     * 充值20%余额
     */
    private Double top20Balance;

    /**
     * 可用红包余额（只是彩金红包）
     */
    private Double effRedBalance;

    /**
     * 待发红包余额
     */
    private Double readyRedBalance;

    /**
     * 0：禁用中奖余额；1：启用
     */
    private Short status;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 可用资金显示方式:0隐藏;1显示
     */
    private Short display;

    /**
     * 
     */
    private Integer version;
    
	public UserWalletPO() {
	}

	public UserWalletPO(Integer userId, Double totalCashBalance, Double winningBalance, Double top80Balance, Double top20Balance, Double effRedBalance, Double readyRedBalance, Short status, Integer version) {
		this.userId = userId;
		this.totalCashBalance = totalCashBalance;
		this.winningBalance = winningBalance;
		this.top80Balance = top80Balance;
		this.top20Balance = top20Balance;
		this.effRedBalance = effRedBalance;
		this.readyRedBalance = readyRedBalance;
		this.status = status;
		this.version = version;
	}    

    /**
     * 自增长主键ID
     * @return id 自增长主键ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 自增长主键ID
     * @param id 自增长主键ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 用户ID表ID
     * @return user_id 用户ID表ID
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 用户ID表ID
     * @param userId 用户ID表ID
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 现金总余额
     * @return total_cash_balance 现金总余额
     */
    public Double getTotalCashBalance() {
        return totalCashBalance;
    }

    /**
     * 现金总余额
     * @param totalCashBalance 现金总余额
     */
    public void setTotalCashBalance(Double totalCashBalance) {
        this.totalCashBalance = totalCashBalance;
    }

    /**
     * 中奖余额
     * @return winning_balance 中奖余额
     */
    public Double getWinningBalance() {
        return winningBalance;
    }

    /**
     * 中奖余额
     * @param winningBalance 中奖余额
     */
    public void setWinningBalance(Double winningBalance) {
        this.winningBalance = winningBalance;
    }

    /**
     * 充值80%余额
     * @return top_80_balance 充值80%余额
     */
    public Double getTop80Balance() {
        return top80Balance;
    }

    /**
     * 充值80%余额
     * @param top80Balance 充值80%余额
     */
    public void setTop80Balance(Double top80Balance) {
        this.top80Balance = top80Balance;
    }

    /**
     * 充值20%余额
     * @return top_20_balance 充值20%余额
     */
    public Double getTop20Balance() {
        return top20Balance;
    }

    /**
     * 充值20%余额
     * @param top20Balance 充值20%余额
     */
    public void setTop20Balance(Double top20Balance) {
        this.top20Balance = top20Balance;
    }

    /**
     * 可用红包余额（只是彩金红包）
     * @return eff_red_balance 可用红包余额（只是彩金红包）
     */
    public Double getEffRedBalance() {
        return effRedBalance;
    }

    /**
     * 可用红包余额（只是彩金红包）
     * @param effRedBalance 可用红包余额（只是彩金红包）
     */
    public void setEffRedBalance(Double effRedBalance) {
        this.effRedBalance = effRedBalance;
    }

    /**
     * 待发红包余额
     * @return ready_red_balance 待发红包余额
     */
    public Double getReadyRedBalance() {
        return readyRedBalance;
    }

    /**
     * 待发红包余额
     * @param readyRedBalance 待发红包余额
     */
    public void setReadyRedBalance(Double readyRedBalance) {
        this.readyRedBalance = readyRedBalance;
    }

    /**
     * 0：禁用中奖余额；1：启用
     * @return status 0：禁用中奖余额；1：启用
     */
    public Short getStatus() {
        return status;
    }

    /**
     * 0：禁用中奖余额；1：启用
     * @param status 0：禁用中奖余额；1：启用
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * 更新时间
     * @return update_time 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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
     * 可用资金显示方式:0隐藏;1显示
     * @return display 可用资金显示方式:0隐藏;1显示
     */
    public Short getDisplay() {
        return display;
    }

    /**
     * 可用资金显示方式:0隐藏;1显示
     * @param display 可用资金显示方式:0隐藏;1显示
     */
    public void setDisplay(Short display) {
        this.display = display;
    }

    /**
     * 
     * @return version 
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * 
     * @param version 
     */
    public void setVersion(Integer version) {
        this.version = version;
    }
}