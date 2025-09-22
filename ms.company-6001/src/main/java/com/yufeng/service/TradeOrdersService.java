package com.yufeng.service;

import com.yufeng.enums.OrderStatus;
import com.yufeng.enums.PayMethod;
import com.yufeng.utils.PagedGridResult;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface TradeOrdersService {


    /**
     * 创建一条订单记录进orders表中
     * @param userId HRid
     * @param companyId 企业id
     * @param itemName 购买的商品名称
     * @param payMethod 支付方式
     * @param totalAmount 支付总金额，单位为分
     * @return 订单号
     */
    public String createOrder(String userId,
                              String companyId,
                              String itemName,
                              PayMethod payMethod,
                              Integer totalAmount);


    /**
     * 更新本地订单状态
     * @param merchantOrderId 商户订单号
     * @param orderStatus 订单状态
     */
    void updateOrderStatus(String merchantOrderId, OrderStatus orderStatus);



    /**
     * 查询分页订单
     * @param companyId 企业id
     * @param page 当前页
     * @param limit 每页显示条数
     * @return PagedGridResult
     */
    PagedGridResult queryOrderListPaged(String companyId, Integer page, Integer limit);
}
