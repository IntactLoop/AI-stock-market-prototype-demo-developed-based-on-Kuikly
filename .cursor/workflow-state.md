# Kuikly AI 股票行情 Demo — 工作状态与 AI 场景规格

> 用途：每次新对话必须先读取本文件，恢复项目状态、当前任务和 AI 场景设计规格。
> 配套文件：`.cursor/rules/project-context.md`、`.cursor/rules/kuiklyComposeDSL.mdc`、`.cursor/rules/design-system.md`
> 最后更新：2026-09-13
> 当前阶段：全部阶段已完成

---

## 1. 项目目标

基于腾讯 Kuikly 跨端框架开发 AI 股票行情原型 Demo，核心包含：

1. 首页行情列表页：股票名称、代码、最新价、涨跌额、涨跌幅，支持滚动和点击进入详情。
2. 个股详情页：名称、代码、最新价、涨跌幅、最高价、最低价、成交量，承载走势区域与摘要。
3. AI 分析与解读模块：信号解读、买卖建议、风险提醒、趋势判断、图表联动、智能问答等。

比赛评分权重：
- 功能实现完整性 40%
- 代码质量与工程设计 25%
- AI 场景设计能力 25%
- 加分项 10%

因此，**AI 场景设计规格不可随意简化**。

---

## 2. 任务总览

| 阶段 | 内容 | 状态 |
| :--- | :--- | :--- |
| 设计系统 | `.cursor/rules/design-system.md` v1.2 | ✅ 已锁定 |
| Phase 1 | 项目骨架、Stock 数据类、StockRepository、MockStockRepository | ✅ 已完成 |
| Phase 2 | 首页行情列表页、个股详情页基础信息、走势图 Canvas | ✅ 已完成 |
| Phase 3 | AI 场景实现：信号解读、买卖建议、盯盘提醒、图表联动、问答 | ✅ 已完成 |
| Phase 4 | UI 打磨、README、架构说明、通用组件整理 | ✅ 已完成 |

---

## 3. AI 场景设计规格（锁定，核心评分项）

### 3.1 总体原则

- AI 能力必须贴合股票业务，不做通用聊天机器人。
- Demo 演示优先稳定：使用“预生成模板 + Mock 数据填充”，不依赖真实 LLM API。
- 每个 AI 模块必须有明确 UI 容器、数据来源、交互反馈。
- AI 输出要结构化：标签、卡片、进度条、徽章、文本区，避免纯大段文字。
- 所有 AI 文本必须与当前股票数据一致，不能出现矛盾建议。

### 3.2 模块清单与优先级

| 模块 | 优先级 | 展示位置 | 核心能力 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| 信号解读引擎 | P0 | 详情页走势图下方 | 技术信号 + 自然语言解读 + 置信度 | ✅ |
| 买卖建议与风险提示 | P0 | 信号解读卡片下方 | 买入/观望/卖出 + 风险等级 + 点位 | ✅ |
| AI 盯盘提醒 | P0 | 详情页顶部 | 滚动提醒风险、放量、趋势 | ✅ |
| 图表联动分析 | P1 | 走势图 + AI 分析区 | 点击时间点，AI 解读同步更新 | ✅ |
| 趋势判断 | P1 | 详情页摘要区 | 短期/中期趋势标签 | ✅ |
| AI 对话问答 | P2 | 详情页底部入口 | 预设问题 + 结构化回答 | ✅ |
| 行情总结 | P2 | 详情页底部 | 当日行情自动总结 | ✅ |

### 3.3 模块详细规格

#### A. 信号解读引擎（P0）

**位置**：个股详情页，走势图下方第一张卡片。

**UI 结构**：
- 标题：`AI 信号解读`
- 第一行：技术信号标签，如 `MACD金叉`、`放量`、`RSI超买`
- 第二行：解读文本，如“短期动能转强，但需关注成交量是否持续配合”
- 第三行：置信度进度条，显示 `72% 置信度`

**数据字段**：
`ma5`、`ma10`、`ma20`、`macd`、`rsi`、`volumeRatio`、`support`、`resistance`、`aiSignal`、`aiConfidence`

**生成规则示例**：
- MACD 金叉 + 成交量放大 → “短期动能转强”
- RSI > 70 → “超买风险，注意回调”
- 价格接近压力位 → “接近压力位，突破需放量”
- 价格接近支撑位 → “接近支撑位，关注企稳信号”

**交互**：点击信号标签展开详细解释；数据变化时自动刷新。

**Kuikly 实现要点**：
- 使用 `kuikly-ui-framework` 中的 `View`、`Text`、`ProgressBar`
- 状态用 `observable` 驱动
- 信号计算逻辑放在 `commonMain` 的 `SignalAnalyzer`

**验收标准**：
- 至少 3 种信号模板
- 置信度 0–100
- UI 不溢出、不重叠

#### B. 买卖建议与风险提示（P0）

**位置**：信号解读卡片下方。

**UI 结构**：
- 建议标签：`买入` / `观望` / `卖出`
- 风险等级：`低风险` / `中风险` / `高风险`
- 建议点位：`支撑买入价 178.50`、`压力卖出价 185.20`
- 点击“查看理由”展开简短说明

**数据字段**：
`aiAdvice`、`riskLevel`、`buyPoint`、`sellPoint`

**规则**：
- 趋势向上 + 接近支撑 → 买入
- 趋势震荡 + 接近压力 → 观望
- 趋势向下 + 跌破支撑 → 卖出
- 建议必须与信号解读不矛盾

**验收标准**：
- 建议、风险、点位三者一致
- 至少覆盖 3 种股票状态

#### C. AI 盯盘提醒（P0）

**位置**：个股详情页顶部。

**UI 结构**：
- 滚动条或轮播卡片，每 3 秒切换一条
- 示例：
  - 🔴 “当前股价接近压力位 185.20，注意回调风险”
  - 🟡 “成交量较昨日同期放大 35%，资金关注度提升”
  - 🟢 “短期均线多头排列，趋势偏强”

**数据字段**：
`price`、`volume`、`changePercent`、`high`、`low`

**规则**：
- 接近压力位 → 风险提醒
- 放量 → 资金提醒
- 均线多头 → 趋势提醒
- 跌破支撑 → 风险提醒

**交互**：点击提醒可跳转到对应分析区域。

**验收标准**：
- 至少 3 条提醒轮播
- 文本随股票不同而变化

#### D. 图表联动分析（P1）

**位置**：走势图 + AI 分析区。

**UI 结构**：
- 走势图使用 Canvas 绘制折线
- 点击某个时间点，该点高亮
- AI 分析区同步更新为该时间点的解读

**数据字段**：
K 线点数组，每个点包含 `time`、`price`、`volume`

**交互**：
- `onPointSelected(index)`
- 选中索引变化 → `observable` 触发 AI 文本更新

**Kuikly 实现要点**：
- 参考官方 `WeatherCanvasPage` 的 Canvas 绘制模式
- 使用 `Canvas` + `onTouch`
- 联动逻辑通过 `observable` 驱动

**验收标准**：
- 点击后文本更新
- 选中点有高亮反馈

#### E. AI 对话问答（P2）

**位置**：个股详情页底部入口。

**UI 结构**：
- 输入框 + 预设问题快捷按钮
- 预设问题示例：
  - “这只股票适合买入吗？”
  - “最近有什么风险？”
  - “成交量怎么样？”
  - “支撑位和压力位在哪？”

**数据字段**：
`Stock` 对象 + 预生成问答模板

**交互**：
- 用户点击预设问题或输入问题
- 返回结构化回答：结论 + 理由 + 风险提示

**验收标准**：
- 至少 5 个预设问题可回答
- 回答与当前股票数据一致

#### F. 行情总结（P2）

**位置**：个股详情页底部。

**UI 结构**：
- 一段总结文本 + 关键指标
- 示例：“今日该股开盘 180.00，最高 184.50，最低 178.20，收盘 183.60，成交量较昨日放大 20%，短期趋势偏强。”

**数据字段**：
`open`、`high`、`low`、`close`、`volume`、`changePercent`

**生成方式**：模板填充。

**验收标准**：
- 总结包含价格、涨跌、成交量、趋势判断

### 3.4 AI 场景对数据层的要求

`Stock` 数据类必须预留以下字段，避免后续 AI 场景缺数据：

```kotlin
data class Stock(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val turnover: Double,
    val pe: Double,
    val pb: Double,
    val ma5: Double,
    val ma10: Double,
    val ma20: Double,
    val macd: Double,
    val rsi: Double,
    val support: Double,
    val resistance: Double,
    val aiSignal: String,
    val aiConfidence: Int,
    val aiAdvice: String,
    val riskLevel: String,
    val buyPoint: Double,
    val sellPoint: Double,
    val aiSummary: String,
    // 以下为图表 / 盯盘 / 多信号所需的扩展字段（Phase 1 一并锁定）
    val volumeRatio: Double,
    val signals: List<String>,
    val aiInterpretation: String,
    val adviceReason: String,
    val shortTrend: String,
    val midTrend: String,
    val alerts: List<WatchAlert>,
    val chartPoints: List<ChartPoint>
)

data class ChartPoint(
    val time: String,
    val price: Double,
    val volume: Long
)

data class WatchAlert(
    val type: String, // 风险 | 资金 | 趋势
    val message: String
)
```

### 3.5 通用组件孵化清单

比赛评分强调“可以孵化出通用组件”，以下组件应放在 `ui/component/` 下：

| 组件 | 用途 |
| :--- | :--- |
| `StockCard` | 首页列表项 |
| `QuoteTopBar` | 固定顶栏 |
| `NavBackButton` | 返回 PNG |
| `PriceChangeGroup` | 详情展示型价格组 |
| `QuoteMetricsBlock` | 高低成交量 |
| `SignalBadge` | 技术信号标签 |
| `AISignalCard` | 信号解读卡片 |
| `AIAdviceChip` | 买卖建议标签 |
| `RiskGhostBadge` | 风险幽灵标签 |
| `AIAdviceCard` | 买卖建议卡片 |
| `AITicker` | 盯盘提醒轮播 |
| `StockChart` | 走势图 Canvas |
| `AITrendRow` | 趋势判断行 |
| `AISummaryCard` | 行情总结 |
| `AIChatEntry` | 问答入口 |
| `PresetQuestionChip` | 问答预设芯片 |
| `AIChatSheet` | 问答底部 Sheet |

---

## 4. 技术决策

- 框架：Kuikly Compose DSL
- 语言：Kotlin Multiplatform
- 网络：Kuikly NetworkModule（后续真实 API 接入时使用）
- 视觉依据：`.cursor/rules/design-system.md` **✅ 已锁定**（v1.2）。后续 UI 任务必须先读该文件，禁止临时改色值/尺寸
- 状态管理：Compose 页面用 `androidx.compose.runtime` 的 `remember` / `mutableStateOf`（已编译验证）；传统 Kuikly DSL 页面仍用 `observable` / `observableList`
- 数据层：`StockRepository` 接口 + `MockStockRepository` 实现；UI 不直接依赖 Mock
- 涨跌颜色：涨用红色 `#E74C3C`，跌用绿色 `#27AE60`
- 不使用 `androidx.compose.ui / foundation / material3 / animation`；Compose UI 组件来自 `com.tencent.kuikly.compose.*`
- 工程前置：`com.tencent.kuikly-open:compose` + `kotlin("plugin.compose")` 2.1.21

---

## 5. 当前任务详情

Phase 1–4 已完成。

- Task 2.1：首页行情列表 `quote_list` ✅
- Task 2.2：个股详情基础信息 `stock_detail` ✅
- Task 2.3：走势图 Canvas `StockChart` ✅
- Task 3.1：`SignalBadge` + `AISignalCard` ✅
- Task 3.2：`AIAdviceChip` + `RiskGhostBadge` + `AIAdviceCard` ✅
- Task 3.3：`AITicker`（36dp、3000ms 硬切、几何圆点；点击滚到信号/建议卡）✅
- Task 3.4：图表联动（选中点高亮 + `AISignalCard` 解读随 `selectedIndex` 更新）✅
- Task 3.5：趋势判断标签行 `AITrendRow`（复用 `SignalBadge`，短期/中期）✅
- Task 3.6：行情总结卡片 `AISummaryCard`（`aiSummary` + 涨跌幅/量能/收盘/趋势指标）✅
- Task 3.7：底部 `AIChatEntry` + `AIChatSheet`（5 个预设问题、模板结构化回答、700ms 加载）✅
- Task 4.1：按设计系统核对间距/色值；抽出 `NavBackButton` / `QuoteMetricsBlock`；README 架构说明 ✅

UI 取数：`AppContainer.stockRepository`。启动页：`KuiklyRenderActivity` 默认 `quote_list`。

---

## 6. 下一步

1. 按 README 在 Android 设备或模拟器安装运行，走通列表 → 详情 → 问答。
2. 视觉仍以 `.cursor/rules/design-system.md` v1.2 为准；改色值/尺寸必须先改设计系统版本号。

---

## 7. 更新日志

| 日期 | 更新内容 |
| :--- | :--- |
| 2026-09-13 | 创建初始工作状态文件，锁定 AI 场景设计规格 |
| 2026-09-13 | 产出并锁定设计系统 `.cursor/rules/design-system.md` v1.0（深色金融风、组件规格、Kuikly 能力边界） |
| 2026-09-13 | 编译验证 `remember` / `LinearProgressIndicator` / `Brush.verticalGradient`；设计系统升至 v1.1 |
| 2026-09-13 | 设计系统升至 v1.2：新增 5.8 AI 对话 Sheet；修正 Space8、语义色同 HEX 焦点规则、返回键 PNG、7.4「用了会编译失败」 |
| 2026-09-13 | 设计系统标记为 ✅ 已锁定；当前任务切回 Phase 1 数据层 |
| 2026-09-13 | 完成 Phase 1：Stock 全字段、StockRepository、50 条 Mock（三种情景一致）、AppContainer；单元测试通过 |
| 2026-09-13 | Phase 2：首页 `quote_list` + `StockCard`；详情基础信息 `stock_detail`；App 默认进入行情列表 |
| 2026-09-13 | 完成 Phase 2 Task 2.3：`StockChart` Canvas（主折线 + 面积渐变 + MA5/10/20 + 选中浮标）；`:shared:compileDebugKotlinAndroid` 通过 |
| 2026-09-13 | 完成 Phase 3 Task 3.1：`SignalBadge` + `AISignalCard`（流式徽章、置信度线性条、点击展开解读）；编译通过 |
| 2026-09-13 | 完成 Phase 3 Task 3.2：`AIAdviceChip` + 风险幽灵标签 + `AIAdviceCard`（点位与查看理由）；编译通过 |
| 2026-09-13 | 完成 Phase 3 Task 3.3：`AITicker` 盯盘条（3000ms 硬切、几何圆点、点击滚到分析区）；有 ticker 时详情顶距为 0 |
| 2026-09-13 | 完成 Phase 3 Task 3.4：图表联动，选中走势点后 `AISignalCard` 解读切换为该时点价格/量能/支撑压力分析 |
| 2026-09-13 | 完成 Phase 3 Task 3.5：趋势判断 `AITrendRow`（短期/中期 `SignalBadge`；盯盘「趋势」条滚到该行） |
| 2026-09-13 | 完成 Phase 3 Task 3.6：行情总结 `AISummaryCard`；模板补涨跌幅；卡片含关键指标且不包 SelectionContainer |
| 2026-09-13 | 完成 Phase 3 Task 3.7：`AIChatEntry` + `AIChatSheet` + `PresetQuestionChip`；`:shared:compileDebugKotlinAndroid` 通过；Phase 3 收口 |
| 2026-09-13 | 完成 Phase 4：顶栏标题避让、Display 行高、底栏上方 24+16dp；抽出 `NavBackButton` / `QuoteMetricsBlock`；补 README 架构说明 |

