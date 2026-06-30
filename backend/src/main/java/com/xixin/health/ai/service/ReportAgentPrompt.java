package com.xixin.health.ai.service;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReportAgentPrompt {
    private String question;
    private String systemPrompt;
    private List<Map<String, Object>> reports;
    private List<Map<String, Object>> packages;
}
