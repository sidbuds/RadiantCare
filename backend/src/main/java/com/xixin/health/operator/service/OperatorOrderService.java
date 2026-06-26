package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.entity.OrderItemEntity;
import com.xixin.health.order.mapper.OrderItemMapper;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.order.service.OrderService;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营端订单服务
 */
@Service
public class OperatorOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;
    private final UserMapper userMapper;

    public OperatorOrderService(OrderMapper orderMapper,
                                OrderItemMapper orderItemMapper,
                                OrderService orderService,
                                UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderService = orderService;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> list(String orderNo, Long userId, Integer status, String payChannel) {
        List<OrderEntity> orders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(orderNo != null && orderNo.trim().length() > 0, OrderEntity::getOrderNo, orderNo)
                .eq(userId != null, OrderEntity::getUserId, userId)
                .eq(status != null, OrderEntity::getStatus, status)
                .eq(payChannel != null && payChannel.trim().length() > 0, OrderEntity::getPayChannel, payChannel)
                .eq(OrderEntity::getIsDeleted, 0)
                .orderByDesc(OrderEntity::getId));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (OrderEntity order : orders) {
            result.add(buildOrderSummary(order));
        }
        return result;
    }

    public Map<String, Object> detail(String orderNo) {
        OrderEntity order = orderService.getByNo(orderNo);
        List<OrderItemEntity> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItemEntity>()
                .eq(OrderItemEntity::getOrderId, order.getId())
                .eq(OrderItemEntity::getIsDeleted, 0)
                .orderByAsc(OrderItemEntity::getId));
        Map<String, Object> result = buildOrderSummary(order);
        result.put("items", items);
        return result;
    }

    public List<Map<String, Object>> export(String orderNo, Long userId, Integer status, String payChannel) {
        return list(orderNo, userId, status, payChannel);
    }

    private Map<String, Object> buildOrderSummary(OrderEntity order) {
        UserEntity user = userMapper.selectById(order.getUserId());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("order", order);
        result.put("userName", user == null ? null : user.getName());
        result.put("userMobile", user == null ? null : user.getMobile());
        return result;
    }
}
