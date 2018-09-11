
package com.hhly.usercore.base.enums;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年4月26日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public class MessageType {

	public enum MemberOperateNode {
		
		register("注册", 11);
		
		private String name;
		
		private Integer nodeId;
		
		private MemberOperateNode(String name, Integer nodeId) {
			this.name = name;
			this.nodeId = nodeId;
		}

		public String getName() {
			return name;
		}

		public Integer getNodeId() {
			return nodeId;
		}
		
		
	}
}
