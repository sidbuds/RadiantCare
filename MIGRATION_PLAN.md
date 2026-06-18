# AIIntAss 鸿蒙原生 → Flutter 跨平台迁移计划

> 项目：AI 模拟面试助手（AIIntAss）
> 原技术栈：ArkTS + HarmonyOS 原生
> 目标技术栈：Flutter 3.x（Dart）→ 适配 Android / iOS / HarmonyOS
> 日期：2026-06-13

---

## 一、项目现状分析

### 1.1 现有页面清单（共 8 个页面）

| 页面 | 文件 | 核心功能 | 复杂度 |
|------|------|----------|--------|
| **首页** | `Index.ets` | 侧边栏导航 + 文件上传入口 + 知识库选择 + 聊天输入 + 快捷入口（简历/面试/聊天历史） | ⭐⭐⭐⭐ |
| **简历列表** | `ResumeList.ets` | 展示已上传简历列表，支持展开更多 | ⭐⭐ |
| **简历详情** | `ResumeDetail.ets` | 简历分析结果展示 + 历史分析记录 + 发起面试入口 | ⭐⭐⭐ |
| **面试进行** | `Interview.ets` | AI 面试问答交互（逐题回答、提交、跳题、未完成会话恢复） | ⭐⭐⭐⭐⭐ |
| **面试列表** | `InterviewList.ets` | 所有简历下的面试记录汇总列表 | ⭐⭐ |
| **面试报告** | `InterviewReport.ets` | 面试评分报告（总分 + 分类评分 + 题目详情 + 参考答案） | ⭐⭐⭐ |
| **知识库** | `KnowledgeBase.ets` | 知识库 CRUD（上传/删除/搜索/分类） | ⭐⭐⭐ |
| **RAG 聊天** | `RagChat.ets` | 基于知识库的 AI 聊天（SSE 流式、知识库切换、会话历史） | ⭐⭐⭐⭐⭐ |

### 1.2 数据模型（15 个接口）

```
Resume, ResumeListItem, ResumeDetail, AnalysisHistory,
InterviewItem, InterviewSession, InterviewQuestion, InterviewReport,
InterviewDetail, CategoryScore, QuestionEvaluation, ReferenceAnswer, AnswerDetail,
KbDoc, KnowledgeBaseItem, KnowledgeBaseStats,
RagSession, RagMessage, RagSessionDetail
```

### 1.3 API 服务层

| 服务 | 接口数 | 主要方法 |
|------|--------|----------|
| **ResumeService** | 6 | `getList`, `getDetail`, `upload`, `delete`, `analyze`, `download` |
| **InterviewService** | 6 | `create`, `getDetail`, `getReport`, `submitAnswer`, `findUnfinished`, `delete` |
| **KbService** | 10 | `upload`, `list`, `getDetail`, `delete`, `query`, `download`, `stats`, `search`, `getCategories`, `getByCategory`, `updateCategory`, `revectorize` |
| **RagChatService** | 7 | `create`, `list`, `getDetail`, `updateTitle`, `togglePin`, `updateKnowledgeBases`, `delete`, `sendMessage`(SSE) |

### 1.4 网络层

- 基础 URL：`http://10.0.2.2:8080`（模拟器 localhost）
- 使用 `@ohos.net.http`（鸿蒙原生 HTTP）
- 支持：GET/POST/PUT/DELETE + 文件上传(multipart) + 文件下载(ArrayBuffer) + SSE 流式请求
- 认证：Bearer Token（预留，当前未启用）

---

## 二、迁移方案

### 2.1 架构设计

```
lib/
├── main.dart                      # 入口
├── app.dart                       # MaterialApp + 路由配置
│
├── core/                          # 核心基础设施
│   ├── network/
│   │   ├── api_client.dart        # HTTP 客户端（基于 dio）
│   │   └── api_endpoints.dart     # 接口路径常量
│   ├── constants/
│   │   └── app_constants.dart     # 全局常量
│   └── theme/
│       └── app_theme.dart         # 主题/颜色/字体
│
├── data/                          # 数据层
│   ├── models/                    # 数据模型
│   │   ├── resume.dart
│   │   ├── interview.dart
│   │   ├── knowledge_base.dart
│   │   └── rag_chat.dart
│   └── services/                  # API 服务
│       ├── resume_service.dart
│       ├── interview_service.dart
│       ├── kb_service.dart
│       └── rag_chat_service.dart
│
├── features/                      # 功能模块（按页面组织）
│   ├── home/
│   │   └── home_page.dart         # 首页（侧边栏 + 快捷入口）
│   ├── resume/
│   │   ├── resume_list_page.dart
│   │   └── resume_detail_page.dart
│   ├── interview/
│   │   ├── interview_page.dart
│   │   ├── interview_list_page.dart
│   │   └── interview_report_page.dart
│   ├── knowledge_base/
│   │   └── knowledge_base_page.dart
│   └── rag_chat/
│       └── rag_chat_page.dart
│
└── shared/                        # 共享组件
    ├── widgets/
    │   ├── loading_indicator.dart
    │   ├── empty_state.dart
    │   ├── score_ring.dart        # 评分环形图
    │   └── sidebar_menu.dart      # 侧边栏菜单
    └── utils/
        ├── file_utils.dart        # 文件工具
        └── date_utils.dart        # 日期格式化
```

### 2.2 依赖选型

| 能力 | 包名 | 说明 |
|------|------|------|
| 网络请求 | `dio` ^5.x | 替代 `@ohos.net.http`，全平台通用 |
| 状态管理 | `provider` ^6.x | 轻量级，适合此项目规模 |
| 路由 | `go_router` ^14.x | 声明式路由，支持深链接 |
| 文件选择 | `file_picker` ^8.x | 跨平台文件选择 |
| 路径 | `path_provider` ^2.x | 获取应用沙箱路径 |
| JSON 序列化 | `json_annotation` + `json_serializable` | 类型安全 |
| 本地存储 | `shared_preferences` ^2.x | 简单 KV 存储（Token 等） |
| 刷新 | `pull_to_refresh` ^2.x | 下拉刷新 |

### 2.3 技术映射（ArkTS → Flutter/Dart）

| ArkTS 概念 | Flutter 对应 | 说明 |
|------------|-------------|------|
| `@Entry @Component struct` | `StatelessWidget` / `StatefulWidget` | 组件化 |
| `@State` | `setState` / `ChangeNotifier` / `Provider` | 状态管理 |
| `@StorageLink` | `Provider.of<T>(context)` | 全局状态共享 |
| `NavPathStack` | `Navigator` / `GoRouter` | 路由导航 |
| `@Builder` | `Widget` 方法或 `Builder` widget | 局部构建器 |
| `ForEach` | `ListView.builder` | 列表渲染 |
| `Column / Row` | `Column / Row` | 布局（几乎 1:1） |
| `Text / Image / Button` | `Text / Image / ElevatedButton` | 基础组件 |
| `TextInput` | `TextField` / `TextFormField` | 输入框 |
| `bindMenu` | `PopupMenuButton` / `showModalBottomSheet` | 弹出菜单 |
| `Visibility.Hidden` | `Visibility` widget / 条件渲染 | 显隐控制 |
| `promptAction.showToast` | `ScaffoldMessenger.showSnackBar` | Toast |
| `promptAction.showDialog` | `showDialog` | 对话框 |
| `SideBarContainer` | `Drawer` / 自定义 `Row` + `AnimatedContainer` | 侧边栏 |
| `Navigation()` | `Scaffold` + `AppBar` + `Drawer` | 导航框架 |
| `Stack` | `Stack` | 层叠布局 |
| `opacity / translate` | `AnimatedOpacity` / `SlideTransition` | 动画过渡 |

---

## 三、分阶段实施计划

### 阶段 1：项目骨架搭建（Day 1）

- [ ] 用 `flutter create` 创建项目
- [ ] 搭建目录结构（core / data / features / shared）
- [ ] 配置 `pubspec.yaml` 依赖
- [ ] 实现 `ApiClient`（基于 dio，替代 HttpClient.ets）
- [ ] 实现所有数据模型（Dart class，从 interface 直接转换）
- [ ] 实现所有 Service 类（从 ArkTS 方法签名 1:1 转换）

### 阶段 2：首页 + 导航框架（Day 2）

- [ ] 实现侧边栏菜单（Drawer）
- [ ] 实现首页布局（快捷入口卡片 + 聊天输入框 + 知识库选择器）
- [ ] 配置 GoRouter 路由表（对应 router_map.json 的 7 条路由）
- [ ] 实现文件上传功能（file_picker + dio multipart）

### 阶段 3：简历模块（Day 3）

- [ ] 简历列表页（ListView + 下拉刷新 + 展开更多）
- [ ] 简历详情页（Tab 切换：分析结果 / 历史记录 / 面试记录）
- [ ] 评分展示组件（环形图 / 柱状图）

### 阶段 4：面试模块（Day 4-5）

- [ ] 面试列表页
- [ ] 面试进行页（逐题问答 + 进度条 + 提交/下一题 + 未完成会话恢复）
- [ ] 面试报告页（总分 + 分类评分 + 题目详情 + 参考答案 Tab）

### 阶段 5：知识库 + RAG 聊天（Day 6-7）

- [ ] 知识库管理页（列表 + 上传 + 删除 + 搜索 + 分类筛选）
- [ ] RAG 聊天页（消息列表 + 输入框 + 知识库选择 + SSE 流式消息展示）

### 阶段 6：鸿蒙适配（Day 8）

- [ ] `flutter create --platforms ohos .` 添加鸿蒙平台
- [ ] 检查并替换不兼容的插件
- [ ] 配置鸿蒙签名和权限
- [ ] 真机/模拟器验证运行

---

## 四、API 接口对照表

### 4.1 简历接口

| 方法 | 路径 | Dart 方法签名 |
|------|------|--------------|
| GET | `/api/resumes` | `Future<Result<List<Resume>>> getList()` |
| GET | `/api/resumes/{id}` | `Future<Result<ResumeDetail>> getDetail(int id)` |
| POST | `/api/resumes/upload` | `Future<Result<Resume>> upload(String filePath)` |
| DELETE | `/api/resumes/{id}` | `Future<Result<void>> delete(int id)` |
| POST | `/api/resumes/{id}/analyze` | `Future<Result<void>> analyze(int id)` |
| GET | `/api/resumes/{id}/download` | `Future<void> download(int id, String savePath)` |

### 4.2 面试接口

| 方法 | 路径 | Dart 方法签名 |
|------|------|--------------|
| POST | `/api/interview/sessions` | `Future<Result<InterviewSession>> create(int resumeId, int questionCount, String resumeText)` |
| GET | `/api/interview/sessions/{sessionId}` | `Future<Result<InterviewDetail>> getDetail(String sessionId)` |
| GET | `/api/interview/sessions/{sessionId}/report` | `Future<Result<InterviewReport>> getReport(String sessionId)` |
| POST | `/api/interview/sessions/{sessionId}/answer` | `Future<Result<void>> submitAnswer(String sessionId, int questionIndex, String answer)` |
| GET | `/api/interview/sessions/unfinished?resumeId={id}` | `Future<Result<InterviewSession>> findUnfinished(int resumeId)` |
| DELETE | `/api/interview/sessions/{sessionId}` | `Future<Result<void>> delete(String sessionId)` |

### 4.3 知识库接口

| 方法 | 路径 | Dart 方法签名 |
|------|------|--------------|
| POST | `/api/knowledgebase/upload` | `Future<Result<dynamic>> upload(String filePath, {String? name, String? category})` |
| GET | `/api/knowledgebase/list` | `Future<Result<List<KbDoc>>> list({String? sortBy, String? vectorStatus})` |
| GET | `/api/knowledgebase/{id}` | `Future<Result<KnowledgeBaseItem>> getDetail(int id)` |
| DELETE | `/api/knowledgebase/{id}` | `Future<Result<void>> delete(int id)` |
| POST | `/api/knowledgebase/query` | `Future<Result<dynamic>> query(String question, {List<int>? knowledgeBaseIds})` |
| GET | `/api/knowledgebase/{id}/download` | `Future<void> download(int id)` |
| GET | `/api/knowledgebase/stats` | `Future<Result<KnowledgeBaseStats>> stats()` |
| GET | `/api/knowledgebase/search?keyword={kw}` | `Future<Result<List<KbDoc>>> search(String keyword)` |
| GET | `/api/knowledgebase/categories` | `Future<Result<List<String>>> getCategories()` |
| GET | `/api/knowledgebase/category/{category}` | `Future<Result<List<KbDoc>>> getByCategory(String category)` |
| PUT | `/api/knowledgebase/{id}/category` | `Future<Result<void>> updateCategory(int id, String category)` |
| POST | `/api/knowledgebase/{id}/revectorize` | `Future<Result<void>> revectorize(int id)` |

### 4.4 RAG 聊天接口

| 方法 | 路径 | Dart 方法签名 |
|------|------|--------------|
| POST | `/api/rag-chat/sessions` | `Future<Result<RagSession>> create(List<int> kbIds, {String? title})` |
| GET | `/api/rag-chat/sessions` | `Future<Result<List<RagSession>>> list()` |
| GET | `/api/rag-chat/sessions/{id}` | `Future<Result<RagSessionDetail>> getDetail(int id)` |
| PUT | `/api/rag-chat/sessions/{id}/title` | `Future<Result<void>> updateTitle(int id, String title)` |
| PUT | `/api/rag-chat/sessions/{id}/pin` | `Future<Result<void>> togglePin(int id)` |
| PUT | `/api/rag-chat/sessions/{id}/knowledge-bases` | `Future<Result<void>> updateKnowledgeBases(int id, List<int> kbIds)` |
| DELETE | `/api/rag-chat/sessions/{id}` | `Future<Result<void>> delete(int id)` |
| POST | `/api/rag-chat/sessions/{id}/messages/stream` | `Future<void> sendMessage(int id, String question)` (SSE) |

---

## 五、注意事项与风险

### 5.1 SSE 流式请求
- 鸿蒙原生用 `http.request` 拿完整响应，Flutter 中用 `dio` 的 `ResponseBody` 流式接收
- 推荐用 `dio` 的 `Options(responseType: ResponseType.stream)` 配合 `StreamTransformer` 逐行解析

### 5.2 文件上传
- 鸿蒙用 `fileIo` 手动拼 multipart，Flutter 中用 `dio.MultipartFile.fromPath` 一行搞定
- 文件选择用 `file_picker` 包

### 5.3 网络地址
- Android 模拟器：`http://10.0.2.2:8080`
- iOS 模拟器：`http://localhost:8080`
- 鸿蒙模拟器：`http://10.0.2.2:8080`
- 建议做成可配置项（设置页或环境变量）

### 5.4 鸿蒙特有适配
- `flutter create --platforms ohos .` 后会自动生成 `ohos/` 目录
- 部分 Flutter 插件可能没有 ohos 实现，需要找替代或自写 Platform Channel
- 网络权限：鸿蒙需要 `ohos.permission.INTERNET`（`flutter create` 生成的 ohos 工程默认已包含）

### 5.5 编码问题
- 原 ArkTS 文件中的中文注释存在编码损坏（乱码），迁移时需重新编写中文注释
- 所有用户可见的中文文案需在 Flutter 中重新整理

---

## 六、验收标准

1. ✅ Flutter 项目在 Android 模拟器/真机上运行正常
2. ✅ Flutter 项目在 iOS 模拟器/真机上运行正常
3. ✅ Flutter 项目在鸿蒙模拟器/真机上运行正常
4. ✅ 所有 8 个页面功能与原 ArkTS 版本一致
5. ✅ 所有 API 接口调用正常（含文件上传/下载/SSE 流式）
6. ✅ 中文 UI 显示正常，无乱码
7. ✅ 侧边栏导航、页面跳转、返回键行为与原版一致
