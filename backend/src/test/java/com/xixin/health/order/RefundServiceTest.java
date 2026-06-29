package com.xixin.health.order;

import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.exam.service.ExamService;
import com.xixin.health.order.dto.RefundAuditRequest;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.entity.RefundApplyEntity;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.order.mapper.RefundApplyMapper;
import com.xixin.health.order.mapper.RefundAuditLogMapper;
import com.xixin.health.order.mapper.RefundRecordMapper;
import com.xixin.health.order.service.RefundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundService 单元测试")
class RefundServiceTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private RefundApplyMapper refundApplyMapper;
    @Mock
    private RefundAuditLogMapper refundAuditLogMapper;
    @Mock
    private RefundRecordMapper refundRecordMapper;
    @Mock
    private AppointmentService appointmentService;
    @Mock
    private ExamService examService;

    @Test
    @DisplayName("退款审核通过后释放预约容量并撤回未完成体检任务")
    void approve_ReleasesCapacityAndRevokesExamTask() {
        RefundService refundService = new RefundService(
                orderMapper,
                appointmentService,
                refundApplyMapper,
                refundAuditLogMapper,
                refundRecordMapper,
                examService);

        RefundApplyEntity apply = new RefundApplyEntity();
        apply.setId(10L);
        apply.setApplyNo("RFA001");
        apply.setOrderId(20L);
        apply.setOrderNo("ORD001");
        apply.setApplyAmount(new BigDecimal("699.00"));
        apply.setApplyStatus(0);

        OrderEntity order = new OrderEntity();
        order.setId(20L);
        order.setOrderNo("ORD001");
        order.setAppointmentId(30L);
        order.setStatus(3);

        RefundAuditRequest request = new RefundAuditRequest();
        request.setAuditRemark("同意退款");

        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(99L);
            when(refundApplyMapper.selectOne(any())).thenReturn(apply);
            when(orderMapper.selectById(20L)).thenReturn(order);
            when(refundApplyMapper.update(eq(null), any())).thenReturn(1);
            when(refundRecordMapper.insert(any())).thenReturn(1);
            when(orderMapper.update(eq(null), any())).thenReturn(1);

            Map<String, Object> result = refundService.approve("RFA001", request);

            assertEquals("DONE", result.get("status"));
            verify(appointmentService).cancelPaidById(30L, "退款审核通过，系统自动取消预约");
            verify(examService).revokeForRefundedOrder(20L, "退款审核通过，系统自动撤回体检任务");
        }
    }
}
