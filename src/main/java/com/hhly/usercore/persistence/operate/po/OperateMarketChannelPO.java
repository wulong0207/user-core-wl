package com.hhly.usercore.persistence.operate.po;

import java.math.BigDecimal;
import java.util.Date;

public class OperateMarketChannelPO {
    /**
     * 自增长ID
     */
    private Integer id;

    /**
     * 自定义ID
     */
    private String channelId;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 1：Web；2：Wap；3：Android；4：IOS；5：未知
     */
    private Short terminalPlatform;

    /**
     * 父级渠道ID，等级为1时为空
     */
    private String parentChannelId;

    /**
     * 最高级是1；1：1级；2：2级；3：3级；4：4级；5：5级
     */
    private Short grade;

    /**
     * 1：不结算；2：销售量；3：有效激活；4：首次启动量；5：注册量；6：实名认证量；7：首次充值；8：首次成功购彩；
     */
    private Short settlementType;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 渠道来源的主页URL
     */
    private String resourceUrl;

    /**
     * 结算费率
     */
    private BigDecimal settlementRate;

    /**
     * 合作开始时间
     */
    private Date coopStartTime;

    /**
     * 合作结束时间
     */
    private Date coopEndTime;

    /**
     * 0：禁用；1：启用；2：合作过期；3：合作终止
     */
    private Short channelStatus;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 修改人
     */
    private String modifyBy;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * app下载路径
     */
    private String appUrl;

    /**
     * 是否为马甲包0：否；1：是
     */
    private Short majia;

    /**
     * 接口对接秘钥
     */
    private String secretKey;

    /**
     * 自增长ID
     * @return id 自增长ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 自增长ID
     * @param id 自增长ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 自定义ID
     * @return channel_id 自定义ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * 自定义ID
     * @param channelId 自定义ID
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId == null ? null : channelId.trim();
    }

    /**
     * 渠道名称
     * @return channel_name 渠道名称
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * 渠道名称
     * @param channelName 渠道名称
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName == null ? null : channelName.trim();
    }

    /**
     * 1：Web；2：Wap；3：Android；4：IOS；5：未知
     * @return terminal_platform 1：Web；2：Wap；3：Android；4：IOS；5：未知
     */
    public Short getTerminalPlatform() {
        return terminalPlatform;
    }

    /**
     * 1：Web；2：Wap；3：Android；4：IOS；5：未知
     * @param terminalPlatform 1：Web；2：Wap；3：Android；4：IOS；5：未知
     */
    public void setTerminalPlatform(Short terminalPlatform) {
        this.terminalPlatform = terminalPlatform;
    }

    /**
     * 父级渠道ID，等级为1时为空
     * @return parent_channel_id 父级渠道ID，等级为1时为空
     */
    public String getParentChannelId() {
        return parentChannelId;
    }

    /**
     * 父级渠道ID，等级为1时为空
     * @param parentChannelId 父级渠道ID，等级为1时为空
     */
    public void setParentChannelId(String parentChannelId) {
        this.parentChannelId = parentChannelId == null ? null : parentChannelId.trim();
    }

    /**
     * 最高级是1；1：1级；2：2级；3：3级；4：4级；5：5级
     * @return grade 最高级是1；1：1级；2：2级；3：3级；4：4级；5：5级
     */
    public Short getGrade() {
        return grade;
    }

    /**
     * 最高级是1；1：1级；2：2级；3：3级；4：4级；5：5级
     * @param grade 最高级是1；1：1级；2：2级；3：3级；4：4级；5：5级
     */
    public void setGrade(Short grade) {
        this.grade = grade;
    }

    /**
     * 1：不结算；2：销售量；3：有效激活；4：首次启动量；5：注册量；6：实名认证量；7：首次充值；8：首次成功购彩；
     * @return settlement_type 1：不结算；2：销售量；3：有效激活；4：首次启动量；5：注册量；6：实名认证量；7：首次充值；8：首次成功购彩；
     */
    public Short getSettlementType() {
        return settlementType;
    }

    /**
     * 1：不结算；2：销售量；3：有效激活；4：首次启动量；5：注册量；6：实名认证量；7：首次充值；8：首次成功购彩；
     * @param settlementType 1：不结算；2：销售量；3：有效激活；4：首次启动量；5：注册量；6：实名认证量；7：首次充值；8：首次成功购彩；
     */
    public void setSettlementType(Short settlementType) {
        this.settlementType = settlementType;
    }

    /**
     * 资源名称
     * @return resource_name 资源名称
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * 资源名称
     * @param resourceName 资源名称
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName == null ? null : resourceName.trim();
    }

    /**
     * 渠道来源的主页URL
     * @return resource_url 渠道来源的主页URL
     */
    public String getResourceUrl() {
        return resourceUrl;
    }

    /**
     * 渠道来源的主页URL
     * @param resourceUrl 渠道来源的主页URL
     */
    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl == null ? null : resourceUrl.trim();
    }

    /**
     * 结算费率
     * @return settlement_rate 结算费率
     */
    public BigDecimal getSettlementRate() {
        return settlementRate;
    }

    /**
     * 结算费率
     * @param settlementRate 结算费率
     */
    public void setSettlementRate(BigDecimal settlementRate) {
        this.settlementRate = settlementRate;
    }

    /**
     * 合作开始时间
     * @return coop_start_time 合作开始时间
     */
    public Date getCoopStartTime() {
        return coopStartTime;
    }

    /**
     * 合作开始时间
     * @param coopStartTime 合作开始时间
     */
    public void setCoopStartTime(Date coopStartTime) {
        this.coopStartTime = coopStartTime;
    }

    /**
     * 合作结束时间
     * @return coop_end_time 合作结束时间
     */
    public Date getCoopEndTime() {
        return coopEndTime;
    }

    /**
     * 合作结束时间
     * @param coopEndTime 合作结束时间
     */
    public void setCoopEndTime(Date coopEndTime) {
        this.coopEndTime = coopEndTime;
    }

    /**
     * 0：禁用；1：启用；2：合作过期；3：合作终止
     * @return channel_status 0：禁用；1：启用；2：合作过期；3：合作终止
     */
    public Short getChannelStatus() {
        return channelStatus;
    }

    /**
     * 0：禁用；1：启用；2：合作过期；3：合作终止
     * @param channelStatus 0：禁用；1：启用；2：合作过期；3：合作终止
     */
    public void setChannelStatus(Short channelStatus) {
        this.channelStatus = channelStatus;
    }

    /**
     * 修改时间
     * @return modify_time 修改时间
     */
    public Date getModifyTime() {
        return modifyTime;
    }

    /**
     * 修改时间
     * @param modifyTime 修改时间
     */
    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * 修改人
     * @return modify_by 修改人
     */
    public String getModifyBy() {
        return modifyBy;
    }

    /**
     * 修改人
     * @param modifyBy 修改人
     */
    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy == null ? null : modifyBy.trim();
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
     * app下载路径
     * @return app_url app下载路径
     */
    public String getAppUrl() {
        return appUrl;
    }

    /**
     * app下载路径
     * @param appUrl app下载路径
     */
    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl == null ? null : appUrl.trim();
    }

    /**
     * 是否为马甲包0：否；1：是
     * @return majia 是否为马甲包0：否；1：是
     */
    public Short getMajia() {
        return majia;
    }

    /**
     * 是否为马甲包0：否；1：是
     * @param majia 是否为马甲包0：否；1：是
     */
    public void setMajia(Short majia) {
        this.majia = majia;
    }

    /**
     * 接口对接秘钥
     * @return secret_key 接口对接秘钥
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * 接口对接秘钥
     * @param secretKey 接口对接秘钥
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey == null ? null : secretKey.trim();
    }
}