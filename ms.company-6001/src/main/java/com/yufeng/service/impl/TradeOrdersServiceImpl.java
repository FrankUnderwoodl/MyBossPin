package com.yufeng.service.impl;

import com.a3test.component.idworker.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.OrderStatus;
import com.yufeng.enums.PayMethod;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.mapper.OrdersMapper;
import com.yufeng.model.bo.MerchantOrdersBO;
import com.yufeng.model.pojo.Company;
import com.yufeng.model.pojo.Orders;
import com.yufeng.service.CompanyService;
import com.yufeng.service.TradeOrdersService;
import com.yufeng.utils.LocalDateUtils;
import com.yufeng.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Slf4j
@Service
public class TradeOrdersServiceImpl extends BaseInfoProperties implements TradeOrdersService {

    // 引入mapper层
    @Autowired
    private OrdersMapper ordersMapper;

    // 引入雪花算法Id生成器
    @Autowired
    private Snowflake snowflake;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CompanyService companyService;


    /**
     * 创建一条订单记录进orders表中
     *
     * @param userId      HRid
     * @param companyId   企业id
     * @param itemName    购买的商品名称
     * @param payMethod   支付方式
     * @param totalAmount 支付总金额，单位为分
     * @return 订单号
     * <p>
     * 问：订单编号怎么进行设计呢？
     * 答：用一个时间前缀 + 主键Id(因为有分布式id生成器，也就是雪花算法，所以不用担心并发问题)
     * <p>
     * 理解：所谓的并发问题，其实就是在同一毫秒内，可能会有多个请求进来，然后生成了相同的订单号
     * 雪花算法（Snowflake）能够解决这个问题，因为它：
     * · 包含时间戳、机器ID、进程ID和序列号
     * · 保证在分布式环境下生成全局唯一的ID
     * · 即使在同一毫秒内，不同机器或同一机器的不同请求也能生成不同的ID
     * 所以注释中提到"不用担心并发问题"，正是因为雪花算法保证了即使在高并发、分布式的环境下，订单号也不会重复。
     */
    @Override
    @Transactional
    public String createOrder(String userId, String companyId, String itemName, PayMethod payMethod, Integer totalAmount) {

        // 时间前缀
        String prefix = LocalDateUtils.format(LocalDateTime.now(), LocalDateUtils.DATETIME_PATTERN_3);
        // 主键Id
        String sid = snowflake.nextId();
        // 拼接前缀和主键Id，生成订单号
        String orderId = prefix + sid;



        // 下面的代码，完全可以交给消息队列，直接将订单号发给前端


        // 1.创建本地订单入库
        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);
        newOrder.setCompanyId(companyId);
        newOrder.setItemName(itemName);
        newOrder.setPayMethod(payMethod.type);
        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(totalAmount);
        newOrder.setPostAmount(0);
        newOrder.setStatus(OrderStatus.WAIT_PAY.type);
        newOrder.setCreatedTime(LocalDateTime.now());
        newOrder.setUpdatedTime(LocalDateTime.now());
        // 入库
        ordersMapper.insert(newOrder);


        // 2.创建本地企业微服务订单后，向老师的支付中心发起这个订单创建，保存订单中的主要数据，作为商户(预交易)订单信息
        MerchantOrdersBO merchantOrdersBO = new MerchantOrdersBO();
        merchantOrdersBO.setMerchantOrderId(orderId);
        merchantOrdersBO.setMerchantUserId(userId);
        merchantOrdersBO.setMerchantCompanyId(companyId);
        merchantOrdersBO.setAmount(totalAmount); // 支付总金额，单位为分
        merchantOrdersBO.setPayMethod(payMethod.type);
        merchantOrdersBO.setReturnUrl(PAY_RETURN_URL); // 这里的回调地址，其实是一个接口地址，也就是当支付成功后，支付中心会访问这个接口，修改订单状态(这就是所谓的回调)
        merchantOrdersBO.setComeFrom("慕聘网 by:Lzm"); // 这个字段没有什么卵用，就是告诉支付中心，这个订单是从哪里来的
        /**
         * 正式调用支付中心的创建订单接口(通过RestTemplate)
         * 这里不能使用openfeign调用，因为我们支付中心的代码是没有放在注册中心中的，所以需要使用RestTemplate调用
         */
        HttpHeaders headers = getHeadersForWxPay();
        HttpEntity<MerchantOrdersBO> entity = new HttpEntity<>(merchantOrdersBO, headers);
        // 这个postForEntity的意思是，发送一个post请求，返回一个ResponseEntity对象。参数分别是：url，请求体，返回值类型
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.postForEntity(PAYMENT_URL_CREATE_MERCHANT_ORDER, entity, GraceJSONResult.class);
        GraceJSONResult paymentResult = responseEntity.getBody();
        if (paymentResult.getStatus() != 200) {
            // 这里是调用支付中心创建订单失败了，所以要抛出异常，让事务回滚，删除刚才插入的订单
            GraceException.doException(ResponseStatusEnum.PAYMENT_ORDER_CREATE_ERROR);
            log.error("调用老师的支付中心创建订单失败，订单号：{}", orderId);
        }

        // 这里需要进行一个优化，因为有可能会产生大量的无效订单(用户创建订单后，没有支付就关闭了页面)，所以可以使用定时任务、消息队列等方式，定期清理这些无效订单
        // 这里就不做了，大家可以自行思考一下，如何去实现

        // 返回订单号
        return orderId;
    }


    /**
     * 修改订单状态，并试着给企业开通会员
     *
     * @param orderId     订单号
     * @param orderStatus 订单状态
     */
    @Override
    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus orderStatus) {

        // 凡是需要修改，都得new一个pojo出来
        Orders paidOrder = new Orders();
        paidOrder.setId(orderId);
        paidOrder.setStatus(orderStatus.type);
        paidOrder.setUpdatedTime(LocalDateTime.now());
        ordersMapper.updateById(paidOrder);

        // 判断当前企业是否为Vip，如果已经是VIP的话，就再此基础上加上30天；如果不是，就设置为30天
        Orders queryOrder = ordersMapper.selectById(orderId);
        String companyId = queryOrder.getCompanyId();
        Company company = companyService.getCompanyById(companyId);
        Boolean isVip = companyService.getIsVip(companyId);

        LocalDate vipExpireDate = LocalDate.now(); // 默认的过期时间
        if (isVip) {
            vipExpireDate = company.getVipExpireDate(); // 假如已经是vip，就获取当前的过期时间
        }

        // 在当前的过期时间上加31天
        LocalDate newExpireDate = LocalDateUtils.plus(vipExpireDate, 31, ChronoUnit.DAYS);
        companyService.setCompanyVip(companyId, newExpireDate);
        // 充值完毕以后立刻生效，删除redis中缓存数据
        redis.del(REDIS_COMPANY_IS_VIP + ":" + companyId);
    }


    /**
     * 查询订单列表，分页
     *
     * @param companyId 企业id
     * @param page      当前页
     * @param limit     每页显示条数
     * @return PagedGridResult
     */
    @Override
    public PagedGridResult queryOrderListPaged(String companyId, Integer page, Integer limit) {
        // 开启分页
        PageHelper.startPage(page, limit);

        List<Orders> ordersList = ordersMapper.selectList(
                new QueryWrapper<Orders>()
                        .eq("company_id", companyId)
                        .orderByDesc("created_time") // 根据创建时间倒序排列
        );

        return setterPagedGrid(ordersList, page);
    }
}
