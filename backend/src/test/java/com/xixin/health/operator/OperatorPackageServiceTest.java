package com.xixin.health.operator;

import com.xixin.health.TestMybatisPlusSupport;
import com.xixin.health.appointment.entity.ExamPackageCenterRelEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.exam.entity.ExamDepartmentRouteEntity;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.appointment.mapper.ExamPackageCenterRelMapper;
import com.xixin.health.exam.mapper.ExamDepartmentRouteMapper;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import com.xixin.health.common.service.AuditLogService;
import com.xixin.health.operator.service.OperatorPackageService;
import com.xixin.health.operator.dto.SavePackageRouteRequest;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.publicapi.service.PublicPackageCacheService;
import com.xixin.health.report.mapper.ReportTemplateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
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

    @Mock
    private ExamPackageCenterRelMapper packageCenterRelMapper;

    @Mock
    private ExamCenterMapper examCenterMapper;

    @Mock
    private ExamDepartmentRouteMapper routeMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PublicPackageCacheService packageCacheService;

    @Test
    @DisplayName("package status update does not require items")
    void updateStatusDoesNotRequireItems() {
        TestMybatisPlusSupport.initTableInfo(ExamPackageEntity.class);
        OperatorPackageService service = new OperatorPackageService(
                examPackageMapper,
                examPackageItemMapper,
                reportTemplateMapper,
                orderMapper,
                examTaskMapper,
                packageCenterRelMapper,
                examCenterMapper,
                routeMapper,
                auditLogService,
                packageCacheService
        );
        ExamPackageEntity entity = new ExamPackageEntity();
        entity.setId(1001L);
        entity.setStatus(1);

        when(examPackageMapper.selectOne(any())).thenReturn(entity);

        service.updateStatus(1001L, 0);

        verify(examPackageMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("saving routes updates existing unique route instead of inserting duplicate")
    void saveRoutesUpdatesExistingRouteInsteadOfInsertingDuplicate() {
        TestMybatisPlusSupport.initTableInfo(ExamPackageEntity.class, ExamPackageItemEntity.class,
                ExamPackageCenterRelEntity.class, ExamDepartmentRouteEntity.class);
        OperatorPackageService service = service();
        ExamPackageEntity pkg = new ExamPackageEntity();
        pkg.setId(1001L);
        when(examPackageMapper.selectOne(any())).thenReturn(pkg);
        when(packageCenterRelMapper.selectCount(any())).thenReturn(1L);

        ExamPackageItemEntity packageItem = new ExamPackageItemEntity();
        packageItem.setPackageId(1001L);
        packageItem.setItemCode("BLOOD");
        when(examPackageItemMapper.selectList(any())).thenReturn(Collections.singletonList(packageItem));

        ExamDepartmentRouteEntity existing = new ExamDepartmentRouteEntity();
        existing.setId(4001L);
        existing.setCenterCode("C001");
        existing.setPackageId(1001L);
        existing.setItemCode("BLOOD");
        existing.setIsDeleted(1);
        when(routeMapper.selectList(any())).thenReturn(Arrays.asList(existing), Collections.singletonList(existing));

        SavePackageRouteRequest request = new SavePackageRouteRequest();
        request.setCenterCode("C001");
        SavePackageRouteRequest.RouteItem routeItem = new SavePackageRouteRequest.RouteItem();
        routeItem.setItemCode("BLOOD");
        routeItem.setDepartmentCode("LAB");
        routeItem.setDepartmentName("检验科");
        request.setRoutes(Collections.singletonList(routeItem));

        service.saveRoutes(1001L, request);

        verify(routeMapper).updateById(any(ExamDepartmentRouteEntity.class));
        verify(routeMapper, never()).insert(any(ExamDepartmentRouteEntity.class));
    }

    private OperatorPackageService service() {
        return new OperatorPackageService(
                examPackageMapper,
                examPackageItemMapper,
                reportTemplateMapper,
                orderMapper,
                examTaskMapper,
                packageCenterRelMapper,
                examCenterMapper,
                routeMapper,
                auditLogService,
                packageCacheService
        );
    }
}
