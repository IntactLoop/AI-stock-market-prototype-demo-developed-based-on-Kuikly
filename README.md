# AI 股票行情 Demo（Kuikly Compose）

基于腾讯 **Kuikly 2.7.0**（Kotlin 2.1.21）的 Android 端 AI 股票行情原型。视觉唯一依据是 [`.cursor/rules/design-system.md`](.cursor/rules/design-system.md) v1.2：深色金融盘面 + AI 冷色批注层，涨红 `#E74C3C` / 跌绿 `#27AE60`。

## 功能

- **行情列表** `quote_list`：50 只 A 股 Mock，名称 / 代码 / 最新价 / 涨跌幅胶囊，点击进详情。
- **个股详情** `stock_detail`：展示型价格、高低成交量、Canvas 走势图（主折线 + 面积 + MA5/10/20 + 选中浮标）。
- **AI 模块**（模板填充，不调真实 LLM）：
  1. 信号解读（徽章 + 置信度条）
  2. 买卖建议 / 风险幽灵标签 / 点位
  3. 盯盘提醒（36dp、3 秒硬切）
  4. 图表点选联动解读
  5. 短期 / 中期趋势
  6. 当日行情总结
  7. 底部问答入口 + Sheet（5 个预设问题，结论 / 理由 / 风险）

## 如何运行（Android）

环境：JDK 17、Android SDK（compileSdk 34、minSdk 23）。

```bash
# Windows
gradlew.bat :androidApp:installDebug

# macOS / Linux
./gradlew :androidApp:installDebug
```

Android Studio 打开工程后，运行 `androidApp` 模块。启动 Activity 为 `KuiklyRenderActivity`，默认页面 `quote_list`。

Kuikly Maven 仓库：`https://mirrors.tencent.com/nexus/repository/maven-tencent/`。

## 架构

```
androidApp/          原生壳：KuiklyRenderActivity、Adapter、Module 注册
shared/              KMP 业务（Compose DSL 写在 commonMain）
  data/              Stock 模型、StockRepository、MockStockRepository、AppContainer
  ui/page/           QuoteListPage、StockDetailPage（ComposeContainer + @Page）
  ui/component/      可复用 UI（列表项、图表、AI 卡片、问答）
  ui/theme/          AppColors / AppDimens / AppType（与设计系统 Token 一一对应）
  ui/util/           QuoteFormat（2 位小数、涨跌符号、成交量单位）
  ui/chat/           问答模板（按当前 Stock 填结论/理由/风险）
```

分层约定：

| 层 | 规则 |
| :--- | :--- |
| 页面 | `ComposeContainer` + `setContent`；状态用 `remember` / `mutableStateOf`，不用 Kuikly `observable` |
| 组件 | 放在 `ui/component/`，KotlinDoc 标明对应设计系统章节 |
| 数据 | UI **只** 通过 `AppContainer.stockRepository` 取数，禁止直接 `MockStockRepository()` |
| 视觉 | 色值 / 间距 / 字号只用 `AppColors` / `AppDimens` / `AppType`，禁止现场发明 HEX |

路由：`RouterModule.openPage("stock_detail", JSONObject().put("symbol", code))`。页面名见 `ui/AppPages.kt`。

## 通用组件

| 组件 | 用途 |
| :--- | :--- |
| `StockCard` | 首页 72dp 列表项 |
| `QuoteTopBar` | 固定 56dp 顶栏，无滚动折叠 |
| `NavBackButton` | `ic_back.png` 返回，禁止 `Icon` |
| `PriceChangeGroup` | 详情展示型价格 + 涨跌 |
| `QuoteMetricsBlock` | 最高 / 最低 / 成交量 / 开盘 |
| `StockChart` | Canvas 走势图 |
| `SignalBadge` | 技术信号标签 |
| `AISignalCard` | 信号解读 + 置信度 |
| `AIAdviceChip` / `RiskGhostBadge` | 买卖实心胶囊 / 风险描边标签 |
| `AIAdviceCard` | 建议 + 点位 + 查看理由 |
| `AITicker` | 盯盘轮播 |
| `AITrendRow` | 短期 / 中期趋势 |
| `AISummaryCard` | 当日总结 |
| `AIChatEntry` | 底栏问答入口 |
| `PresetQuestionChip` | 问答预设（不用 AdviceChip） |
| `AIChatSheet` | `ModalBottomSheet` + `TextField` |

## Kuikly 2.7.0 实现边界

- UI 包名：`com.tencent.kuikly.compose.*`；只有 `androidx.compose.runtime.*` 可用官方包。
- 不要用：`OutlinedTextField`、`Icon`、`verticalScroll`、`animateItem`、AppBar `scrollBehavior`、原生 `ModalBottomSheet(sheetState/dragHandle)`。
- `ModalBottomSheet` 签名仅有 `visible` / `onDismissRequest` / `containerColor` / `scrimColor` 等，**没有** `dismissOnDrag`。
- 输入占位：`Modifier.placeHolder`（`compose.extension`）；长度：`setProp("maxTextLength", 120)`，禁止在 `onValueChange` 里截断。
- 盯盘切换是硬切，不用 `AnimatedVisibility + fadeIn`。

## 设计与规格文档

- 设计系统：`.cursor/rules/design-system.md`
- 任务与 AI 场景规格：`.cursor/workflow-state.md`
- 项目约束：`.cursor/rules/project-context.md`
