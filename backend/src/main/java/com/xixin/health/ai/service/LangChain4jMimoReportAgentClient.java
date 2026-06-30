package com.xixin.health.ai.service;

import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.SystemConfigService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LangChain4jMimoReportAgentClient implements ReportAgentModelClient {

    private static final String DEFAULT_NOTICE = "AI建议不能替代医生诊断，如有不适请及时咨询医生。";
    private final SystemConfigService systemConfigService;

    public LangChain4jMimoReportAgentClient(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @Override
    public String chat(ReportAgentPrompt prompt) {
        if (!Boolean.TRUE.equals(systemConfigService.getBooleanValue("ai.api.enabled", Boolean.FALSE))) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "AI助手未启用");
        }
        String apiKey = systemConfigService.getValue("ai.api.key", "");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "AI API Key未配置");
        }
        String baseUrl = systemConfigService.getValue("ai.api.base_url", "https://api.xiaomimimo.com/v1");
        String model = systemConfigService.getValue("ai.api.model", "mimo-v1");
        int timeout = systemConfigService.getIntValue("ai.api.timeout_seconds", 30);
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .timeout(Duration.ofSeconds(timeout <= 0 ? 30 : timeout))
                .build();
        ReportAgent agent = AiServices.builder(ReportAgent.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .tools(new AuthorizedReportTool(prompt), new PackageRecommendTool(prompt))
                .build();
        return agent.chat(buildUserMessage(prompt));
    }

    private String buildUserMessage(ReportAgentPrompt prompt) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：").append(prompt.getQuestion()).append('\n');
        builder.append("已授权报告JSON：").append(prompt.getReports()).append('\n');
        builder.append("候选套餐JSON：").append(prompt.getPackages()).append('\n');
        if (prompt.getSystemPrompt() != null && prompt.getSystemPrompt().trim().length() > 0) {
            builder.append("运营补充提示：").append(prompt.getSystemPrompt()).append('\n');
        }
        builder.append("请只返回JSON，字段为answer、orderPitch、recommendedPackages、safetyNotice。safetyNotice固定包含：")
                .append(DEFAULT_NOTICE);
        return builder.toString();
    }

    interface ReportAgent {
        @SystemMessage("你是熙心健康体检平台的报告分析智能体。只能基于用户主动授权的报告和候选套餐回答。禁止诊断疾病，禁止创建订单。必须输出严格JSON。")
        String chat(@UserMessage String message);
    }

    static class AuthorizedReportTool {
        private final ReportAgentPrompt prompt;

        AuthorizedReportTool(ReportAgentPrompt prompt) {
            this.prompt = prompt;
        }

        @Tool("读取当前AI会话中用户主动发送给智能助手的报告")
        public String authorizedReports() {
            return String.valueOf(prompt.getReports());
        }
    }

    static class PackageRecommendTool {
        private final ReportAgentPrompt prompt;

        PackageRecommendTool(ReportAgentPrompt prompt) {
            this.prompt = prompt;
        }

        @Tool("读取启用的候选体检套餐，用于结合用户需求和报告异常项推荐")
        public String candidatePackages() {
            return String.valueOf(prompt.getPackages());
        }
    }
}
