package com.xixin.health.publicapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.publicapi.entity.ExamCenterEntity;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 公开接口服务 - 套餐/中心/指南等公开数据
 */
@Service
public class PublicService {

    private final ExamPackageMapper examPackageMapper;
    private final ExamPackageItemMapper examPackageItemMapper;
    private final ExamCenterMapper examCenterMapper;
    private final ResourceCapacityMapper resourceCapacityMapper;

    public PublicService(ExamPackageMapper examPackageMapper,
                         ExamPackageItemMapper examPackageItemMapper,
                         ExamCenterMapper examCenterMapper,
                         ResourceCapacityMapper resourceCapacityMapper) {
        this.examPackageMapper = examPackageMapper;
        this.examPackageItemMapper = examPackageItemMapper;
        this.examCenterMapper = examCenterMapper;
        this.resourceCapacityMapper = resourceCapacityMapper;
    }

    /** 查询可用套餐列表 */
    public List<ExamPackageEntity> listPackages() {
        return examPackageMapper.selectList(new LambdaQueryWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getStatus, 1)
                .eq(ExamPackageEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageEntity::getId));
    }

    /** 查询套餐详情 */
    public Map<String, Object> getPackageDetail(String packageCode) {
        ExamPackageEntity pkg = examPackageMapper.selectOne(new LambdaQueryWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getPackageCode, packageCode)
                .eq(ExamPackageEntity::getStatus, 1)
                .eq(ExamPackageEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (pkg == null) {
            return null;
        }
        List<ExamPackageItemEntity> items = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, pkg.getId())
                .eq(ExamPackageItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageItemEntity::getSortNo));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", pkg.getId());
        result.put("packageCode", pkg.getPackageCode());
        result.put("packageName", pkg.getPackageName());
        result.put("category", pkg.getCategory());
        result.put("price", pkg.getPrice());
        result.put("remark", pkg.getRemark());
        result.put("items", items);
        return result;
    }

    /** 查询体检中心列表 */
    public List<ExamCenterEntity> listCenters() {
        return examCenterMapper.selectList(new LambdaQueryWrapper<ExamCenterEntity>()
                .eq(ExamCenterEntity::getStatus, 1)
                .eq(ExamCenterEntity::getIsDeleted, 0)
                .orderByAsc(ExamCenterEntity::getId));
    }

    /** 查询体检中心详情 */
    public ExamCenterEntity getCenterDetail(String centerCode) {
        return examCenterMapper.selectOne(new LambdaQueryWrapper<ExamCenterEntity>()
                .eq(ExamCenterEntity::getCenterCode, centerCode)
                .eq(ExamCenterEntity::getStatus, 1)
                .eq(ExamCenterEntity::getIsDeleted, 0)
                .last("limit 1"));
    }

    /** 查询体检中心可用时段 */
    public List<ResourceCapacityEntity> getCenterSlots(String centerCode, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return resourceCapacityMapper.selectList(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(ResourceCapacityEntity::getCenterCode, centerCode)
                .eq(ResourceCapacityEntity::getAppointDate, date)
                .eq(ResourceCapacityEntity::getStatus, 1)
                .eq(ResourceCapacityEntity::getIsDeleted, 0)
                .orderByAsc(ResourceCapacityEntity::getTimeSlotCode));
    }

    /** 获取体检指南 */
    public Map<String, Object> getCheckupGuide() {
        Map<String, Object> guide = new LinkedHashMap<>();
        List<Map<String, String>> steps = new ArrayList<>();

        Map<String, String> step1 = new LinkedHashMap<>();
        step1.put("title", "预约体检");
        step1.put("description", "选择套餐、体检中心和日期时段，完成预约。");
        steps.add(step1);

        Map<String, String> step2 = new LinkedHashMap<>();
        step2.put("title", "到院签到");
        step2.put("description", "携带身份证到体检中心前台签到，领取导检单。");
        steps.add(step2);

        Map<String, String> step3 = new LinkedHashMap<>();
        step3.put("title", "按路线体检");
        step3.put("description", "按照导检路线依次完成各科室检查项目。");
        steps.add(step3);

        Map<String, String> step4 = new LinkedHashMap<>();
        step4.put("title", "等待报告");
        step4.put("description", "体检完成后，医生录入结果并生成报告。");
        steps.add(step4);

        Map<String, String> step5 = new LinkedHashMap<>();
        step5.put("title", "查看报告");
        step5.put("description", "报告审核发布后，可在线查看详细报告和健康建议。");
        steps.add(step5);

        guide.put("steps", steps);
        guide.put("tips", "体检前一天请保持清淡饮食，避免剧烈运动。体检当天请空腹到院。");
        return guide;
    }

    /** 获取常见问题 */
    public List<Map<String, String>> getFaq() {
        List<Map<String, String>> faqList = new ArrayList<>();

        Map<String, String> q1 = new LinkedHashMap<>();
        q1.put("question", "体检前需要注意什么？");
        q1.put("answer", "体检前一天晚上8点后禁食，避免剧烈运动和饮酒。当天早上空腹到院，携带身份证。");
        faqList.add(q1);

        Map<String, String> q2 = new LinkedHashMap<>();
        q2.put("question", "可以取消预约吗？");
        q2.put("answer", "可以在体检日前一天取消预约，体检当天不可取消。");
        faqList.add(q2);

        Map<String, String> q3 = new LinkedHashMap<>();
        q3.put("question", "报告多久可以出来？");
        q3.put("answer", "一般体检后3-5个工作日可查看电子报告，部分特殊项目可能需要更长时间。");
        faqList.add(q3);

        Map<String, String> q4 = new LinkedHashMap<>();
        q4.put("question", "可以历年对比吗？");
        q4.put("answer", "可以。在报告详情页选择两份已发布的报告进行对比，系统会自动生成指标趋势和健康建议。");
        faqList.add(q4);

        Map<String, String> q5 = new LinkedHashMap<>();
        q5.put("question", "发现异常指标怎么办？");
        q5.put("answer", "可以通过平台向医生发起在线咨询，医生会根据报告给出专业建议。");
        faqList.add(q5);

        return faqList;
    }
}
