package com.hhly.usercore.persistence.message.po;

import com.hhly.skeleton.user.vo.UserMsgInfoVO;

import java.util.Date;

/**
 * 验证码信息（对表）
 * @desc
 * @author zhouyang
 * @date 2017年2月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public class UserMessagePO {
	
	/**
	 * 主键id
	 */
	private Integer id;
	
	/**
	 * 用户id
	 */
	private Integer userId; 
	
	/**
	 * 发送方式名称，手机或email
	 */
	private String account;
	
	/**
	 * 验证码
	 */
	private String code;
	
	/**
	 * 完整信息
	 */
	private String message;
	
	/**
	 * 消息类型	1：登录，2：注册，3完善资料绑定手机号码，4：找回/重置密码， 9：其它
	 */
	private Short messageType;
	
	/**
	 * 状态	0：未使用，1：已使用
	 */
	private Short status;
	
	/**
	 * 类型	1：短信，2：邮件
	 */
	private Short type;
	
	/**
	 * 创建时间（系统时间）
	 */
	private Date createTime;
	
	/**
	 * 更新时间
	 * @return
	 */
	private Date updateTime;
	
	public UserMessagePO(String account, String code, String message, Short messageType, Short status,
			Short type) {
		super();
		this.account = account;
		this.code = code;
		this.message = message;
		this.messageType = messageType;
		this.status = status;
		this.type = type;
	}

	public UserMessagePO(Integer userId, String account, String code, String message, Short messageType,
			Short status, Short type) {
		super();
		this.userId = userId;
		this.account = account;
		this.code = code;
		this.message = message;
		this.messageType = messageType;
		this.status = status;
		this.type = type;
	}

	public UserMessagePO(String account, String code, Short status) {
		this.account = account;
		this.code = code;
		this.status = status;
	}

	public UserMessagePO() {
		super();
		// TODO Auto-generated constructor stub
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

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Short getMessageType() {
		return messageType;
	}

	public void setMessageType(Short messageType) {
		this.messageType = messageType;
	}

	public Short getStatus() {
		return status;
	}

	public void setStatus(Short status) {
		this.status = status;
	}

	public Short getType() {
		return type;
	}

	public void setType(Short type) {
		this.type = type;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	/**
     * @author zhouyang
     * @version 1.0
     * @desc 消息中心PO
     * @date 2017/11/7
     * @company 益彩网络科技公司
     */
    public static class UserMsgInfoPO {

        /**
         * 主键
         */
        private Integer id;

        /**
         * 昵称
         */
        private String nickName;

        /**
         * 帐户名
         */
        private String accountName;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 模板id
         */
        private Integer templateId;

        /**
         * 模板名
         */
        private String templateName;

        /**
         * 发送方式
         */
        private String sendType;

        /**
         * 信息状态:0：发送失败，1：发送成功，3：已阅读
         */
        private Short status;

        /**
         * 发送失败原因
         */
        private String sendError;

        /**
         * 信息标题
         */
        private String msgTitle;

        /**
         * 信息内容
         */
        private String msgContent;

        /**
         * 发送批次号
         */
        private String msgBatch;

        /**
         * 发送时间
         */
        private Date sendTime;

        /**
         * 阅读时间
         */
        private Date readTime;

        /**
         * 备注
         */
        private String msgDesc;

        /**
         * 创建时间
         */
        private Date createTime;

        /**
         * 待发送时间
         */
        private Date preSendTime;

        /**
         * 创建人
         */
        private String createBy;

        /**
         * 购彩会员ID
         */
        private Integer userId;

        /**
         * 消息分类字段:1：系统消息；2：账户提醒；3：活动优惠；4：交易信息；5：其它
         */
        private Short msgType;

        public UserMsgInfoPO() {
        }

        public UserMsgInfoPO(UserMsgInfoVO vo) {
            this.id = vo.getId();
            this.nickName = vo.getNickName();
            this.accountName = vo.getAccountName();
            this.mobile = vo.getMobile();
            this.templateId = vo.getTemplateId();
            this.templateName = vo.getTemplateName();
            this.sendType = vo.getSendType();
            this.status = vo.getStatus();
            this.sendError = vo.getSendError();
            this.msgTitle = vo.getMsgTitle();
            this.msgContent = vo.getMsgContent();
            this.msgBatch = vo.getMsgBatch();
            this.sendTime = vo.getSendTime();
            this.readTime = vo.getReadTime();
            this.msgDesc = vo.getMsgDesc();
            this.createTime = vo.getCreateTime();
            this.preSendTime = vo.getPreSendTime();
            this.createBy = vo.getCreateBy();
            this.userId = vo.getUserId();
            this.msgType = vo.getMsgType();
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
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

        public String getSendType() {
            return sendType;
        }

        public void setSendType(String sendType) {
            this.sendType = sendType;
        }

        public Short getStatus() {
            return status;
        }

        public void setStatus(Short status) {
            this.status = status;
        }

        public String getSendError() {
            return sendError;
        }

        public void setSendError(String sendError) {
            this.sendError = sendError;
        }

        public String getMsgTitle() {
            return msgTitle;
        }

        public void setMsgTitle(String msgTitle) {
            this.msgTitle = msgTitle;
        }

        public String getMsgContent() {
            return msgContent;
        }

        public void setMsgContent(String msgContent) {
            this.msgContent = msgContent;
        }

        public String getMsgBatch() {
            return msgBatch;
        }

        public void setMsgBatch(String msgBatch) {
            this.msgBatch = msgBatch;
        }

        public Date getSendTime() {
            return sendTime;
        }

        public void setSendTime(Date sendTime) {
            this.sendTime = sendTime;
        }

        public Date getReadTime() {
            return readTime;
        }

        public void setReadTime(Date readTime) {
            this.readTime = readTime;
        }

        public String getMsgDesc() {
            return msgDesc;
        }

        public void setMsgDesc(String msgDesc) {
            this.msgDesc = msgDesc;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public Date getPreSendTime() {
            return preSendTime;
        }

        public void setPreSendTime(Date preSendTime) {
            this.preSendTime = preSendTime;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Short getMsgType() {
            return msgType;
        }

        public void setMsgType(Short msgType) {
            this.msgType = msgType;
        }
    }
}
