package com.xixin.health.operator;

import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.operator.service.OperatorPackageService;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.report.mapper.ReportTemplateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperatorPackageService tests")
class OperatorPackageServiceTest {

    @Mock
    private ExamPackageMapper examPackageMapper;

    @Mock
    private ExamPackageItemMapper examPackageItemMapper;

    @Mock
    private ReportTemplateMapper reportTemplateMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ExamTaskMapper examTaskMapper;

    @Test
    @DisplayName("package status update does not require items")
    void updateStatusDoesNotRequireItems() {
        OperatorPackageService service = new OperatorPackageService(
                examPackageMapper,
                examPackageItemMapper,
                reportTemplateMapper,
                orderMapper,
                examTaskMapper
        );
        ExamPackageEntity entity = new ExamPackageEntity();
        entity.setId(1001L);
        entity.setStatus(1);

        when(examPackageMapper.selectOne(any())).thenReturn(entity);

        service.updateStatus(1001L, 0);

        verify(examPackageMapper).update(isNull(), any());
    }
}
