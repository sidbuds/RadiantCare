package com.xixin.health.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.user.entity.HealthProfileEntity;
import com.xixin.health.user.mapper.HealthProfileMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HealthProfileService {

    private final HealthProfileMapper healthProfileMapper;

    public HealthProfileService(HealthProfileMapper healthProfileMapper) {
        this.healthProfileMapper = healthProfileMapper;
    }

    public Map<String, Object> getHealthProfile() {
        Long userId = AuthContext.getUserId();
        HealthProfileEntity profile = healthProfileMapper.selectOne(
                new LambdaQueryWrapper<HealthProfileEntity>()
                        .eq(HealthProfileEntity::getUserId, userId)
                        .eq(HealthProfileEntity::getIsDeleted, 0)
                        .last("limit 1"));
        if (profile == null) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("allergyHistory", profile.getAllergyHistory());
        result.put("medicalHistory", profile.getMedicalHistory());
        result.put("familyHistory", profile.getFamilyHistory());
        result.put("medicationHistory", profile.getMedicationHistory());
        result.put("smokingStatus", profile.getSmokingStatus());
        result.put("drinkingStatus", profile.getDrinkingStatus());
        result.put("remark", profile.getRemark());
        return result;
    }

    public void saveHealthProfile(Map<String, Object> data) {
        Long userId = AuthContext.getUserId();
        HealthProfileEntity profile = healthProfileMapper.selectOne(
                new LambdaQueryWrapper<HealthProfileEntity>()
                        .eq(HealthProfileEntity::getUserId, userId)
                        .eq(HealthProfileEntity::getIsDeleted, 0)
                        .last("limit 1"));
        if (profile == null) {
            profile = new HealthProfileEntity();
            profile.setUserId(userId);
            profile.setIsDeleted(0);
            setFields(profile, data);
            healthProfileMapper.insert(profile);
        } else {
            setFields(profile, data);
            healthProfileMapper.updateById(profile);
        }
    }

    @SuppressWarnings("unchecked")
    private void setFields(HealthProfileEntity profile, Map<String, Object> data) {
        if (data.containsKey("allergyHistory")) profile.setAllergyHistory((String) data.get("allergyHistory"));
        if (data.containsKey("medicalHistory")) profile.setMedicalHistory((String) data.get("medicalHistory"));
        if (data.containsKey("familyHistory")) profile.setFamilyHistory((String) data.get("familyHistory"));
        if (data.containsKey("medicationHistory")) profile.setMedicationHistory((String) data.get("medicationHistory"));
        if (data.containsKey("smokingStatus")) profile.setSmokingStatus((Integer) data.get("smokingStatus"));
        if (data.containsKey("drinkingStatus")) profile.setDrinkingStatus((Integer) data.get("drinkingStatus"));
        if (data.containsKey("remark")) profile.setRemark((String) data.get("remark"));
    }
}
