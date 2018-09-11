package com.hhly.usercore.persistence.pay.po;

import java.io.Serializable;
import java.util.Date;

/**
 * @desc 用户提款信息记录表
 * @author xiongjingang
 * @date 2017年3月1日 下午3:13:16
 * @company 益彩网络科技公司
 * @version 1.0
 */
public class TransTakenPO implements Serializable {

	private static final long serialVersionUID = 1728881085812204831L;
	/**
	 *自增长ID
	 */
	private Integer id;
	/**
	*自定义用户充值流水编号
	*/
	private String transTakenCode;
	/**
	*1：支付宝充值；2：微信支付；3：练练支付；4：百度支付；5：人工充值
	*/
	private Short payChannel;
	/**
	*1：农商银行；2：光大银行；3：交通银行；4：平安银行；5：农业银行；6：中信银行；7：广发银行；8：华夏银行；9：浦发银行；10：民生银行；11：建设银行；12：中国银行；13：工商银行；14：邮储银行；15：招商银行；16：兴业银行；
	*/
	private Integer takenBank;
	/**
	*银行卡号
	*/
	private String bankCardNum;
	/**
	*审核时间
	*/
	private Date reviewTime;
	/**
	*汇款时间
	*/
	private Date remitTime;
	/**
	*到账时间
	*/
	private Date arrivalTime;
	/**
	*银行处理时间
	*/
	private Date dealTime;
	/**
	*下发时间
	*/
	private Date sendDownTime;
	/**
	*分支行信息
	*/
	private String bankInfo;
	/**
	*用户ID
	*/
	private Integer userId;
	/**
	*提款金额
	*/
	private Double extractAmount;
	/**
	*服务费
	*/
	private Double serviceCharge;
	/**
	*审核人
	*/
	private String reviewBy;
	/**
	*银行交易凭证
	*/
	private String transCert;
	/**
	*交易失败原因
	*/
	private String transFailInfo;
	/**
	*1：进行中；2：交易成功；3：交易失败；4：订单已关闭；
	*/
	private Short transStatus;
	/**
	*1：本站WEB；2：本站WAP；3：Android客户端；4：IOS客户端；5：未知；
	*/
	private Short takenPlatform;
	/**
	*市场渠道ID
	*/
	private String channelId;
	/**
	*第三方流水号
	*/
	private String thirdTransNum;
	/**
	*批次号
	*/
	private String batchNum;
	/**
	*批次状态； 0：处理失败；1：处理成功
	*/
	private Short batchStatus;
	/**
	*银行返回信息描述
	*/
	private String bankReturnInfo;
	/**
	*修改时间
	*/
	private Date modifyTime;
	/**
	*修改人
	*/
	private String modifyBy;
	/**
	*创建人
	*/
	private String createBy;
	/**
	*更新时间
	*/
	private Date updateTime;
	/**
	*创建时间
	*/
	private Date createTime;
	/**
	*描述
	*/
	private String remark;
	/**
	 *用户实际到账金额（提款金额-提款手续费）
	 */
	private Double realAmount;
	/**
	 *提款次数，一天只能提3次
	 */
	private Short takenTimes;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTransTakenCode() {
		return transTakenCode;
	}

	public void setTransTakenCode(String transTakenCode) {
		this.transTakenCode = transTakenCode;
	}

	public Short getPayChannel() {
		return payChannel;
	}

	public void setPayChannel(Short payChannel) {
		this.payChannel = payChannel;
	}

	public Integer getTakenBank() {
		return takenBank;
	}

	public void setTakenBank(Integer takenBank) {
		this.takenBank = takenBank;
	}

	public String getBankCardNum() {
		return bankCardNum;
	}

	public void setBankCardNum(String bankCardNum) {
		this.bankCardNum = bankCardNum;
	}

	public Date getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(Date reviewTime) {
		this.reviewTime = reviewTime;
	}

	public Date getRemitTime() {
		return remitTime;
	}

	public void setRemitTime(Date remitTime) {
		this.remitTime = remitTime;
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public Date getDealTime() {
		return dealTime;
	}

	public void setDealTime(Date dealTime) {
		this.dealTime = dealTime;
	}

	public Date getSendDownTime() {
		return sendDownTime;
	}

	public void setSendDownTime(Date sendDownTime) {
		this.sendDownTime = sendDownTime;
	}

	public String getBankInfo() {
		return bankInfo;
	}

	public void setBankInfo(String bankInfo) {
		this.bankInfo = bankInfo;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Double getExtractAmount() {
		return extractAmount;
	}

	public void setExtractAmount(Double extractAmount) {
		this.extractAmount = extractAmount;
	}

	public Double getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(Double serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public String getReviewBy() {
		return reviewBy;
	}

	public void setReviewBy(String reviewBy) {
		this.reviewBy = reviewBy;
	}

	public String getTransCert() {
		return transCert;
	}

	public void setTransCert(String transCert) {
		this.transCert = transCert;
	}

	public String getTransFailInfo() {
		return transFailInfo;
	}

	public void setTransFailInfo(String transFailInfo) {
		this.transFailInfo = transFailInfo;
	}

	public Short getTransStatus() {
		return transStatus;
	}

	public void setTransStatus(Short transStatus) {
		this.transStatus = transStatus;
	}

	public Short getTakenPlatform() {
		return takenPlatform;
	}

	public void setTakenPlatform(Short takenPlatform) {
		this.takenPlatform = takenPlatform;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getThirdTransNum() {
		return thirdTransNum;
	}

	public void setThirdTransNum(String thirdTransNum) {
		this.thirdTransNum = thirdTransNum;
	}

	public String getBatchNum() {
		return batchNum;
	}

	public void setBatchNum(String batchNum) {
		this.batchNum = batchNum;
	}

	public Short getBatchStatus() {
		return batchStatus;
	}

	public void setBatchStatus(Short batchStatus) {
		this.batchStatus = batchStatus;
	}

	public String getBankReturnInfo() {
		return bankReturnInfo;
	}

	public void setBankReturnInfo(String bankReturnInfo) {
		this.bankReturnInfo = bankReturnInfo;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getModifyBy() {
		return modifyBy;
	}

	public void setModifyBy(String modifyBy) {
		this.modifyBy = modifyBy;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Double getRealAmount() {
		return realAmount;
	}

	public void setRealAmount(Double realAmount) {
		this.realAmount = realAmount;
	}

	public Short getTakenTimes() {
		return takenTimes;
	}

	public void setTakenTimes(Short takenTimes) {
		this.takenTimes = takenTimes;
	}

}