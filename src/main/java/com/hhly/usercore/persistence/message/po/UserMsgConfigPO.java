package com.hhly.usercore.persistence.message.po;

import com.hhly.skeleton.user.vo.UserMsgConfigVO;

/**
 * @author zhouyang
 * @version 1.0
 * @desc 消息设置po类
 * @date 2017/11/14
 * @company 益彩网络科技公司
 */
public class UserMsgConfigPO {

    private Integer id;

    /**
     * 模板id
     */
    private Integer templateId;

    /**
     * 消息类型子分类名称
     */
    private String templateName;

    /**
     * 节点编码
     */
    private String typeNode;

    /**
     * 账户id
     */
    private Integer userId;

    /**
     * 手机短信：0.不接收，1.接收
     */
    private Short mobile;

    /**
     * 站内消息：0.不接收，1.接收
     */
    private Short site;

    /**
     * 手机app：0.不接收，1.接收
     */
    private Short app;

    /**
     * 微信：0.不接收，1.接收
     */
    private Short wechat;

    /**
     * 消息类型
     */
    private Short msgType;

    /**
     * 消息类型名称
     */
    private String msgName;

    /**
     * 开关名称
     */
    private String typeName;

    /**
     * 彩种编号
     */
    private Integer lotteryCode;

    /**
     * 彩种名称
     */
    private String lotteryName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTypeNode() {
        return typeNode;
    }

    public void setTypeNode(String typeNode) {
        this.typeNode = typeNode;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Short getMobile() {
        return mobile;
    }

    public void setMobile(Short mobile) {
        this.mobile = mobile;
    }

    public Short getSite() {
        return site;
    }

    public void setSite(Short site) {
        this.site = site;
    }

    public Short getApp() {
        return app;
    }

    public void setApp(Short app) {
        this.app = app;
    }

    public Short getWechat() {
        return wechat;
    }

    public void setWechat(Short wechat) {
        this.wechat = wechat;
    }

    public Short getMsgType() {
        return msgType;
    }

    public void setMsgType(Short msgType) {
        this.msgType = msgType;
    }

    public String getMsgName() {
        return msgName;
    }

    public void setMsgName(String msgName) {
        this.msgName = msgName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getLotteryCode() {
        return lotteryCode;
    }

    public void setLotteryCode(Integer lotteryCode) {
        this.lotteryCode = lotteryCode;
    }

    public String getLotteryName() {
        return lotteryName;
    }

    public void setLotteryName(String lotteryName) {
        this.lotteryName = lotteryName;
    }

    public UserMsgConfigPO() {
    }

    public UserMsgConfigPO(UserMsgConfigVO vo) {
        this.id = vo.getId();
        this.templateId = vo.getTemplateId();
        this.templateName = vo.getTemplateName();
        this.typeNode = vo.getTypeNode();
        this.userId = vo.getUserId();
        this.mobile = vo.getSwitchStatus();
        this.site = vo.getSwitchStatus();
        this.app = vo.getSwitchStatus();
        this.wechat = vo.getSwitchStatus();
        this.msgType = vo.getMsgType();
        this.msgName = vo.getMsgName();
        this.typeName = vo.getTypeName();
        this.lotteryCode = vo.getLotteryCode();
        this.lotteryName = vo.getLotteryName();
    }
}
