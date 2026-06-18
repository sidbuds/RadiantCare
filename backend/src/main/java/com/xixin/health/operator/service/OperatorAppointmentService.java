package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperatorAppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final AppointmentService appointmentService;
    private final UserMapper userMapper;

    public OperatorAppointmentService(AppointmentMapper appointmentMapper,
                                      AppointmentService appointmentService,
                                      UserMapper userMapper) {
        this.appointmentMapper = appointmentMapper;
        this.appointmentService = appointmentService;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> list(String centerCode, LocalDate appointDate, Integer status, String mobile) {
        List<Long> userIds = null;
        if (mobile != null && mobile.trim().length() > 0) {
            List<UserEntity> users = userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getMobile, mobile)
                    .eq(UserEntity::getIsDeleted, 0));
            userIds = new ArrayList<Long>();
            for (UserEntity user : users) {
                userIds.add(user.getId());
            }
            if (userIds.isEmpty()) {
                return new ArrayList<Map<String, Object>>();
            }
        }
        List<AppointmentEntity> appointments = appointmentMapper.selectList(new LambdaQueryWrapper<AppointmentEntity>()
                .eq(centerCode != null && centerCode.trim().length() > 0, AppointmentEntity::getCenterCode, centerCode)
                .eq(appointDate != null, AppointmentEntity::getAppointDate, appointDate)
                .eq(status != null, AppointmentEntity::getStatus, status)
                .in(userIds != null, AppointmentEntity::getUserId, userIds)
                .eq(AppointmentEntity::getIsDeleted, 0)
                .orderByDesc(AppointmentEntity::getId));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (AppointmentEntity appointment : appointments) {
            result.add(buildAppointment(appointment));
        }
        return result;
    }

    public Map<String, Object> detail(String appointmentNo) {
        return buildAppointment(appointmentService.getByNo(appointmentNo));
    }

    public void cancel(String appointmentNo, String reason) {
        appointmentService.cancel(appointmentNo, reason);
    }

    private Map<String, Object> buildAppointment(AppointmentEntity appointment) {
        UserEntity user = userMapper.selectById(appointment.getUserId());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("appointment", appointment);
        result.put("userName", user == null ? null : user.getName());
        result.put("userMobile", user == null ? null : user.getMobile());
        return result;
    }
}
