package com.xixin.health.operator;

import com.xixin.health.admin.service.AdminDictService;
import com.xixin.health.common.entity.DataDictionaryEntity;
import com.xixin.health.operator.service.OperatorOptionService;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperatorOptionService tests")
class OperatorOptionServiceTest {

    @Mock
    private ExamCenterMapper examCenterMapper;

    @Mock
    private AdminDictService adminDictService;

    @Test
    @DisplayName("departments without center returns distinct dictionary departments")
    void departmentsWithoutCenterDoesNotCreateCenterCartesianProduct() {
        when(adminDictService.listEnabledItems(OperatorOptionService.DICT_EXAM_DEPARTMENT))
                .thenReturn(Arrays.asList(dict("LAB", "检验科"), dict("ECG_ROOM", "心电图室")));

        List<Map<String, Object>> result = service().departments(null);

        assertEquals(2, result.size());
        assertEquals(null, result.get(0).get("centerCode"));
        assertEquals("LAB", result.get(0).get("departmentCode"));
        assertEquals("检验科", result.get(0).get("departmentName"));
        assertEquals("ECG_ROOM", result.get(1).get("departmentCode"));
    }

    @Test
    @DisplayName("departments with center attaches the requested center code")
    void departmentsWithCenterAttachesCenterCode() {
        when(adminDictService.listEnabledItems(OperatorOptionService.DICT_EXAM_DEPARTMENT))
                .thenReturn(Arrays.asList(dict("LAB", "检验科")));

        List<Map<String, Object>> result = service().departments("C001");

        assertEquals(1, result.size());
        assertEquals("C001", result.get(0).get("centerCode"));
        assertEquals("LAB", result.get(0).get("departmentCode"));
    }

    private OperatorOptionService service() {
        return new OperatorOptionService(examCenterMapper, adminDictService);
    }

    private DataDictionaryEntity dict(String code, String name) {
        DataDictionaryEntity entity = new DataDictionaryEntity();
        entity.setDictCode(code);
        entity.setDictName(name);
        return entity;
    }
}
