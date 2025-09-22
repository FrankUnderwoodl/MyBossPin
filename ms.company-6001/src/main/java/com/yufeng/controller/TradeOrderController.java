package com.yufeng.controller;

import com.yufeng.api.interceptor.JWTUserInterceptor;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.OrderStatus;
import com.yufeng.enums.PayMethod;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.model.bo.MerchantOrdersBO;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.TradeOrdersService;
import com.yufeng.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 * 这个类主要是针对‘订单’的控制器
 */
@Slf4j
@RestController
@RequestMapping("/tradeOrder")
public class TradeOrderController extends BaseInfoProperties {

    @Autowired
    private TradeOrdersService tradeOrdersService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 创建订单
     * @return GraceJSONResult
     */
    @PostMapping("/create")
    public GraceJSONResult create() {

        // 从jwt中获取用户id、企业id
        Users users = JWTUserInterceptor.currentUser.get();
        String userId = users.getId();
        String companyId = users.getHrInWhichCompanyId();
        // 记得判空
        if (StringUtils.isBlank(companyId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorMsg("用户信息异常，请重新登录");
        }
        // 设定订单的基础信息
        String itemName = "VIP企业会员";
        PayMethod payMethod = PayMethod.WEIXIN; // 写死是微信支付
        Integer totalAmount = 1;    // 设定为永远1分钱

        // 调用service层开始创建订单，并获得订单号
        String orderNum = tradeOrdersService.createOrder(userId, companyId, itemName, payMethod, totalAmount);

        return GraceJSONResult.ok(orderNum);
    }


    /**
     * 每当创建完订单后，前端就会传回订单号，然后调用这个接口，本地 -> 支付中心 -> 微信，拿到二维码地址
     */
    @PostMapping("generatorWXPayQRCode")
    public GraceJSONResult generatorWXPayQRCode(String merchantOrderId) {

        // 构建一个MerchantOrdersBO对象(用于进行远程调用)
        MerchantOrdersBO merchantOrdersBO = new MerchantOrdersBO();
        merchantOrdersBO.setMerchantOrderId(merchantOrderId);

        // 远程调用支付中心，获取微信支付二维码
        HttpEntity<MerchantOrdersBO> entity = new HttpEntity<>(merchantOrdersBO, getHeadersForWxPay());
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate
                .postForEntity(PAYMENT_URL_GET_WXPAY_QRCODE,
                        entity,
                        GraceJSONResult.class);

        GraceJSONResult paymentResult = responseEntity.getBody();
        if (paymentResult.getStatus() != 200) {
            return GraceJSONResult.errorMsg(paymentResult.getMsg());
        }

        return GraceJSONResult.ok(paymentResult.getData());
    }


    /**
     * 提供给老师的支付中心的回调接口，支付中心在用户支付成功后会调用这个接口(将某一个订单改为支付成功状态)
     * @param merchantOrderId 商户订单号
     * @return GraceJSONResult
     */
    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId) {

        tradeOrdersService.updateOrderStatus(merchantOrderId, OrderStatus.SUCCESS);
        return HttpStatus.OK.value();
    }




    /**
     * 查询订单列表
     * @return GraceJSONResult
     */
    @PostMapping("list")
    public GraceJSONResult list(Integer page, Integer limit) {

        Users hrUser = JWTUserInterceptor.currentUser.get();
        String companyId = hrUser.getHrInWhichCompanyId();

        PagedGridResult gridResult = tradeOrdersService.queryOrderListPaged(companyId, page, limit);

        return GraceJSONResult.ok(gridResult);
    }
}
