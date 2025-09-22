package com.yufeng.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MerchantOrdersBO {

    private String merchantOrderId;         // 商户订单号
    private String merchantUserId;          // 商户方的发起用户的用户主键id
    private String merchantCompanyId;       // 商户方的发起用户所在的企业主键id
    private Integer amount;                 // 实际支付总金额（包含商户所支付的订单费邮费总额）
    private Integer payMethod;              // 支付方式 1:微信   2:支付宝
    private String returnUrl;               // 支付成功后的回调地址（学生自定义）
    private String comeFrom;                // 支付来源，从哪个平台来的，用于标记

}
