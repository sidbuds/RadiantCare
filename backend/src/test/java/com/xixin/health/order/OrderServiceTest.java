package com.xixin.health.order;

import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.order.dto.CreateOrderRequest;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.mapper.OrderItemMapper;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.order.service.MockPaymentService;
import com.xixin.health.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 单元测试")
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private ExamPackageMapper examPackageMapper;

    @Mock
    private MockPaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest createRequest;
    private AppointmentEntity appointment;
    private ExamPackageEntity packageEntity;
    private OrderEntity order;

    @BeforeEach
    void setUp() {
        createRequest = new CreateOrderRequest();
        createRequest.setAppointmentNo("APT001");

        appointment = new AppointmentEntity();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT001");
        appointment.setUserId(1L);
        appointment.setPackageId(1001L);

        packageEntity = new ExamPackageEntity();
        packageEntity.setId(1001L);
        packageEntity.setPackageCode("PKG001");
        packageEntity.setPackageName("入职体检套餐");
        packageEntity.setPrice(new BigDecimal("699.00"));

        order = new OrderEntity();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setUserId(1L);
        order.setStatus(0);
        order.setPayAmount(new BigDecimal("699.00"));
    }

    @Test
    @DisplayName("创建订单成功")
    void create_Success() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);

            when(appointmentService.getByNo("APT001")).thenReturn(appointment);
            when(orderMapper.selectCount(any())).thenReturn(0L);
            when(examPackageMapper.selectById(1001L)).thenReturn(packageEntity);
            when(orderMapper.insert(any())).thenReturn(1);
            when(orderItemMapper.insert(any())).thenReturn(1);

            Map<String, Object> result = orderService.create(createRequest);

            assertNotNull(result);
            assertNotNull(result.get("orderNo"));
            assertEquals(new BigDecimal("699.00"), result.get("payAmount"));
            verify(orderMapper).insert(any());
        }
    }

    @Test
    @DisplayName("重复订单创建失败")
    void create_DuplicateOrder() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);

            when(appointmentService.getByNo("APT001")).thenReturn(appointment);
            when(orderMapper.selectCount(any())).thenReturn(1L);
            when(orderMapper.selectOne(any())).thenReturn(order);

            Map<String, Object> result = orderService.create(createRequest);
            assertEquals("ORD001", result.get("orderNo"));
        }
    }

    @Test
    @DisplayName("支付成功")
    void pay_Success() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);

            Map<String, Object> payResult = new HashMap<>();
            payResult.put("transactionNo", "MOCK_123");
            payResult.put("payChannel", "MOCK_SANDBOX");

            when(orderMapper.selectOne(any())).thenReturn(order);
            when(paymentService.createPayment(any())).thenReturn(payResult);

            Map<String, Object> result = orderService.pay("ORD001");

            assertNotNull(result);
            assertEquals("PAID", result.get("status"));
            verify(paymentService).handlePaymentSuccess("ORD001", "MOCK_123", "MOCK_SANDBOX");
        }
    }

    @Test
    @DisplayName("已支付订单不能重复支付")
    void pay_AlreadyPaid() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);

            order.setStatus(1);
            when(orderMapper.selectOne(any())).thenReturn(order);

            assertThrows(BizException.class, () -> orderService.pay("ORD001"));
        }
    }
}
