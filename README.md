# AI 股票行情 Demo

基于腾讯 Kuikly Compose DSL 的 Android 端行情原型：首页看盘、个股详情、自选 / 选股 / 榜单 / 对比，以及叠在盘面上的结构化 AI 解读。业务 UI 写在 `shared` 的 `commonMain`，由 `androidApp` 壳启动。行情与 AI 文案来自 `StockRepository` 的 Mock 模板，**不调用真实行情接口，也不调用真实 LLM**。

深色金融盘面（涨红 `#E74C3C` / 跌绿 `#27AE60`）上用蓝青冷色标注信号、建议、盯盘与问答，数字均可与当前 `Stock` 字段对上。

**一句话概括：用一套 Kotlin 代码，跑通股票行情 + AI 分析全链路。**

### 演示视频

视频展示：首页行情 → 个股详情 → AI 信号解读 → 图表联动 → 多股对比

<video src="docs/demo.webm" controls width="720">
浏览器不支持内嵌视频时，请打开 [docs/demo.webm](docs/demo.webm)。
</video>

---

## 一、简介

这是一份面向看盘场景的 **Android Debug Demo**：用户从 `quote_list` 进入沪深默认列表，点进 `stock_detail` 看价格、走势与 AI 卡片，也可走自选、选股、基金榜单和最多 4 只股票的对比页。解决的问题不是「再做一个聊天机器人」，而是把技术信号、点位、风险和问答嵌进同一套行情数据里，演示时输出稳定、可核对。

实现上采用 Kotlin Multiplatform + Kuikly Compose DSL（`com.tencent.kuikly.compose.*`，仅 `androidx.compose.runtime.*` 使用官方包）。页面经 `@Page` 注册，数据只通过 `AppContainer.stockRepository` / `watchlistRepository` 读取，UI 不直接构造 `MockStockRepository`。

代码在 `commonMain` 编写，**当前交付与验收路径是 Android**。仓库里虽有 `h5App` / `miniApp` / `iosApp` / `ohosApp` 模板目录，本 Demo **不宣称已支持 iOS、鸿蒙、H5 或小程序**。

---

## 二、功能

### 2.1 基础功能

- **行情列表**（`quote_list`）：`StockCard` 展示名称、代码、最新价、涨跌幅、成交额与换手率；点击进入详情。
- **个股详情**（`stock_detail`）：价格组、高低成交量与营收/市值、盯盘条、走势图与一组 AI 卡片。
- **走势图**：Canvas 主图（折线 / K 线）+ 成交量副图；可选中时点，并绘制 Mock 的 AI 标注。
- **自选**（`watchlist`）：进程内 `WatchlistRepository`；列表复用 `StockCard`，支持添加 / 取消。
- **选股**（`stock_picker`）：选公司 / 选 ETF / 跟机构 / 跟资金；股票可搜索、可点星加入自选。
- **榜单**（`rankings`）：基金重仓股、公募重仓、加仓榜、新进榜，数据走 `getRankings`。
- **对比**（`compare`）：首页对比模式勾选 2–4 只（上限 `AppContainer.COMPARE_MAX = 4`），进入指标列、归一化走势与 AI 综合分析。

### 2.2 核心功能

- **多市场切换**：沪深 / 港股 / 美股 Tab；`getStocks()` 仍为 50 只 A 股，港/美走 `getStocksByMarket`。
- **指数 / 板块横滑**：`IndexCard`、`SectorRow`，数据来自 `getIndexes` / `getSectors`。
- **多周期 K 线**：分时 / 五日 / 日 K / 周 K / 月 K，接口 `getChartData`。
- **复权**：前复权 / 后复权 / 不复权；Mock 用价格缩放近似，不模拟真实分红。
- **特征标签**：详情页 `TagChip` 展示 `stock.tags`；无标签不占位。
- **筛选**：价格 / 涨跌幅 / 市值 / 标签，`filterStocks` + `FilterPanel` Sheet。
- **排序升降序**：涨跌幅 / 人气 / 换手率，内存排序，同项再点切换升/降。

### 2.3 特色亮点

**信号解读**  
详情页 `AISignalCard` 展示技术信号徽章、解读句、置信度进度条，以及可点开的分步推理链。数据来自 `Stock.signals` / `aiInterpretation` / `aiConfidence` / `aiReasoningSteps`（Mock 按涨跌情景填模板）。点击徽章展开该信号说明；与当前均线、MACD、RSI 数字一致。

**买卖建议**  
`AIAdviceCard` + `AIAdviceChip` + `RiskGhostBadge`：买入 / 观望 / 卖出，配风险等级与支撑买入价、压力卖出价。字段为 `aiAdvice`、`riskLevel`、`buyPoint`、`sellPoint`、`adviceReasons`。点「查看理由」展开，并列出与行情数字一致的支撑点。

**盯盘提醒**  
详情顶栏 `AITicker`：36dp 条、约 3 秒硬切轮播 `stock.alerts`（风险 / 资金 / 趋势 / 事件）。几何圆点区分类型；点击对应条可滚到分析区相关模块。

**图表联动**  
`StockChart` 选中某一时点后，把下标传给 `AISignalCard`，解读切换为该点的价格 / 量能 / 支撑压力分析（`chartPointInterpretation`）。图上同时有 Mock 的 `aiChartMarks`（买入、支撑、压力、放量、跌破），点击标注出气泡。效果：点走势图任意点，AI 解读同步更新。

**问答**  
底栏 `AIChatEntry` 打开 `AIChatSheet`。五个预设问题由 `ui/chat/ChatReply.kt` 的两参模板填充结论 / 理由 / 风险；可手输问题，回答下最多 3 个追问芯片。首次首页可选风险偏好，三参包装只改语气前后缀，不改模板数字。无真实大模型。

**多股 AI 对比**  
对比页 `AICompareCard` 按勾选组合打分，给出推荐股、建议与风险短文；`CompareChart` 把近 20 个点归一化到 100；`MetricCompareBar` 画涨跌幅 / 量比等横向对比条。全部用已选 `Stock` 的现价与走势点，不另接模型。

**早报**  
首页顶栏「早报」弹出 `AIBriefingSheet`。按自选合并为 `BriefingItem`：名称、价格、信号标签、辅助说明；无自选显示「暂无自选股早报，请先添加自选」。本会话自动弹出一次，可关闭。文案来自 Mock 的 `briefingItems` / `briefingBullets`。

**事件预警**  
盯盘 `alerts` 中的事件类（业绩预告、减持窗口等）由 Mock 按股票下标生成，竖条与圆点用 AI 色，不占用涨跌红绿。与风险 / 资金 / 趋势同一条 `AITicker` 轮播。

**舆情**  
详情 `SentimentMeter`：0–100 情绪温度条 + 正 / 中 / 负面新闻条数。数据为 `sentimentScore`、`newsPositive` / `newsNeutral` / `newsNegative`，与该股涨跌情景同向。

**量化推演**  
`AISignalCard` 底部「未来 5 日上涨概率」进度条，字段 `upsideProbability5d`（0–100）。随置信度与短期趋势 Mock，卖出建议时不超过 70。不是回测引擎。

**复盘**  
`AISummaryCard` 内「盘中 / 复盘」Tab：盘中用 `aiSummary`，复盘用 `reviewSummary`（含开高低收与涨跌幅）。关键指标行展示价量，不包文本选择。

**持仓诊断**  
建议卡旁 `HoldingDiagnoseEntry` 打开 `HoldingDiagnoseSheet`。本地输入成本与数量，按现价计算盈亏，再结合 `aiAdvice` 给出策略句。不改 `Stock` 模型，不写仓位到仓库。

**产业链关联**  
`IndustryChainCard` 列出 `chainPeers` 的上游 / 下游各至少一家，名称、代码、涨跌幅与 `getStock` 一致。这是关联列表，不是可拖拽图谱。

---

## 三、代码

### 3.1 技术栈

| 项 | 值 |
| :--- | :--- |
| 框架 | Kuikly **2.7.0-2.1.21**（`core` / `compose` / `core-render-android`） |
| 语言 | Kotlin **2.1.21**（KMP） |
| UI | Kuikly **Compose DSL**；`com.tencent.kuikly.compose.*` |
| 构建 | Gradle **8.9**，Android Gradle Plugin 随工程；JDK **17** |
| SDK | minSdk **23**，compileSdk **34**，targetSdk **30** |
| 分层 | `ui/page` → `ui/component` → `data`（接口）→ Mock；状态用 Compose `remember` / `mutableStateOf` |
| 数据 | `StockRepository` + `MockStockRepository`；自选 `WatchlistRepository` |
| 取数入口 | `AppContainer.stockRepository` / `watchlistRepository` |
| 网络 | 工程可接 Kuikly `NetworkModule`；本 Demo **行情不发真实请求** |
| Maven | `https://mirrors.tencent.com/nexus/repository/maven-tencent/` |
| 交付端 | **Android Debug**（`:androidApp`） |

环境：JDK 17、Android SDK。Kuikly 依赖需能访问上述 Maven。

```bash
# Windows
gradlew.bat :androidApp:installDebug

# macOS / Linux
./gradlew :androidApp:installDebug
```

也可在 Android Studio 打开工程，运行 `androidApp`。启动 Activity 为 `KuiklyRenderActivity`，**默认页面 `quote_list`**。详情跳转：`RouterModule.openPage("stock_detail", JSONObject().put("symbol", code))`。

### 3.2 代码结构目录说明

```
MyAndroidApplication/
├── androidApp/                          # 原生壳：KuiklyRenderActivity、Adapter、Module
├── shared/                              # KMP 业务；Compose UI 在 commonMain
│   └── src/
│       ├── commonMain/
│       │   ├── assets/                  # 本地图，如返回键 ic_back.png（common 目录）
│       │   └── kotlin/.../myandroidapplication/
│       │       ├── data/                # 模型、Repository、AppContainer
│       │       │   ├── model/           # Stock、ETF、ChartData、Ranking 等
│       │       │   └── repository/      # StockRepository 接口与 Mock
│       │       ├── ui/page/             # @Page 页面（6 个业务页）
│       │       ├── ui/component/        # 列表、图表、AI 卡、Sheet
│       │       ├── ui/theme/            # AppColors / AppDimens / AppType
│       │       ├── ui/util/             # QuoteFormat
│       │       └── ui/chat/             # ChatReply 问答模板
│       ├── androidMain/                 # Android 渲染依赖
│       └── commonTest/                  # 仓库与问答单测
├── h5App/ / miniApp/                    # 模板端，未作为本 Demo 交付
├── iosApp/ / ohosApp/                   # 模板端，未纳入 settings 交付路径
├── buildSrc/                            # Kuikly / Kotlin 版本号
├── docs/                                # 演示视频 / GIF 占位
└── README.md
```

| 路径 | 说明 |
| :--- | :--- |
| `androidApp/` | 安装入口；默认打开 `quote_list` |
| `shared/.../data/` | `Stock` 等模型；进程内 Mock |
| `shared/.../data/AppContainer.kt` | 仓库、自选、对比勾选、风险偏好 |
| `shared/.../ui/page/` | `ComposeContainer` + `@Page`，名称见 `AppPages` |
| `shared/.../ui/component/` | 可复用盘面与 AI 组件 |
| `shared/.../ui/theme/` | 设计系统 Token，页面不用裸 HEX |
| `shared/.../ui/util/` | `QuoteFormat`（价、涨跌幅、成交额、换手率等） |
| `shared/.../ui/chat/` | 预设问答与追问模板 |

页面名（`ui/AppPages.kt`）：`quote_list`、`stock_detail`、`watchlist`、`stock_picker`、`rankings`、`compare`；详情参数 `symbol`。

### 3.3 组件

下列均为 `shared/src/commonMain/kotlin/com/example/myandroidapplication/ui/component/` 下的对外 Composable（共 **50** 个）。不包含已删除的投资旅程条，也不包含页面内私有行（如选股页 `ETFPickRow`）。

| 组件 | 用途 | 位置 |
| :--- | :--- | :--- |
| `StockCard` | 列表项：名称 / 价格 / 代码 / 涨跌幅 / 成交额·换手率 | `StockCard.kt` |
| `QuoteTopBar` | 固定顶栏，高 56dp | `QuoteTopBar.kt` |
| `QuoteTopBarAction` | 顶栏文字入口 | `QuoteTopBar.kt` |
| `NavBackButton` | 返回 PNG，不用 `Icon` | `NavBackButton.kt` |
| `PriceChangeGroup` | 详情展示型价格组 | `PriceChangeGroup.kt` |
| `QuoteMetricsBlock` | 高低、成交量、开盘、营收、市值 | `QuoteMetricsBlock.kt` |
| `ListHeader` | 列表列头（股票 / 最新价 / 涨跌幅） | `ListHeader.kt` |
| `SignalBadge` | 技术信号标签 | `SignalBadge.kt` |
| `AISignalCard` | 信号解读、推理链、5 日概率 | `AISignalCard.kt` |
| `AIAdviceChip` | 买卖建议标签 | `AIAdviceChip.kt` |
| `RiskGhostBadge` | 风险幽灵标签 | `AIAdviceChip.kt` |
| `AIAdviceCard` | 买卖建议卡片 | `AIAdviceCard.kt` |
| `AdviceSupportPoints` | 建议支撑点列表 | `AIAdviceCard.kt` |
| `AITicker` | 盯盘轮播（含事件） | `AITicker.kt` |
| `StockChart` | 走势 Canvas、标注、成交量副图 | `StockChart.kt` |
| `AITrendRow` | 短 / 中 / 长期趋势与置信度 | `AITrendRow.kt` |
| `AISummaryCard` | 盘中 / 复盘总结 | `AISummaryCard.kt` |
| `AIChatEntry` | 问答入口 | `AIChatEntry.kt` |
| `PresetQuestionChip` | 预设 / 追问芯片 | `PresetQuestionChip.kt` |
| `AIChatSheet` | 问答底部 Sheet | `AIChatSheet.kt` |
| `MarketTab` | 沪深 / 港 / 美 | `MarketTab.kt` |
| `IndexCard` | 首页指数卡 | `IndexCard.kt` |
| `SectorRow` | 首页板块横滑项 | `SectorRow.kt` |
| `SortBar` | 排序 + 筛选 / 对比入口 | `SortBar.kt` |
| `PeriodTab` | 分时到月 K | `PeriodTab.kt` |
| `ChartTypeToggle` | 折线 / K 线 | `ChartTypeToggle.kt` |
| `AdjustSelector` | 复权方式 | `AdjustSelector.kt` |
| `TagChip` | 个股特征标签 | `TagChip.kt` |
| `FilterPanel` | 自定义筛选 Sheet | `FilterPanel.kt` |
| `SearchBar` | 选股页搜索 | `SearchBar.kt` |
| `WatchStarButton` | 选股页自选星标 | `WatchStarButton.kt` |
| `PickerTab` | 选股 / 榜单分类 Tab | `PickerTab.kt` |
| `RankingItem` | 基金榜单行 | `RankingItem.kt` |
| `CompareModeBar` | 对比模式提示条 | `CompareModeBar.kt` |
| `CompareSelectMark` | 列表勾选叠加 | `CompareModeBar.kt` |
| `CompareActionBar` | 已选对比浮层 | `CompareModeBar.kt` |
| `CompareColumn` | 对比页单列指标 | `CompareColumn.kt` |
| `AICompareCard` | 多股 AI 综合分析 | `AICompareCard.kt` |
| `CompareChart` | 归一化对比走势 | `CompareChart.kt` |
| `MetricCompareBar` | 对比页指标条 | `MetricCompareBar.kt` |
| `AIBriefingCard` | 早报结构化正文 | `AIBriefingCard.kt` |
| `AIBriefingSheet` | 早报底部 Sheet | `AIBriefingSheet.kt` |
| `SentimentMeter` | 舆情温度与新闻计数 | `SentimentMeter.kt` |
| `HoldingDiagnoseEntry` | 持仓诊断入口 | `HoldingDiagnoseSheet.kt` |
| `HoldingDiagnoseSheet` | 持仓诊断 Sheet | `HoldingDiagnoseSheet.kt` |
| `RiskProfileSheet` | 首次风险偏好 | `RiskProfileSheet.kt` |
| `IndustryChainCard` | 产业链关联列表 | `IndustryChainCard.kt` |
| `QuoteBottomSheet` | 底部 Sheet 外壳 | `QuoteBottomSheet.kt` |
| `SheetDragHandle` | Sheet 拖拽条 | `QuoteBottomSheet.kt` |
| `AICardFrame` | AI 卡片青竖条外壳 | `AICardFrame.kt` |
