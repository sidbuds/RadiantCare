# Codex 官方第三方模型接入说明

## 当前接入方式
- Provider: codex++ 本地代理 (127.0.0.1:57321)
- Model: mimo-v2.5-pro (小米 MiMo)
- Wire API: Chat Completions (官方标准格式)
- 认证: 自定义 API Key (非 OpenAI Auth)

## 从 codex++ 迁移到官方接入的关键改动
1. wire_api: "responses" -> "chat" (codex++ 使用标准 Chat Completions)
2. requires_openai_auth: true -> false (第三方无需 OpenAI 认证)
3. 新增 api_key 字段 (codex++ 的 token)
4. base_url 保持不变 (127.0.0.1:57321/v1)
