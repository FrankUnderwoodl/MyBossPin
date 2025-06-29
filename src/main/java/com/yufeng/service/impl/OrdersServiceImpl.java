package com.yufeng.service.impl;

import com.yufeng.pojo.Orders;
import com.yufeng.mapper.OrdersMapper;
import com.yufeng.service.OrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

}
