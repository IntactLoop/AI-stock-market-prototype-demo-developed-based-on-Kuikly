# Kuikly AI 股票行情 Demo — 工作状态与 AI 场景规格

> 用途：每次新对话必须先读取本文件，恢复项目状态、当前任务和 AI 场景设计规格。
> 配套文件：`.cursor/rules/project-context.md`、`.cursor/rules/kuiklyComposeDSL.mdc`、`.cursor/rules/design-system.md`
> 最后更新：2026-09-14
> 当前阶段：Phase 12.6 已完成（对比走势 / 建议支撑点 / 三维趋势 / 指标对比条）；等待下一阶段

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
| Phase 5 | 数据层扩展：多市场、指数、板块、多周期 K 线模型 | ✅ 已完成 |
| Phase 6 | 首页升级：市场 Tab、指数卡、板块、排序 | ✅ 已完成 |
| Phase 7 | 详情页升级：周期 / K 线类型 / 复权 / 成交量副图 | ✅ 已完成 |
| Phase 8 | 特征标签 + 自定义筛选 | ✅ 已完成（8.1–8.4） |
| Phase 9 | 用户操作入口与首页能力扩展：自选股、开户入口、排序升降序、营收字段 | ✅ 已完成（9.1–9.5） |
| Phase 10 | 选股分类与榜单：选公司/ETF/跟机构/跟资金 + 基金重仓榜等 4 榜 | ✅ 已完成（10.1–10.5） |
| Phase 11 | 多股对比与 AI 综合分析 | ✅ 已完成（11.1–11.5） |
| Phase 12 | AI 场景强化：推理链、图表自动标注、多轮追问 | ✅ 已完成（12.1–12.4） |
| Phase 12.5 | Phase 12 遗留 UI：对比勾选重叠、对比并入 SortBar、满额 Toast、排序箭头常显、营收单位 | ✅ 已完成 |
| Phase 12.6 | 对比页与详情页数据增强：归一化走势、建议支撑点、三维趋势、指标对比条 | ✅ 已完成 |

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

### 3.6 Phase 5–8 任务规格

> Phase 1–4 已完成。以下阶段补齐股票业务维度（多市场、指数、板块、排序、多周期 K 线、复权、标签与筛选），**不削弱**已锁定的 AI 场景。
> 执行顺序必须是 5 → 6 → 7 → 8；未完成上一 Phase 的完成标准前，不得开始下一 Phase。

#### 契约冻结清单（全 Phase 5–8 共用）

下列文件/签名在 Phase 5–8 **一律禁止改名、删除、改签名**（Phase 5 仅允许对 `Stock` / `StockRepository` **追加**字段或方法）：

| 冻结项 | 路径 / 签名 |
| :--- | :--- |
| 设计系统 | `.cursor/rules/design-system.md` |
| 项目上下文 | `.cursor/rules/project-context.md` |
| 页面名与路由参数 | `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/AppPages.kt`（`quote_list` / `stock_detail` / `ARG_SYMBOL`） |
| Stock 已有字段 | `data/model/Stock.kt` 中 Phase 1 已锁定的全部属性名与类型；`ChartPoint`、`WatchAlert` |
| Repository 已有方法 | `StockRepository.getStocks()`、`getStock(symbol: String)` |
| 取数入口 | `AppContainer.stockRepository`；UI 仍不得直接构造 `MockStockRepository` |
| 数字格式化已有方法 | `ui/util/QuoteFormat.kt` 现有 `price` / `change` / `percent` / `volume` 等签名 |
| 列表项与顶栏 | `ui/component/StockCard.kt`、`ui/component/QuoteTopBar.kt`（Phase 6–8 只复用，不改） |
| AI 模块组件 | `AISignalCard`、`AIAdviceCard`、`AIAdviceChip`、`RiskGhostBadge`、`AITicker`、`AITrendRow`、`AISummaryCard`、`AIChatEntry`、`AIChatSheet`、`PresetQuestionChip`、`ui/chat/ChatReply.kt` |

---

##### Phase 5：数据层扩展

**目标**：在不破坏现有 `Stock` / `StockRepository` 契约的前提下，补齐多市场、指数、板块、人气/换手、以及多周期 K 线所需的数据模型与 Mock。

**任务清单**

| Task | 要做什么 |
| :--- | :--- |
| 5.1 | `Stock.kt` **只新增**字段：`market`、`sector`、`tags`、`popularity`、`turnoverRate`、`marketCap`（总市值，亿元）、`week52High`、`week52Low`。不改名、不删字段、不改已有类型。为 `market` 等约定常量（如沪深 / 港股 / 美股）。 |
| 5.2 | 新增 `Index.kt`：至少包含 `MarketIndex`、`Sector`（指数代码/名称/最新点位/涨跌幅/所属市场；板块名称/涨跌幅/领涨股/所属市场）。 |
| 5.3 | 新增 `ChartData.kt`：至少包含 `ChartPeriod`、`ChartType`、`AdjustType`、`CandlePoint`（开高低收 + 成交量 + 时间）。保留现有 `ChartPoint` 供默认折线继续使用。 |
| 5.4 | `StockRepository` **只新增**方法：`getIndexes`、`getStocksByMarket`、`getSectors`、`getHotStocks`、`getChartData`。不改 `getStocks` / `getStock` 签名。 |
| 5.5 | `MockStockRepository` 补齐：A 股 **50** + 港股 **20** + 美股 **20** + **5** 个指数 + 每市场 **5–8** 个板块；每只股票填满 5.1 新字段；`getChartData` 能按 period / type / adjust 返回可用点列。现有 50 只 A 股的 AI 字段与三种情景一致性不得破坏。 |
| 5.6 | 更新 `commonTest` 中的 `MockStockRepositoryTest`（及必要的新测试）：覆盖新方法、多市场数量、指数/板块数量；原有「50 只 A 股 / AI 字段一致」断言仍通过。 |

**允许修改的文件**

- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/Stock.kt`（仅追加字段）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/Index.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/ChartData.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/StockRepository.kt`（仅追加方法）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/MockStockRepository.kt`
- `shared/src/commonTest/kotlin/com/example/myandroidapplication/data/repository/` 下测试

**禁止修改的文件**

- 契约冻结清单全部项
- 任意 `ui/` 下 `.kt`（本 Phase 无 UI）
- `AppContainer.kt`（除非确需，默认不改；取数入口保持 `stockRepository`）
- `design-system.md`、`project-context.md`、`README.md`

**完成标准**

- `:shared:compileDebugKotlinAndroid` 通过
- 现有 `getStocks()` 仍返回且至少含 50 只沪深 A 股；`getStock(symbol)` 行为不变
- Mock：A 股 50 + 港股 20 + 美股 20；指数 5；每市场 5–8 个板块
- 新字段/新方法有测试覆盖；Phase 1 的 AI 情景一致性测试仍通过
- 无任何 UI 行为变化

**风险提示**

- `Stock` 是 `data class`，追加字段会改构造器：必须同步改 `MockStockRepository.buildStock` 与全部测试构造，漏一处即编译失败
- 不要把 `chartPoints` 改成 `CandlePoint` 或删掉：详情页默认折线仍依赖它
- `getStocks()` 若改为「全市场混合」会破坏首页现有「A 股列表」心智；本 Phase 保持 `getStocks()` 语义，新市场走 `getStocksByMarket`
- 港股/美股代码规则与 A 股 6 位不同，测试里不要再用「symbol.length == 6」套全部市场

---

##### Phase 6：首页升级

**目标**：在复用 `StockCard` / `QuoteTopBar` 的前提下，为 `quote_list` 增加市场 Tab、指数总览、板块表现与排序，使首页具备多市场行情终端信息架构。

本 Phase 有两层横滑 + 一层竖滑，Kuikly 下手势冲突风险高，**必须**拆成 6.1 / 6.2 / 6.3，每步单独 `:shared:compileDebugKotlinAndroid`，通过后再做下一步。

**任务清单**

| Task | 要做什么 |
| :--- | :--- |
| 6.1 | 新增 `MarketTab`：沪深 / 港股 / 美股。改 `QuoteListPage`：TopBar → MarketTab → `LazyColumn(StockCard)`。切换后列表走 `getStocksByMarket`。默认仍为沪深。**本步不加指数、板块、排序。** 复用现有 `StockCard`，零改动。编译通过后再进入 6.2。 |
| 6.2 | 新增 `IndexCard`、`SectorRow`。在 MarketTab 与列表之间插入两层 `LazyRow` 横滑：指数（`getIndexes`）+ 板块（`getSectors`，含涨跌与领涨股）。指数点击不强制进详情。本步不加 SortBar。编译通过后再进入 6.3。 |
| 6.3 | 新增 `SortBar`：涨跌幅 / 人气 / 换手率。放在横滑区与 `LazyColumn` 之间。排序在内存对当前市场列表进行，不改 Repository 已有方法签名。最终结构：TopBar → MarketTab → 指数横滑 → 板块横滑 → SortBar → `LazyColumn(StockCard)`。水平边距 16dp、列表项 72dp 仍遵设计系统。 |

**允许修改的文件**

- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/QuoteListPage.kt`
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/MarketTab.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/IndexCard.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/SectorRow.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/SortBar.kt`（新建）
- 如需把指数/板块色值映射进 `QuoteFormat`，**只允许新增函数**，不得改已有签名

**禁止修改的文件**

- `StockCard.kt`、`QuoteTopBar.kt`（直接复用，零改动）
- `StockDetailPage.kt` 及全部详情/AI 组件
- `StockChart.kt`、`NavBackButton.kt`、`PriceChangeGroup.kt`
- `data/model/`、`StockRepository` 已有签名（本 Phase 不扩展数据契约）
- `design-system.md`、`project-context.md`
- 契约冻结清单其余项

**完成标准**

- `:shared:compileDebugKotlinAndroid` 通过（6.1、6.2、6.3 **各自**编译通过后再进入下一步）
- 三个市场 Tab 可切换，列表股票属于对应市场
- 指数卡、板块行可见且数据来自 Repository，不写死在页面里
- 三种排序生效；`StockCard` 外观与点击进详情不变
- 默认进入沪深列表，与 Phase 2 一致

**风险提示**

- **分步验证**：禁止在同一提交里做完 6.1–6.3。先只有 Tab+列表，再加双横滑，最后加排序，才容易定位手势冲突
- 首页最终是「Tab + 双 LazyRow + LazyColumn」嵌套滚动：Kuikly **禁止** `verticalScroll` / 原生 `nestedScroll(connection)`；横滑用 `LazyRow`，竖滑用 `LazyColumn`，必要时用 `nestedScroll(SELF_FIRST, SELF_FIRST)`，不要把指数/板块塞进会抢手势的错误容器
- 不要改 `StockCard`「顺手加板块名」——标签是 Phase 8 的事
- `LazyColumn` 的 `beyondBoundsItemCount = 3` 保留；不要加 `reverseLayout` / `flingBehavior`
- 指数/板块数字仍走 `QuoteFormat` 与涨跌色 Token，禁止新 HEX

---

##### Phase 7：详情页升级

**目标**：详情走势区支持分时/五日/日K/周K/月K、折线/K 线、不复权/前复权/后复权，并增加成交量副图；AI 模块位置与行为保持不变。

本 Phase 风险最高，**必须**拆成 7.1 / 7.2 / 7.3，每步单独 `:shared:compileDebugKotlinAndroid`，通过后再做下一步。

**任务清单**

| Task | 要做什么 |
| :--- | :--- |
| 7.1 | 新增 `PeriodTab`（分时 / 五日 / 日K / 周K / 月K）。`StockDetailPage` 在图表上方接入；选中后调用 `getChartData(symbol, period, …)`。**本步图表仍只画折线**（用返回点的收盘或现有 `ChartPoint` 映射），不改 K 线蜡烛、不改复权。编译通过后再进入 7.2。 |
| 7.2 | 新增 `ChartTypeToggle`（折线 / K 线）。改造 `StockChart`：折线路径保持 Phase 2 面积+MA；K 线绘制 `CandlePoint` 阴阳烛（涨红跌绿）。默认折线，保证未点切换时与 Phase 2/3 视觉一致。本步不做复权、副图可先不做。编译通过后再进入 7.3。 |
| 7.3 | 新增 `AdjustSelector`（不复权 / 前复权 / 后复权）。`getChartData` 带上 `AdjustType`；`StockChart` 增加成交量副图：主图锁定 `HeightChart` **220dp**，副图固定 **64dp**（4 的倍数，独立区域，不得挤进 220dp），主副图间距 `Space2` **8dp**。分时/五日可无蜡烛则禁用 K 线或回退折线。 |

**允许修改的文件**

- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/StockDetailPage.kt`（图表区上方插入控件；不打乱 6.3 模块顺序：Ticker → 价格 → 指标 → 图表 → AI…）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/StockChart.kt`
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/PeriodTab.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/ChartTypeToggle.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/AdjustSelector.kt`（新建）
- `shared/src/commonTest/kotlin/com/example/myandroidapplication/ui/component/ChartPointInterpretationTest.kt`（仅当折线点映射变化时最小改动）
- 图表联动仍用选中 index → `AISignalCard.selectedPointIndex`；必要时在详情页把 K 线点映射为现有解读输入，**不改** `AISignalCard` 对外语义

**禁止修改的文件**

- `StockCard.kt`、`QuoteTopBar.kt`、`QuoteListPage.kt` 及 Phase 6 新组件
- 全部 AI 组件与 `ChatReply.kt`（解读模板不在本 Phase 重写）
- `PriceChangeGroup.kt`、`QuoteMetricsBlock.kt`、`NavBackButton.kt`
- `design-system.md`（副图高度、Tab 高度用现有 Token：`HeightChart` 220dp、`MinTouch` 44dp、`Space*`）
- `Stock.kt` 已有字段、`getStocks` / `getStock` 签名
- 契约冻结清单其余项

**完成标准**

- 7.1、7.2、7.3 **各自**编译通过后再合并进入下一子任务
- 默认：折线 + 当前默认周期，看起来仍是 Phase 2 的主折线+面积+MA+选中浮标
- 五个周期可切换且数据变化；折线/K 线可切换；三种复权可切换（Mock 允许用缩放近似，但 UI 状态必须真切换）
- 成交量副图不遮挡主图、不破坏 AI 卡片顺序；副图高度 **64dp**，与主图 220dp 分开计算
- 点选联动仍更新 `AISignalCard`；问答入口仍在底部

**风险提示**

- **最高风险 Phase**：一次改周期+蜡烛+复权+副图极易让 `StockChart` 无法编译或手势错乱。禁止在同一提交里做完 7.1–7.3
- Canvas 触摸：`pointerInput` 的 key 必须包含 period / type / 点列，否则复用闭包读到旧数据
- 不要用 `Icon` 做 K 线/折线切换；用 `Text` 或几何 `Box`
- 主图高度锁定 `HeightChart` 220dp；成交量副图固定 **64dp**、间距 8dp，禁止把副图画进 220dp 主图区内导致蜡烛不可读
- 分时无 OHLC 时不要硬画空心蜡烛；降级折线并禁用 Toggle，避免 NaN 把 Canvas 打崩
- `getChartData` 若返回空列表，沿用现有「暂无走势」空态，不要转圈

---

##### Phase 8：特征标签 + 自定义筛选

**目标**：详情展示股票特征标签；首页提供自定义筛选（价格 / 涨跌幅 / 市值 / 标签）。筛选计算放在数据层，UI 只传参。特征标签仅在详情页展示；首页 `StockCard` 保持 72dp 固定高度不变，不显示标签。

**任务清单**

| Task | 要做什么 |
| :--- | :--- |
| 8.1 | 新增 `TagChip`。详情页在价格区附近（PriceChangeGroup 与 QuoteMetrics 之间或名称行下方）展示 `stock.tags`。样式区分于 `SignalBadge` / `AIAdviceChip` / `PresetQuestionChip`，避免红绿实心胶囊。 |
| 8.2 | `StockRepository` **追加**筛选方法（例如 `filterStocks(market, criteria)`），入参包含价格区间、涨跌幅区间、市值区间、标签集合。实现放在 Repository/Mock，不在 Composable 里写过滤循环。 |
| 8.3 | 新增 `FilterPanel`：首页排序栏附近入口展开（`ModalBottomSheet` 的 `visible` API，禁止原生 `sheetState` / `dragHandle`）。面板内区间与标签多选；确定后把 criteria 传给 8.2。 |
| 8.4 | `QuoteListPage` 在当前市场 + 排序基础上叠加筛选结果；空态文案 14sp `ColorTextHint`。不改 `StockCard`。 |

**允许修改的文件**

- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/TagChip.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/FilterPanel.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/QuoteListPage.kt`
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/StockDetailPage.kt`（只加标签行，不改图表/AI 顺序）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/StockRepository.kt`（仅追加筛选方法）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/MockStockRepository.kt`
- 筛选条件数据类可新建于 `data/model/`（如 `StockFilter.kt`）
- 对应 `commonTest`

**禁止修改的文件**

- `StockCard.kt`、`QuoteTopBar.kt`
- `StockChart.kt` 与 Phase 7 图表控件（PeriodTab / ChartTypeToggle / AdjustSelector）
- AI 组件与 `ChatReply.kt`
- `design-system.md`、`project-context.md`
- 不得在 `QuoteListPage` 内对全量 list 做复杂业务过滤（只调用 Repository）
- 契约冻结清单其余项

**完成标准**

- `:shared:compileDebugKotlinAndroid` 通过
- 详情页标签与当前 `Stock.tags` 一致；无标签时不留空洞大块
- 首页筛选后列表正确；清空筛选恢复当前市场全量（再套排序）
- 过滤逻辑有单元测试；UI 不出现与数据矛盾的标签
- 列表点击进详情、AI 模块、图表切换均仍可用

**风险提示**

- 不要复用 `SignalBadge` 做特征标签（那是技术信号色）；不要复用 `PresetQuestionChip`（问答区样式）
- `FilterPanel` 用 Kuikly 2.7.0 `ModalBottomSheet(visible, …)`，首次合成可能误触发 `onDismissRequest`，需与 `AIChatSheet` 同样的「可见才组合 / 忽略首次 dismiss」策略
- 筛选 + 市场 Tab + 排序三者叠加时，以「先市场 → 再筛选 → 再排序」为唯一顺序，避免 UI 与 Repository 各排一次

---

### 3.7 Phase 9–12 任务规格

> Phase 1–8 已完成。以下阶段补齐用户操作入口、选股分类与榜单、多股对比，并强化 AI 场景（推理链、图表标注、多轮追问），**不削弱**已锁定的 AI 场景。
> 执行顺序必须是 8 → 12 → 9 → 10 → 11；未完成上一 Phase 的完成标准前，不得开始下一 Phase。Phase 12 优先级提前，因其直接影响 AI 场景评分（25%）。

##### Phase 9：用户操作入口与首页能力扩展

**目标**：补齐股票 App 的用户操作入口，并扩展首页的数据维度与排序能力。

**任务清单**

| Task | 要做什么 |
| :--- | :--- |
| 9.1 | `Stock.kt` **只追加**字段 `revenue`（营收，亿元）、`revenueGrowth`（营收同比，%）。不改名、不删字段、不改已有类型。 |
| 9.2 | `SortBar` 支持升降序切换。点击相同排序项时在升序/降序之间切换，用文字 `↑` / `↓` 表示方向，禁止用 `Icon`。 |
| 9.3 | `QuoteTopBar` 右上角追加两个入口：「自选」「开户」。自选进入新页面 `watchlist`；开户弹出占位提示（Kuikly Toast 或简单弹窗），标注为占位功能。 |
| 9.4 | 新增 `watchlist` 页面：展示用户自选股列表，复用 `StockCard`，支持取消自选。空态文案 14sp `ColorTextHint`。 |
| 9.5 | 新增 `WatchlistRepository`（Mock 内存实现），提供 `add(symbol)` / `remove(symbol)` / `getAll()` / `contains(symbol)`。在 `AppContainer` 中注册实例。 |

**允许修改的文件**

- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/Stock.kt`（仅追加字段）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/MockStockRepository.kt`（补齐新字段 Mock）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/StockRepository.kt`（仅追加方法，如需）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/WatchlistRepository.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/WatchlistPage.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/SortBar.kt`（加升降序逻辑）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/QuoteTopBar.kt`（仅追加右侧入口，不改标题/高度）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/QuoteListPage.kt`（接入排序方向）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/AppPages.kt`（追加 `watchlist`）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/AppContainer.kt`（注册 WatchlistRepository）

**禁止修改的文件**

- `StockCard.kt`（直接复用）
- 全部 AI 组件与 `ui/chat/ChatReply.kt`
- `StockChart.kt` 与 Phase 7 图表控件
- `design-system.md`、`project-context.md`
- 契约冻结清单其余项

**完成标准**

- `:shared:compileDebugKotlinAndroid` 通过
- 排序升降序切换后列表顺序确实反转
- 首页可进入自选页，能加/取消自选，退出重进状态保留
- 开户入口有占位提示，不崩溃
- 原有 AI 场景与详情页无回归

**风险提示**

- `QuoteTopBar` 属契约冻结项，只允许在右侧追加图标/文字区，不改标题、不改 56dp 高度
- 自选状态需在进程内保持（Mock 内存即可，不做持久化）
- 开户占位不要引入新依赖，用 Kuikly 内置 Toast 或已有 `ModalBottomSheet`
- 排序方向切换不要重新请求数据，在内存对当前列表反转即可

---

##### Phase 10：选股分类与榜单

**目标**：新增选股页和榜单页，扩展股票 App 的发现能力。

**任务清单**

| Task | 要做什么 |
| :--- | :--- |
| 10.1 | 新增 `stock_picker` 页面，4 个 Tab：选公司、选 ETF、跟机构、跟资金。每个 Tab 下是筛选器 + 列表，列表复用 `StockCard`（ETF 可用简化版）。 |
| 10.2 | 新增 `rankings` 页面，4 个榜单：基金重仓股、公募基金重仓榜、公募基金加仓榜、公募基金新进榜。每个榜单是排行列表（序号 + 股票 + 持仓变动）。 |
| 10.3 | 数据层追加 `getStockPicks(category: String)`、`getRankings(type: String)`、`getFundHoldings(symbol: String)`（只追加，不改已有签名）。 |
| 10.4 | Mock 补齐 4 个分类的股票池（可从现有 A 股列表按 `sector` / `tags` 派生）和 4 个榜单数据。 |
| 10.5 | 首页（`QuoteListPage` 顶部或 `QuoteTopBar` 右侧）增加「选股」「榜单」两个入口。 |

**允许修改的文件**

- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/ETF.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/Ranking.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/FundHolding.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/StockRepository.kt`（仅追加方法）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/MockStockRepository.kt`
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/StockPickerPage.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/RankingsPage.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/PickerTab.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/RankingItem.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/QuoteTopBar.kt`（仅追加右侧入口）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/AppPages.kt`

**禁止修改的文件**

- `StockCard.kt`（直接复用）
- 全部 AI 组件与 `ChatReply.kt`
- `StockChart.kt` 与 Phase 7 图表控件
- `design-system.md`、`project-context.md`
- 契约冻结清单其余项

**完成标准**

- `:shared:compileDebugKotlinAndroid` 通过
- 选股页 4 个 Tab 可切换，列表数据来自 Repository，不写死在页面
- 榜单页 4 个榜单可切换，排行数据完整
- 从首页能进入两个页面并能返回

**风险提示**

- ETF 数据模型与 `Stock` 不同（无涨跌额，有净值/IOPV），需新建模型，不要硬塞进 `Stock`
- 榜单的「持仓变动」字段需 Mock，不要临时拼字符串
- 选股分类不要与 Phase 8 的筛选面板重复：前者是分类入口，后者是条件筛选
- `RankingItem` 的序号用文字展示（1、2、3），不要用 `Icon`

---

##### Phase 11：多股对比与 AI 综合分析

**目标**：支持勾选多只股票进行对比，AI 给出横向综合分析。

**任务清单**

| Task | 要做什么 |
| :--- | :--- |
| 11.1 | `QuoteListPage` 增加「对比模式」切换（SortBar 右侧或 TopBar 右侧）。进入后 `StockCard` 左侧显示勾选框（用外层 `Box` 叠加，不改组件本身）。 |
| 11.2 | 对比模式下底部浮层显示「已选 N 只，去对比」按钮，点击进入 `compare` 页面。 |
| 11.3 | 新增 `compare` 页面：并排展示 2–4 只股票的关键指标（价格、涨跌、市值、营收、PE、PB、MA5/10/20）。 |
| 11.4 | `compare` 页面底部增加 AI 综合分析卡片：对比推荐（哪只更优）、每只的买卖建议、横向风险提醒。 |
| 11.5 | 对比状态建议放在 `AppContainer` 顶层（`observableList<String>`），供列表页与对比页共享。 |

**允许修改的文件**

- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/QuoteListPage.kt`（对比模式）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/StockDetailPage.kt`（可选：加「加入对比」按钮）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/page/ComparePage.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/CompareModeBar.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/CompareColumn.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/AICompareCard.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/AppPages.kt`
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/AppContainer.kt`（对比状态管理）

**禁止修改的文件**

- `StockCard.kt`（勾选框通过外层 `Box` 叠加实现，不改组件本身）
- 全部 AI 组件与 `ChatReply.kt`（`AICompareCard` 是新建组件，不修改已有 AI 卡片）
- `design-system.md`、`project-context.md`
- 契约冻结清单其余项

**完成标准**

- `:shared:compileDebugKotlinAndroid` 通过
- 对比模式可进入/退出，勾选状态正确
- 对比页 2–4 只并排展示，指标齐全
- AI 综合分析文本随对比股票不同而变化
- 原有列表点击进详情不冲突（对比模式下点击卡片是勾选而非跳转）

**风险提示**

- `StockCard` 是契约冻结项，勾选框必须通过外层 `Box` 叠加实现
- 对比页并排宽度有限，最多 4 只，超过自动截断并提示
- 对比模式退出时清空勾选，避免下次进入残留
- `AICompareCard` 是新组件，不要复用 `AIAdviceCard` 的对外接口

---

##### Phase 12：AI 场景强化（优先级仅次于 Phase 8）

**目标**：让 AI 从「静态模板展示」升级为「过程可见、主动发现、可追问」。

**任务清单**

| Task | 要做什么 |
| :--- | :--- |
| 12.1 | `AISignalCard` 增加「分步推理链」：在解读文本上方显示 3–5 步推理，每步用 `✓` / `⚠` / `→` 字符 + 简短文本。禁止用 `Icon`。 |
| 12.2 | `StockChart` 增加 AI 自动标注点：在走势图上自动绘制 3–5 个标记（买入信号、压力位、放量点等），点击标记弹出小气泡解读。标记点数据来自 Mock 的 `aiChartMarks`。 |
| 12.3 | `AIChatSheet` 增加追问：每次 AI 回答后追加 2–3 个「你可能还想问」芯片，点击追问后回答带上下文前缀（如「基于刚才关于支撑位的讨论…」）。 |
| 12.4 | 数据层追加 `aiReasoningSteps: List<ReasoningStep>`、`aiChartMarks: List<ChartMark>`；新建 `ReasoningStep.kt`、`ChartMark.kt` 模型。 |

**允许修改的文件**

- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/Stock.kt`（仅追加字段）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/ReasoningStep.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/model/ChartMark.kt`（新建）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/data/repository/MockStockRepository.kt`（补齐新字段 Mock）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/AISignalCard.kt`（仅追加推理链区，不改已有结构）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/StockChart.kt`（仅追加标注绘制，不改折线/K线主逻辑）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/AIChatSheet.kt`（仅追加追问芯片）
- `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/chat/ChatReply.kt`（仅追加追问模板方法）

**禁止修改的文件**

- `StockCard.kt`、`QuoteTopBar.kt`
- `StockChart` 的主折线 / 面积 / MA / 选中浮标逻辑
- `AISignalCard` 的置信度条与徽章行
- `AISummaryCard`、`AIAdviceCard`、`AITrendRow`、`AITicker` 主体
- `design-system.md`、`project-context.md`
- 契约冻结清单其余项

**完成标准**

- `:shared:compileDebugKotlinAndroid` 通过
- 推理链随股票不同而变化，不是固定 3 步
- 图表标注点可点击且解读正确，坐标与折线点对齐
- 追问芯片可点击，回答带上下文前缀
- 原有 AI 场景（信号解读、买卖建议、盯盘、图表联动、问答）无回归

**风险提示**

- 推理链不要写成固定文案，应随股票数据变化
- 图表标注点坐标计算必须与已有折线点数据对齐，否则会错位
- 追问芯片不要超过 3 个，否则会挤压 `AIChatSheet` 高度
- `ChatReply` 是契约冻结项，只允许追加追问相关方法，不改已有回答模板
- 12.1 / 12.2 / 12.3 建议分三次单独编译验证

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

Phase 1–12 已全部完成。核心功能可编译运行。

- Task 11.1：对比模式开关；外层 Box 叠 `✓` 勾选；对比模式下点卡片只勾选不进详情；退出清空勾选；未改 `StockCard` ✅
- Task 11.2：底部「已选 N 只，去对比」；满 2 只可进 `compare` ✅
- Task 11.3：对比页并排 2–4 列（价格/涨跌/市值/营收/PE/PB/均线）；超过 4 只截断提示 ✅
- Task 11.4：`AICompareCard` 推荐/建议/风险随组合变化；未改已有 AI 卡片接口 ✅
- Task 11.5：勾选状态在 `AppContainer.compareSymbols` 共享 ✅

- Task 10.1：`stock_picker` 四 Tab + 分类芯片筛选；股票复用 `StockCard`；ETF 简化行（净值/IOPV）；数据走 `getStockPicks` / `getETFs` ✅
- Task 10.2：`rankings` 四榜 + `RankingItem` 文字序号；持仓变动为数值字段；点行进详情 ✅
- Task 10.3：`StockRepository` 追加 `getStockPicks` / `getETFs` / `getRankings` / `getFundHoldings`；未改已有签名 ✅
- Task 10.4：Mock 四分类股票池 + 四榜 + 基金持仓明细；unit test 通过 ✅
- Task 10.5：首页顶栏「选股」「榜单」进入对应页；保留自选/开户；未改 56dp 高度 ✅

- Task 9.1：`Stock` 追加 `revenue`（亿元）、`revenueGrowth`（同比 %）；Mock 已填；未改已有字段 ✅
- Task 9.2：`SortBar` 同项点击切换升/降序，文字 `↑` / `↓`；内存反转，未改 Repository；未用 `Icon` ✅
- Task 9.3：`QuoteTopBar` 右侧「自选」「开户」；自选进 `watchlist`；开户占位 Sheet；未改 56dp 高度 ✅
- Task 9.4：`watchlist` 复用 `StockCard`；外层「取消」；空态 14sp Hint；顶栏「添加」选股入自选 ✅
- Task 9.5：`WatchlistRepository` 内存 `add/remove/getAll/contains`；`AppContainer` 注册；退出重进保留 ✅

- Task 12.4：`ReasoningStep` / `ChartMark` + `Stock` 追加 `aiReasoningSteps` / `aiChartMarks`；Mock 3–5 步随情景变化 ✅
- Task 12.1：`AISignalCard` 解读上方分步推理链（`✓` / `⚠` / `→`）；未改徽章行与置信度条 ✅
- Task 12.2：`StockChart` AI 标注点对齐折线坐标；点击弹出解读气泡；未改折线/K 线/MA/选中浮标主逻辑 ✅
- Task 12.3：`AIChatSheet` 最近一条回答下追问 2–3 个；`ChatReply` 只追加 `answerFollowUp` / 前缀，未改已有模板 ✅

- Task 8.1：详情页 `TagChip` 展示 `stock.tags`；无标签不占位；未改 `StockCard` ✅
- Task 8.2：`StockFilter` + `filterStocks(market, criteria)`；空条件返回市场全量；单元测试通过 ✅
- Task 8.3：`FilterPanel` Kuikly `ModalBottomSheet(visible)`；区间预设 + 标签多选；确定后走 `filterStocks`；顺序市场 → 筛选 → 排序 ✅
- Task 8.4：筛选空列表 14sp `ColorTextHint`「没有符合条件的股票」；清空恢复当前市场全量再排序；Sheet 与列表用 `weight(1f)` 叠放 ✅

- Task 7.1：`PeriodTab`（分时 / 五日 / 日K / 周K / 月K）+ `getChartData`；默认日K 折线 ✅
- Task 7.2：`ChartTypeToggle` 折线/K 线；默认折线；阴阳烛涨红跌绿 ✅
- Task 7.3：`AdjustSelector` + 成交量副图 64dp（主图 220dp，间距 8dp）；分时/五日禁用 K 线 ✅

- Task 6.1：`MarketTab`（沪深 / 港股 / 美股）+ `QuoteListPage` 列表走 `getStocksByMarket`；默认沪深 ✅
- Task 6.2：`IndexCard` + `SectorRow` 双 `LazyRow` 横滑；数据来自 `getIndexes` / `getSectors` ✅
- Task 6.3：`SortBar`（涨跌幅 / 人气 / 换手率）内存排序；水平边距 16dp；`StockCard` 72dp 未改 ✅

- Task 5.1：`Stock` 追加 `market` / `sector` / `tags` / `popularity` / `turnoverRate` / `marketCap` / `week52High` / `week52Low`；常量 `MARKET_CN/HK/US` ✅
- Task 5.2：新增 `Index.kt`（`MarketIndex`、`Sector`）✅
- Task 5.3：新增 `ChartData.kt`（`ChartPeriod`、`ChartType`、`AdjustType`、`CandlePoint`）；保留 `ChartPoint` ✅
- Task 5.4：`StockRepository` 追加 `getIndexes` / `getStocksByMarket` / `getSectors` / `getHotStocks` / `getChartData` ✅
- Task 5.5：Mock 沪深 50 + 港股 20 + 美股 20 + 指数 5 + 每市场 5–8 板块；`getStocks()` 仍仅 A 股 ✅
- Task 5.6：`MockStockRepositoryTest` 覆盖新方法与数量；AI 情景一致性仍通过 ✅
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

**下一批任务**：Phase 6.5 已完成；Phase 7 → 8 → 12 → 9 → 10 → 11。

---

## 6. 下一步

1. 按 Phase 6.5（已完成）→ 7 → 8 → 12 → 9 → 10 → 11 顺序执行。
2. 规划任务已全部收口。可安装 `androidApp` Debug APK 演示：行情列表、详情 AI、自选、选股、榜单、多股对比。
3. Phase 12 已完成。未达上一 Phase 完成标准不得开始下一 Phase。
4. 视觉仍以 design-system.md v1.2 为准；StockCard / QuoteTopBar 在 7–12 中只复用、不改（Phase 9 除外，仅允许在 QuoteTopBar 右侧追加入口）。

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
| 2026-09-14 | 规划 Phase 5–8（数据层扩展 / 首页升级 / 详情图表 / 标签与筛选）；当前阶段切到 Phase 5 待开始；本轮未改业务代码 |
| 2026-09-14 | 规划微调：`Stock` 追加 `marketCap`；Phase 6 拆为 6.1 Tab / 6.2 横滑 / 6.3 排序；副图高度锁定 64dp；标签仅详情页展示 |
| 2026-09-14 | 完成 Phase 5：Stock 扩展字段、Index/ChartData 模型、Repository 新方法、沪深/港/美 Mock 与 K 线；`:shared:compileDebugKotlinAndroid` 与 unit test 通过；无 UI 改动 |
| 2026-09-14 | 完成 Phase 6.1：`MarketTab` + 首页按市场取数；默认沪深；未加指数/板块/排序；`:shared:compileDebugKotlinAndroid` 通过 |
| 2026-09-14 | 完成 Phase 6.2：`IndexCard` / `SectorRow` 横滑；数据走 Repository；未加 SortBar；`:shared:compileDebugKotlinAndroid` 通过 |
| 2026-09-14 | 完成 Phase 6.3：`SortBar` 涨跌幅/人气/换手率内存排序；Phase 6 收口；`:shared:compileDebugKotlinAndroid` 通过 |
| 2026-09-14 | 完成 Phase 7.1：`PeriodTab` + `getChartData` 映射收盘为折线；默认日K；未改蜡烛/复权；编译通过 |
| 2026-09-14 | 完成 Phase 7.2：`ChartTypeToggle` + `StockChart` 阴阳烛；默认折线；未加复权/副图；编译通过 |
| 2026-09-14 | 完成 Phase 7.3：`AdjustSelector` + 成交量副图 64dp；分时/五日回退折线；Phase 7 收口；编译通过 |
| 2026-09-14 | 完成 Phase 8.1：详情页 `TagChip` 展示 `stock.tags`；无标签不占位；未改 `StockCard`；编译通过 |
| 2026-09-14 | 完成 Phase 8.2：`StockFilter` + `filterStocks`；空条件返回市场全量；unit test 通过 |
| 2026-09-14 | 完成 Phase 8.3：`FilterPanel` ModalBottomSheet；区间/标签多选后调用 `filterStocks`；编译通过 |
| 2026-09-14 | 完成 Phase 8.4：筛选空态 14sp Hint；清空恢复当前市场全量；Phase 8 收口；编译通过 |
| 2026-09-14 | 新增 Phase 9–12 规划：用户操作入口、选股分类与榜单、多股对比、AI 场景强化；执行顺序调整为 6 → 7 → 8 → 12 → 9 → 10 → 11 |
| 2026-09-14 | 完成 Phase 12.4 + 12.1：推理链模型与 Mock、AISignalCard 分步推理；未改图表标注/追问；编译与 unit test 通过 |
| 2026-09-14 | 完成 Phase 12.2：StockChart AI 标注点 + 点击气泡；坐标对齐当前点列；未改折线/K 线主逻辑；编译通过 |
| 2026-09-14 | 完成 Phase 12.3：问答追问芯片 2–3 个，回答带上下文前缀；ChatReply 未改已有模板；Phase 12 收口；编译通过 |
| 2026-09-14 | 完成 Phase 9.1：Stock 追加 revenue / revenueGrowth；Mock 补齐；unit test 通过 |
| 2026-09-14 | 完成 Phase 9.2：SortBar 同项切换升/降序，文字 ↑↓；内存排序不重新取数；编译通过 |
| 2026-09-14 | 完成 Phase 9.3：顶栏自选/开户入口；watchlist 路由与空态；开户占位 Sheet；未改顶栏高度；编译通过 |
| 2026-09-14 | 完成 Phase 9.4 + 9.5：自选列表复用 StockCard、取消/添加、WatchlistRepository 内存实现；Phase 9 收口；编译与 unit test 通过 |
| 2026-09-14 | 完成 Phase 10.1：stock_picker 四 Tab + 分类芯片；ETF 独立模型与简化行；未做榜单/首页入口；编译与 unit test 通过 |
| 2026-09-14 | 完成 Phase 10.2–10.4：rankings 四榜、Ranking/FundHolding、getRankings/getFundHoldings Mock；编译与 unit test 通过 |
| 2026-09-14 | 完成 Phase 10.5：首页顶栏选股/榜单入口；Phase 10 收口；编译通过 |
| 2026-09-14 | 完成 Phase 11.1：对比模式 + 外层勾选叠加；退出清空；未改 StockCard；编译通过 |
| 2026-09-14 | 完成 Phase 11.2–11.5：对比浮层、compare 页、AICompareCard；Phase 11 收口；assembleDebug 与 unit test 通过 |
| 2026-09-14 | 完成 Phase 12.5：对比勾选 32dp 留白、对比并入 SortBar、满 4 只灰勾选+Toast、排序箭头常显、营收 `QuoteFormat.revenue`；`:shared:compileDebugKotlinAndroid` 通过 |
| 2026-09-14 | 完成 Phase 12.6：CompareChart 归一化走势、adviceReasons 支撑点、AITrendRow 三维置信度、MetricCompareBar；Task 1–4 分别编译通过；未改 StockChart 主折线与冻结 AI 组件 |

