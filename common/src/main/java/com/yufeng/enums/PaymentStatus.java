
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.enums;

/**
 * @Description: 支付中心的支付状态 10：未支付 20：已支付 30：支付失败 40：已退款
 */
public enum PaymentStatus {

	WAIT_PAY(10, "未支付"),
	PAID(20, "已支付"),
	PAY_FAILED(30, "支付失败"),
	SUCCESS(40, "已退款");

	public final Integer type;
	public final String value;

	PaymentStatus(Integer type, String value){
		this.type = type;
		this.value = value;
	}

}
