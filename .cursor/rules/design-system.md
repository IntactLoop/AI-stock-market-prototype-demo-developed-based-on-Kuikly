# AI 股票行情 Demo — 设计系统

> **地位**：后续所有 UI 开发的唯一视觉依据。每次 UI 任务开始前必须先读取本文件。
> **技术栈**：Kuikly Compose DSL（`com.tencent.kuikly.compose.*`）。禁止使用 `androidx.compose.ui / foundation / material3 / animation`。
> **配套文档**：`.codebuddy/rules/workflow-state.md`、`.codebuddy/rules/project-context.md`
> **版本**：v1.2 · 2026-09-13（补 5.8 AI 对话 Sheet；修正间距/语义色/返回键/禁用 API 说明）
> **适用范围**：Android 端 Demo（首页行情列表、个股详情、AI 分析模块）

---

## 0. 使用约定

1. 下文所有尺寸单位为 **dp**，字号单位为 **sp**，颜色为 **HEX**（不带 alpha 的 6 位；半透明用独立「透明度」列）。
2. Token 名称（如 `ColorBgPage`、`SpacePageH`）是后续代码里的唯一命名，禁止临时发明相近色值。
3. 涨跌色已在项目技术决策中锁定：**涨红 `#E74C3C` / 跌绿 `#27AE60`**（A 股惯例），本文件不得改写这两个 HEX。
4. 本文件只约束视觉与 Kuikly 实现边界，不包含业务逻辑。

---

## 1. 设计基调

### 1.1 风格定位

**深色金融专业风 + AI 冷色叠加**（Dark Terminal × Fintech Intelligence）。

盘面是「夜间行情终端」：深色底、高对比数字、左名右价的高密度信息。AI 能力不是聊天装饰，而是叠在盘面上的冷色批注层（信号徽章、置信度条、盯盘提醒）。

### 1.2 选择理由

| 理由 | 说明 |
| :--- | :--- |
| 盯盘场景 | 详情页顶部有 3 秒轮播的风险/放量/趋势提醒，深色底降低长时间看盘的刺眼感，并让红绿数字更跳。 |
| 数据优先 | 股票产品的第一阅读对象是价格与涨跌幅，不是插画。深色 + 等宽数字更接近专业行情软件。 |
| AI 分层 | 行情语义占用红/绿；AI 占用蓝青冷色。用户能立刻区分「市场事实」与「模型判断」。 |
| 比赛展示 | 深色盘面 + 结构化 AI 卡片（徽章/进度条/标签）比浅色列表更易在 Demo 中体现「AI 场景设计能力」。 |
| 图表可读 | Canvas 折线、网格、选中高亮在深色底上对比更强，避免浅色底上的灰线发糊。 |

不采用浅色清爽风：红绿在白底上易显「消费资讯」，专业感弱。不采用纯极简白底数据风：AI 模块需要色块层级，极简会把信号做成大段灰字，违反「结构化输出」规格。

### 1.3 设计语言方向（气质，不绑定具体框架）

- **专业行情终端**：信息密度高、价格右对齐、涨跌瞬时可扫读；参考气质类似夜间交易终端，而不是内容社区 Feed。
- **现代 Fintech 卡片**：详情页用 12dp 圆角卡片分区（信号 / 建议 / 总结），层级靠「底色差 + 1dp 描边」，不靠厚重投影。
- **AI 批注层**：冷色（蓝 `#4C9FFF` / 青 `#5CE1C5`）只出现在 AI 容器、置信度、问答入口；不用于涨跌数字。
- **克制动效**：盯盘轮播、卡片展开以内容切换为主；不做夸张缩放。Kuikly 的 `AnimatedVisibility + fadeIn/fadeOut` 目前不可依赖（见第 7 章）。

情绪关键词：**冷静、可信、数据密、智能克制**。禁止：霓虹炫光、大面积渐变按钮、拟物金属边、社交化头像瀑布流。

---

## 2. 色彩系统

### 2.1 色板总表

| Token | HEX | 透明度 | 用途 |
| :--- | :--- | :--- | :--- |
| `ColorBgPage` | `#0B0E14` | 100% | 页面底色（列表页、详情页） |
| `ColorBgElevated` | `#121722` | 100% | TopAppBar、盯盘条、底栏等贴边容器 |
| `ColorBgCard` | `#161C28` | 100% | 默认卡片 / 列表项背景 |
| `ColorBgCardAI` | `#1A2433` | 100% | AI 专用卡片（信号解读、建议、总结），比普通卡片略偏蓝 |
| `ColorBgPress` | `#1E2736` | 100% | 列表项、卡片的按压态 |
| `ColorDivider` | `#2A3344` | 100% | 分割线、图表网格 |
| `ColorBorder` | `#2A3344` | 100% | 卡片描边 1dp |
| `ColorPrimary` | `#4C9FFF` | 100% | 主色：可点击文字、选中折线、置信度填充、问答入口 |
| `ColorPrimaryDim` | `#4C9FFF` | 16% | 主色浅底（中性徽章底、选中点光晕） |
| `ColorAI` | `#5CE1C5` | 100% | AI 点缀：AI 标题前导条、问答图标点、置信度高亮数字 |
| `ColorAIDim` | `#5CE1C5` | 16% | AI 浅底（可选的 AI 标签底） |

半透明实现：`Color(0x294C9FFF)`（16% ≈ 0x29）、`Color(0x294C9FFF)` 同理用于 AI。代码里不要用 `copy(alpha)` 现场估算，按本表固定 alpha。

### 2.2 语义色（涨跌已锁定）

| Token | HEX | 透明度 | 语义 | 用途 |
| :--- | :--- | :--- | :--- | :--- |
| `ColorRise` | `#E74C3C` | 100% | **涨** | 上涨价格、正涨跌额、正涨跌幅、买入建议填充、看多信号文字 |
| `ColorRiseDim` | `#E74C3C` | 16% | 涨浅底 | 正涨跌幅胶囊底、看多 `SignalBadge` 底 |
| `ColorFall` | `#27AE60` | 100% | **跌** | 下跌价格、负涨跌额、负涨跌幅、卖出建议填充、看空信号文字 |
| `ColorFallDim` | `#27AE60` | 16% | 跌浅底 | 负涨跌幅胶囊底、看空 `SignalBadge` 底 |
| `ColorFlat` | `#8B95A8` | 100% | 平 | 涨跌幅 = 0 的数字与胶囊文字 |
| `ColorFlatDim` | `#2A3344` | 100% | 平浅底 | 平盘胶囊底（直接用分割线色，不再叠透明） |
| `ColorWarning` | `#F5A623` | 100% | 警告 | 中风险、放量提醒、接近压力/支撑、盯盘黄条 |
| `ColorWarningDim` | `#F5A623` | 16% | 警告浅底 | 中风险描边底、黄色提醒条底 |
| `ColorSuccess` | `#27AE60` | 100% | 成功 / 低风险 | 低风险文字、趋势偏强提醒（与跌色同 HEX，语义不同，必须靠文案+位置区分） |
| `ColorDanger` | `#E74C3C` | 100% | 高风险 | 高风险文字、跌破支撑提醒（与涨色同 HEX，仅用于风险标签，不用在价格上表示「危险」） |

**使用铁律**：

- 价格、涨跌额、涨跌幅 **只** 用 `ColorRise` / `ColorFall` / `ColorFlat`，不用 `ColorWarning`。
- `买入` 填充 `ColorRise`；`卖出` 填充 `ColorFall`；`观望` 填充 `ColorWarning`。
- 风险等级用 **描边幽灵标签**，不用实心：避免「高风险」实心红块被扫读成「大涨」。
- `ColorSuccess` 与 `ColorFall` 同为 `#27AE60`，`ColorDanger` 与 `ColorRise` 同为 `#E74C3C`，这是深色金融盘面的常见做法，**接受同 HEX**。但 **两者不得同时出现在同一视觉焦点区域**：价格数字占用涨跌色时，同一行不得再放低/高风险色块；风险只用描边标签，并与价格区分行。

### 2.3 文字颜色分级

| Token | HEX | 字重配合 | 用途 |
| :--- | :--- | :--- | :--- |
| `ColorTextTitle` | `#F5F7FA` | Semibold | 一级标题：页面标题、股票名称、详情页最新价（非涨跌语义时） |
| `ColorTextSecondary` | `#C5CDD8` | Medium / Regular | 二级标题：卡片标题「AI 信号解读」、区块标题 |
| `ColorTextBody` | `#A8B2C1` | Regular | 正文：信号解读句、建议理由、行情总结 |
| `ColorTextHint` | `#6B7687` | Regular | 辅助文字：股票代码、时间轴、单位、「置信度」标签 |
| `ColorTextDisabled` | `#3D4654` | Regular | 禁用文字、不可点的预设问题 |
| `ColorTextOnAccent` | `#FFFFFF` | Medium | 实心色块上的文字（买入/卖出芯片、主按钮） |
| `ColorTextOnWarning` | `#0B0E14` | Medium | 警告实心底上的文字（观望芯片，琥珀色对比需要深色字） |

详情页「最新价」若带涨跌语义：数字颜色用 `ColorRise` / `ColorFall` / `ColorFlat`，**不要**用 `ColorTextTitle`。

### 2.4 功能色速查（实现时对照）

| 场景 | 色 |
| :--- | :--- |
| 主按钮底 | `ColorPrimary` |
| 主按钮字 | `ColorTextOnAccent` |
| 页面背景 | `ColorBgPage` |
| 普通卡片 | `ColorBgCard` + 描边 `ColorBorder` 1dp |
| AI 卡片 | `ColorBgCardAI` + 左侧 3dp `ColorAI` 竖条 |
| 分割线 | `ColorDivider` 高度 1dp |
| 图表折线 | `ColorPrimary` |
| 图表面积填充 | `ColorPrimary` 18%（`0x2E4C9FFF`）→ 透明 |
| MA5 / MA10 / MA20 | `#F5A623` / `#4C9FFF` / `#B388FF` |

---

## 3. 字体系统

### 3.1 字号阶梯（只使用下列档位）

| Token | 字号 | 行高 | 典型用途 |
| :--- | :--- | :--- | :--- |
| `TypeDisplay` | 32sp | 40sp | 详情页最新价（唯一允许的 32sp） |
| `TypeTitle` | 20sp | 28sp | 页面大标题（如首页「行情」） |
| `TypeHeadline` | 18sp | 24sp | 股票名称、TopAppBar 标题、列表最新价 |
| `TypeBody` | 16sp | 24sp | 区块标题旁的强调数字、按钮文字 |
| `TypeCallout` | 14sp | 22sp | 解读正文、涨跌额、建议点位 |
| `TypeCaption` | 12sp | 16sp | 代码、徽章、涨跌幅胶囊、盯盘文案、置信度 |
| `TypeMicro` | 10sp | 14sp | 图表轴标签、图例「MA5」。页面正文禁止用 10sp |

禁止出现 11 / 13 / 15 / 17 / 19 等中间值。

### 3.2 字重规则

Kuikly `FontWeight` 只用三档，避免过多字重在 Android 上回退不一致：

| 角色 | FontWeight | 说明 |
| :--- | :--- | :--- |
| 标题 | `SemiBold`（600） | 页面标题、股票名称、卡片标题、Display 最新价 |
| 数字 | `Medium`（500） | 所有价格、涨跌额、涨跌幅、点位、成交量、置信度百分比 |
| 正文 | `Normal`（400） | 解读句、总结、辅助说明 |
| 标签 | `Medium`（500） | `SignalBadge`、`AIAdviceChip`、风险标签、预设问题芯片 |

不要用 `ExtraBold` / `Black`。不要用斜体表示涨跌。

### 3.3 行高规则

| 内容类型 | 行高公式 | 落地值 |
| :--- | :--- | :--- |
| 单行数字（价格、涨跌） | 字号 + 8sp，且单行 | Display 40sp；Headline 价格 24sp |
| 单行标题 | 字号 + 8sp | Title 28sp；Headline 名称 24sp |
| 多行正文（AI 解读、总结） | 字号 × 1.5 再取整到偶 | Callout 14sp → 22sp；Caption 多行 18sp |
| 徽章 / 芯片内文字 | 与容器高度垂直居中，不另加段落行距 | 见第 5 章组件高度 |

`Text` 多行解读最多 **3 行**，超出用省略号；完整内容放到「查看理由 / 展开」。

### 3.4 金融数字展示规范

| 规则 | 规定 |
| :--- | :--- |
| 小数位 | 价格、涨跌额、涨跌幅、点位：**固定 2 位小数**（`178.50`，不得写成 `178.5`） |
| 正号 | 涨跌额、涨跌幅 > 0 必须带 `+`（`+1.20`、`+0.67%`）；跌用 `-`；平盘 `0.00` / `0.00%` 不带正号 |
| 百分号 | 涨跌幅与数字之间 **无空格**：`+1.25%` |
| 颜色 | 数字颜色 = 涨跌语义色；同一组「价格 + 涨跌额 + 涨跌幅」必须同色 |
| 字重 | `Medium`；详情页 Display 最新价用 `SemiBold` |
| 等宽 | **必须等宽或等效等宽**。优先 `FontFamily.Monospace`；若不生效，列表价格列固定宽度 **88dp** 右对齐，涨跌幅胶囊固定最小宽度 **64dp** 居中，避免滚动时数字跳动 |
| 对齐 | 列表右侧价格右对齐；涨跌幅胶囊在价格正下方右对齐 |
| 成交量 | `≤ 9999` 原始整数；`1.00万`～`9999.99万`；`≥ 1亿` 用 `x.xx亿`。单位用 `TypeCaption` + `ColorTextHint` |
| 置信度 | 整数 0–100 + `%`，无小数：`72%` |
| 禁止 | 不要对数字做淡入动画改变字号；不要把涨跌幅写成「↑1.25」——只用 `+`/`-` 与颜色 |

---

## 4. 间距系统

### 4.1 基准

**基准单位 `Space = 4dp`。** 所有间距必须是 4 的倍数。

| Token | 值 | 用途 |
| :--- | :--- | :--- |
| `Space1` | 4dp | 名称与代码间距、徽章内边距纵向、图标与文字 |
| `Space2` | 8dp | 同组元素间距（价格与涨跌幅、徽章与徽章） |
| `Space3` | 12dp | 卡片内边距（紧凑）、卡片与卡片 |
| `Space4` | 16dp | **页面左右边距**、AI 卡片内边距、区块内段落 |
| `Space5` | 20dp | 详情页价格区与图表区间距 |
| `Space6` | 24dp | 大区块分隔（图表区与 AI 区） |
| `Space8` | 32dp | 详情页 **无** AITicker 时，TopAppBar 下方到价格区的顶距。**有** AITicker 时不用此值（顶距 0dp，盯盘条贴 AppBar） |

禁止 6dp / 10dp / 14dp / 18dp。

### 4.2 页面、卡片、列表标准值

| 项目 | Token / 值 |
| :--- | :--- |
| 页面左右边距 | `Space4` = **16dp** |
| 页面顶部（TopAppBar 下方到内容） | 列表 **8dp**；详情页有 AITicker 时 **0dp**；详情页无 AITicker 时 **32dp**（`Space8`） |
| 页面底部留白 | **24dp**；有底栏时底栏上方再加 **16dp** |
| 普通卡片内边距 | **12dp**（StockCard 用水平 16 + 垂直 12，见组件） |
| AI 卡片内边距 | **16dp** |
| 卡片之间间距 | **12dp** |
| 同卡片内部区块间距 | **12dp** |
| 行内元素间距 | **8dp** |
| 名称列与价格列 | `SpaceBetween`，中间最小 **12dp** |

### 4.3 圆角、高度、分割线

| Token | 值 | 用途 |
| :--- | :--- | :--- |
| `RadiusBadge` | 4dp | SignalBadge、涨跌幅胶囊 |
| `RadiusChip` | 14dp | AIAdviceChip、风险幽灵标签（胶囊，高度 28 的一半） |
| `RadiusCard` | 12dp | 所有卡片、图表容器、问答入口 |
| `RadiusSheet` | 16dp | 底部展开层（查看理由 / 问答） |
| `HeightListItem` | 72dp | StockCard 固定高度 |
| `HeightTicker` | 36dp | AITicker |
| `HeightAppBar` | 56dp | TopAppBar |
| `HeightButton` | 44dp | 主按钮、预设问题行高（可点区域） |
| `HeightChip` | 28dp | AIAdviceChip |
| `HeightBadge` | 24dp | SignalBadge、风险标签 |
| `HeightChatEntry` | 56dp | 详情页底部问答入口条 |
| `HeightChart` | 220dp | StockChart 绘图区（不含图例） |
| `HeightProgress` | 4dp | 置信度条 |
| `StrokeDivider` | 1dp | 分割线、卡片描边 |
| `StrokeAccent` | 3dp | AI 卡片左竖条、盯盘严重度竖条 |
| `MinTouch` | 44dp | 所有可点击热区最小边 |

阴影：**默认不用阴影**。层级用 `ColorBgPage` / `ColorBgCard` / `ColorBgCardAI` + 1dp `ColorBorder`。若必须浮起（底部 Sheet），`Modifier.shadow(elevation = 8.dp, spotShadowColor = Color(0x66000000))`，**禁止**设 `ambientShadowColor`（Kuikly 无效）。

---

## 5. 组件视觉规范

通用状态（所有可点组件）：

| 状态 | 规格 |
| :--- | :--- |
| Default | 下文各组件默认值 |
| Pressed | 背景切到 `ColorBgPress`，持续整段按压；数字颜色不变 |
| Disabled | 文字 `ColorTextDisabled`，背景仍用卡片色，不降低整个卡片 alpha（避免叠在深色底上看不清） |
| Selected | 仅图表选中点、问答预设问题：描边改为 `ColorPrimary` 1dp，背景 `ColorPrimaryDim` |

---

### 5.1 StockCard（首页股票列表项）

**角色**：一行一只股票，点击进入详情。

| 属性 | 规格 |
| :--- | :--- |
| 尺寸 | 宽 = 屏宽 − 32dp（左右各 16）；**高 72dp** 固定 |
| 外轮廓 | 圆角 **0**（列表连续项）；背景 `ColorBgCard` |
| 分割线 | 底边 1dp `ColorDivider`，左边距 16dp、右边距 16dp（不是通栏） |
| 内边距 | 左 16 / 右 16 / 上 12 / 下 12 |
| 布局 | `Row` + `SpaceBetween`；左列名称组，右列数字组 |

**左列（最大宽度 = 父宽 − 88dp − 12dp）**

| 元素 | 规格 |
| :--- | :--- |
| 股票名称 | 18sp / SemiBold / `ColorTextTitle` / 单行省略 |
| 名称↔代码 | 4dp |
| 股票代码 | 12sp / Regular / `ColorTextHint` / 单行 |

**右列（宽 88dp）**

| 元素 | 规格 |
| :--- | :--- |
| 最新价 | 18sp / Medium / 等宽 / 右对齐 / 涨跌色 |
| 价格↔胶囊 | 4dp |
| 涨跌幅胶囊 | 高 20dp、最小宽 64dp、圆角 4dp、文字 12sp Medium 居中；正：字 `ColorRise` 底 `ColorRiseDim`；负：字 `ColorFall` 底 `ColorFallDim`；平：字 `ColorFlat` 底 `ColorFlatDim` |
| 涨跌额 | **列表项默认不展示**（避免拥挤）；详情页再展示 |

**状态**：Pressed 整行背景 `ColorBgPress`。无选中描边（列表不是多选）。

**禁止**：左侧头像、行业彩色条、右箭头图标（`Icon` 不可用；也不用 `>` 字符，避免视觉噪点）。

---

### 5.2 SignalBadge（技术信号标签）

**角色**：`MACD金叉`、`放量`、`RSI超买` 等，出现在 AISignalCard 第一行。

| 属性 | 规格 |
| :--- | :--- |
| 高度 | **24dp** |
| 水平内边距 | 8dp |
| 垂直内边距 | 4dp（文字垂直居中） |
| 圆角 | 4dp |
| 文字 | 12sp / Medium / 单行 / 无省略（标签文案 ≤ 6 个汉字） |
| 间距 | 同行徽章之间 **8dp**；自动换行，行距 **8dp** |
| 边框 | 无 |

**颜色映射**

| 信号方向 | 文字 | 背景 | 示例 |
| :--- | :--- | :--- | :--- |
| 看多 | `ColorRise` | `ColorRiseDim` | MACD金叉、均线多头、放量上涨 |
| 看空 | `ColorFall` | `ColorFallDim` | MACD死叉、RSI超卖、放量下跌 |
| 风险/临界 | `ColorWarning` | `ColorWarningDim` | RSI超买、接近压力位、接近支撑位 |
| 中性 | `ColorPrimary` | `ColorPrimaryDim` | 震荡、量能平稳 |

**状态**：Default 如上。点击展开解释时，增加 1dp 同色描边（看多则 `ColorRise`），背景不变。无 Disabled。

---

### 5.3 AISignalCard（AI 信号解读卡片）

**角色**：详情页走势图下方第一张 AI 卡片。结构锁定为：标题 → 信号行 → 解读 → 置信度。

| 属性 | 规格 |
| :--- | :--- |
| 宽度 | 屏宽 − 32dp |
| 高度 | 自适应，最小 **148dp**，最大内容约 3 行解读 + 徽章 2 行 |
| 背景 | `ColorBgCardAI` |
| 描边 | 1dp `ColorBorder` |
| 圆角 | 12dp |
| 左竖条 | 宽 3dp、高 = 卡片高度、色 `ColorAI`、贴左并随 12dp 圆角裁剪 |
| 内边距 | 16dp（左内边距 = 16 + 3，即文字从竖条右侧 16dp 起算：实现上 `padding(start=19.dp)` 或竖条 + `padding(16.dp)`） |

**内部纵向节奏（从上到下）**

| 行 | 规格 | 间距（与下一行） |
| :--- | :--- | :--- |
| 标题 | 「AI 信号解读」16sp SemiBold `ColorTextSecondary` | 12dp |
| 信号行 | `SignalBadge` 流式 Wrap，最多展示 **4** 个，超出「+N」用中性徽章 | 12dp |
| 解读文本 | 14sp Regular `ColorTextBody`，行高 22sp，最多 3 行 | 12dp |
| 置信度行 | 左：「72% 置信度」12sp Medium，数字用 `ColorAI`，「置信度」三字用 `ColorTextHint`；右无文字 | 8dp |
| 进度条 | 高 **4dp**、圆角 2dp、宽铺满内容区；轨道 `ColorDivider`；填充 `ColorPrimary`；进度 = `aiConfidence / 100` | — |

**状态**：卡片本身不可点；仅徽章可点。无 Pressed 整卡变色。

**禁止**：大段无结构文字；环形百分比（Kuikly 无现成 CircularProgress，且本规范明确用条）。

---

### 5.4 AIAdviceChip（买卖建议标签）与风险/点位组

**角色**：建议标签是实心胶囊；风险是幽灵胶囊；点位是纯文字。三者同一行或折行，位于 AISignalCard 下方的建议卡片内。

#### 建议标签（买入 / 观望 / 卖出）

| 属性 | 规格 |
| :--- | :--- |
| 高度 | **28dp** |
| 最小宽度 | 56dp |
| 水平内边距 | 12dp |
| 圆角 | 14dp（完全胶囊） |
| 文字 | 14sp Medium |

| 建议 | 背景 | 文字 |
| :--- | :--- | :--- |
| 买入 | `ColorRise` | `ColorTextOnAccent` |
| 观望 | `ColorWarning` | `ColorTextOnWarning` |
| 卖出 | `ColorFall` | `ColorTextOnAccent` |

同时只出现 **一个** 建议芯片。

#### 风险标签（低 / 中 / 高）

| 属性 | 规格 |
| :--- | :--- |
| 高度 | 24dp |
| 水平内边距 | 8dp |
| 圆角 | 4dp |
| 背景 | 透明 |
| 描边 | 1dp |

| 风险 | 描边与文字 |
| :--- | :--- |
| 低风险 | `ColorSuccess`（`#27AE60`） |
| 中风险 | `ColorWarning` |
| 高风险 | `ColorDanger`（`#E74C3C`） |

文字 12sp Medium。建议芯片与风险标签间距 **8dp**。

#### 点位文字

| 元素 | 规格 |
| :--- | :--- |
| 文案格式 | `支撑买入价 178.50`、`压力卖出价 185.20` |
| 字号 | 14sp；标签词 Regular `ColorTextHint`；数字 Medium 等宽 `ColorTextTitle` |
| 两行点位间距 | 4dp |
| 与芯片行间距 | 12dp |

「查看理由」：14sp Medium `ColorPrimary`，下划线 **无**；展开区与点位间距 8dp，正文 14sp / 22sp / `ColorTextBody` / 最多 4 行。

建议卡片容器：与 AISignalCard 同宽、圆角 12dp、背景 `ColorBgCardAI`、内边距 16dp、无左竖条（避免两条竖条重复；标题「买卖建议」16sp SemiBold `ColorTextSecondary`）。

---

### 5.5 AITicker（盯盘提醒条）

**角色**：详情页 TopAppBar 正下方，全宽，每 **3000ms** 切换一条，至少 3 条。

| 属性 | 规格 |
| :--- | :--- |
| 尺寸 | 宽 = 屏宽；**高 36dp** 固定 |
| 背景 | `ColorBgElevated` |
| 底部分割线 | 1dp `ColorDivider` 通栏 |
| 左竖条 | 宽 3dp、高 36dp，颜色随当前条类型 |
| 内边距 | 左 12dp（竖条外再 12）、右 16dp |
| 布局 | `Row` 垂直居中：圆点 6dp + 间距 8dp + 单行文本 |

**类型颜色**

| 类型 | 竖条 / 圆点 | 示例语义 |
| :--- | :--- | :--- |
| 风险 | `ColorRise` | 接近压力位、跌破支撑 |
| 资金 | `ColorWarning` | 放量 |
| 趋势 | `ColorSuccess` | 均线多头 |

圆点：6×6dp、`CircleShape`（`RoundedCornerShape(3.dp)`）。**不要用 Emoji**（🔴🟡🟢 在 Android 上大小不一，破坏 36dp 高度）。

文本：12sp Regular `ColorTextSecondary`，单行省略。数字片段（如 `185.20`、`35%`）保持原句，不单独变色（36dp 内来不及扫读双色）。

**切换**：3000ms 一条。视觉：**直接替换文案 + 竖条颜色**（0ms）。不要依赖 `AnimatedVisibility + fade`。若实现滑动切换，位移距离 **24dp**、时长 **180ms**，同时只显示一条。

**点击**：整条可点，热区 36dp 高 × 全宽（低于 44dp，属规格例外：为贴顶信息条让路）。Pressed 背景 `ColorBgPress`。

---

### 5.6 StockChart（走势图容器）

**角色**：详情页价格区下方；Canvas 折线；点击时间点高亮并驱动 AI 文案。

| 属性 | 规格 |
| :--- | :--- |
| 外容器宽 | 屏宽 − 32dp |
| 外容器圆角 | 12dp |
| 外容器背景 | `ColorBgCard` |
| 外容器描边 | 1dp `ColorBorder` |
| 外容器内边距 | 12dp |
| 图例行高 | 20dp（MA5/MA10/MA20），与绘图区间距 8dp |
| **绘图区高度** | **220dp** 固定 |
| 绘图区内边距 | 左 44dp（给价格轴）、右 8dp、上 8dp、下 24dp（给时间轴） |

**图例**：10sp Regular；色块 8×8dp 圆角 2dp + 间距 4dp + 文案；图例项之间 12dp。MA5 `#F5A623`，MA10 `#4C9FFF`，MA20 `#B388FF`。

**网格与轴**

| 元素 | 规格 |
| :--- | :--- |
| 水平网格 | 4 条，1dp `ColorDivider` |
| 垂直网格 | 不画满，仅选中时画竖线 |
| Y 轴文字 | 10sp `ColorTextHint`，右对齐贴在绘图区左侧 44dp 槽内 |
| X 轴文字 | 10sp `ColorTextHint`，展示 4 个时间点 |

**折线与面积**

| 元素 | 规格 |
| :--- | :--- |
| 主折线 | 1.5dp（Canvas 里用 1.5f 或 2px，取整到 2px）、色 `ColorPrimary`、无圆点（未选中） |
| 面积 | 自折线垂直向下到图表底，填充 `#2E4C9FFF`（约 18%）至透明；若线性渐变不可用，则用 18% 实心降级 |
| MA 线 | 1dp，无面积 |

**选中态（图表联动）**

| 元素 | 规格 |
| :--- | :--- |
| 竖线 | 1dp `ColorPrimary`，贯穿绘图区 |
| 点 | 外圆 10dp `ColorPrimary`；内圆 6dp `ColorBgCard`（形成 2dp 环） |
| 浮标 | 选中点上方，高 22dp、水平内边距 8dp、圆角 4dp、背景 `ColorBgElevated`、描边 1dp `ColorPrimary`；文字 10sp Medium `ColorTextTitle`，格式 `183.60` |
| 未选中 | 无浮标、无竖线 |

**空/加载**：绘图区居中 12sp `ColorTextHint`「暂无走势」；不要转圈（无可靠跨端 CircularProgress 规格）。

---

### 5.7 涨跌数字展示组件（PriceChangeGroup）

**角色**：可复用的「最新价 + 涨跌额 + 涨跌幅」组合。列表用紧凑型，详情用展示型。

#### 紧凑型（StockCard 右列）

| 元素 | 字号 / 字重 / 色 / 其它 |
| :--- | :--- |
| 最新价 | 18sp Medium 等宽 涨跌色 右对齐 |
| 涨跌幅 | 12sp Medium 胶囊内 见 5.1 |
| 涨跌额 | 不展示 |

#### 展示型（详情页顶部价格区）

布局：左主价，右涨跌两行。价格区高度 **72dp**，左右边距 16dp，背景 `ColorBgPage`（无卡片）。

| 元素 | 规格 |
| :--- | :--- |
| 最新价 | 32sp SemiBold 等宽 涨跌色；行高 40sp |
| 涨跌额 | 14sp Medium 等宽 涨跌色；格式 `+1.20` |
| 涨跌幅 | 14sp Medium 等宽 涨跌色；格式 `+0.67%`；**详情页不用胶囊**，与涨跌额同一行，中间间距 8dp |
| 名称 | 18sp SemiBold `ColorTextTitle` |
| 代码 | 12sp Regular `ColorTextHint`，名称右侧 8dp |

平盘：全部 `ColorFlat`。

**高低成交量行**（展示型下方 12dp）：四列均分或两行 Key-Value。

| 标签 | 值 |
| :--- | :--- |
| 标签 | 12sp Regular `ColorTextHint`（最高 / 最低 / 成交量 / 开盘） |
| 值 | 14sp Medium 等宽 `ColorTextTitle`（价格类仍 2 位小数；成交量按 3.4 节单位） |
| 列间距 | 12dp |
| 行高 | 每行 20dp，两行间距 8dp |

最高/最低 **不** 用涨跌色（避免和「当前涨跌」冲突）。

---

### 5.8 AI 对话 Sheet（AIChatSheet）

**角色**：详情页底部 `AIChatEntry` 点击后弹出。承载预设问题、用户/AI 气泡、输入区。不是通用聊天机器人，回答必须与当前股票数据一致。

实现容器用 Kuikly `ModalBottomSheet`（`visible: Boolean`，**不要**用原生 `sheetState` / `dragHandle` 参数）。

#### 5.8.1 Sheet 容器

| 属性 | 规格 |
| :--- | :--- |
| 高度 | 页面高度 × **0.72**（实现：`pagerData.pageViewHeight * 0.72f`），且不小于 **480dp**、不大于 **640dp** |
| 宽度 | 屏宽 100% |
| 背景 | `ColorBgElevated` |
| 遮罩 | `#0B0E14` 60%（`Color(0x990B0E14)`） |
| 顶圆角 | **16dp**（`RadiusSheet`）；底边贴屏幕，圆角 **0** |
| 描边 | 无（靠遮罩分层） |
| 阴影 | `shadow(8.dp, spotShadowColor = Color(0x66000000))`，禁止 `ambientShadowColor` |
| 下拉关闭 | `dismissOnDrag = true`，`dismissThreshold = 0.25f`，`animationDurationMillis = 250` |
| 水平内边距 | 16dp |
| 内部结构（上→下） | 把手区 → 标题行 → 分割线 → 消息列表 → 预设问题行 → 输入区 |

**顶部把手**

| 属性 | 规格 |
| :--- | :--- |
| 外区高度 | 20dp（含上下留白） |
| 条尺寸 | 宽 **32dp** × 高 **4dp** |
| 圆角 | 2dp |
| 颜色 | `ColorDivider` |
| 对齐 | 水平居中；距 Sheet 顶 **8dp** |
| 实现 | `Box` 矩形，**不要**用 `ModalBottomSheet` 的 `dragHandle` 参数（Kuikly 无此参数） |

**标题行**

| 元素 | 规格 |
| :--- | :--- |
| 高度 | 44dp |
| 标题 | 「问 AI」16sp SemiBold `ColorTextSecondary`，左对齐 |
| 关闭 | 右侧「关闭」14sp Medium `ColorPrimary`，热区 44×44dp |
| 底部分割线 | 1dp `ColorDivider` 通栏（相对 Sheet，不受 16dp 内边距裁切） |

#### 5.8.2 消息气泡

消息列表用 `LazyColumn`（禁止 `verticalScroll`），项间距 **8dp**，列表上下各 12dp。用户气泡右对齐，AI 气泡左对齐。同一条消息内「结论 / 理由 / 风险」分三段，不用大段纯文字。

| 属性 | 用户气泡 | AI 气泡 |
| :--- | :--- | :--- |
| 背景 | `ColorPrimary` | `ColorBgCardAI` |
| 描边 | 无 | 1dp `ColorBorder` |
| 文字 | 14sp Regular / 行高 22sp / `ColorTextOnAccent` | 14sp Regular / 行高 22sp / `ColorTextBody` |
| 内边距 | 水平 12dp、垂直 10dp | 水平 12dp、垂直 10dp |
| 最大宽度 | 内容区宽度 × **0.76** | 内容区宽度 × **0.76** |
| 圆角 | 12 / 12 / **4** / 12（上左 / 上右 / 下右 / 下左） | 12 / 12 / 12 / **4** |
| 数字 | 价格、点位仍 2 位小数、等宽 Medium；涨跌色仅出现在 AI 气泡的数字上，气泡底仍用 `ColorBgCardAI`（遵守 2.2「不同焦点」） |

AI 结构化回答内部节奏：结论 14sp Medium `ColorTextTitle` → 间距 8dp → 理由 14sp Regular `ColorTextBody` → 间距 8dp → 风险行复用 5.4 风险幽灵标签（不要在气泡里再放实心 `AIAdviceChip`）。

#### 5.8.3 预设问题快捷按钮（独立样式，禁止复用 AIAdviceChip）

`AIAdviceChip` 是买入/观望/卖出的实心语义胶囊，红绿琥珀会污染问答区。预设问题使用独立组件 **`PresetQuestionChip`**。

| 属性 | 规格 |
| :--- | :--- |
| 高度 | **32dp**（视觉）；外热区不够时外包 44dp 高点击层 |
| 水平内边距 | 12dp |
| 圆角 | **8dp**（有意区别于 AdviceChip 的 14dp 胶囊） |
| 默认 | 背景 `ColorBgCard`，描边 1dp `ColorBorder`，文字 12sp Medium `ColorTextSecondary` |
| Pressed | 背景 `ColorBgPress` |
| Selected / 发送中 | 背景 `ColorPrimaryDim`，描边 1dp `ColorPrimary`，文字 `ColorPrimary` |
| Disabled | 文字 `ColorTextDisabled`，描边 `ColorDivider` |
| 布局 | `LazyRow`，芯片间距 8dp，行高 44dp，位于输入区上方、与输入区间距 8dp |
| 文案 | 单行省略；示例不超过 12 个汉字：「适合买入吗？」「最近有什么风险？」「成交量怎么样？」「支撑压力在哪？」「今日如何总结？」 |

至少 **5** 个预设问题。点击后等同发送该文案，并进入加载态。

#### 5.8.4 输入区（禁止 OutlinedTextField）

Kuikly **没有** `OutlinedTextField`。用 `TextField` + 背景块模拟圆角输入框；`onValueChange` **必须显式传入**，否则输入不更新。

| 属性 | 规格 |
| :--- | :--- |
| 外区高度 | **56dp** |
| 背景 | `ColorBgElevated` |
| 顶部分割线 | 1dp `ColorDivider` |
| 水平内边距 | 16dp |
| 输入框高度 | **44dp** |
| 输入框背景 | `ColorBgPage` |
| 输入框圆角 | 8dp |
| 输入框描边 | 无（靠底色差，不画 outline） |
| 指示线 | 下划线颜色设为透明，去掉 Material 默认 underline |
| 文字 | 14sp Regular `ColorTextTitle` |
| 占位 | `Modifier.placeHolder("输入关于这只股票的问题", ColorTextHint)`，**不要**用 `placeholder` 参数以外的 androidx API |
| 长度 | `Modifier.maxLength(length = 120, type = LengthLimitType.CHARACTER)`；禁止在 `onValueChange` 里截断 |
| 行数 | `singleLine = true` |
| 发送 | 右侧「发送」14sp Medium；有文本时 `ColorPrimary`，空文本时 `ColorTextDisabled` 且不可点；热区 44×44dp。**禁止 `Icon`** |
| 间距 | 输入框与发送 8dp |

键盘：默认文本键盘。不要用 `Modifier.imePadding()`（Kuikly 不可用，键盘由原生层处理）。`show()` / `hide()` 若使用，避免同帧连续调用。

#### 5.8.5 空状态与加载状态

**空状态**（尚无任何消息）：

| 元素 | 规格 |
| :--- | :--- |
| 位置 | 消息区垂直居中 |
| 主文案 | 「选择下方问题，或输入后提问」14sp Regular `ColorTextHint`，居中 |
| 辅文案 | 「回答基于当前行情数据」12sp Regular `ColorTextDisabled`，主文案下方 8dp |
| 装饰 | 无插画、无 Emoji、无空态大图 |
| 预设行 | **仍然展示**（空态的主要操作入口） |
| 输入区 | 正常展示，可输入 |

**加载状态**（已发送，等待模板回答）：

| 元素 | 规格 |
| :--- | :--- |
| 用户气泡 | 立即出现（已发送文案） |
| AI 占位气泡 | 左对齐，尺寸规格同 AI 气泡；高 **48dp**；文案「正在分析这只股票…」12sp Regular `ColorTextHint` |
| 进度 | 占位气泡内底部 4dp 条，使用 **不确定进度** `LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(4.dp), color = ColorPrimary, trackColor = ColorDivider)`（不传 `progress`） |
| 时长 | Demo 模板填充建议 600–800ms 后替换为正式 AI 气泡；不要无限转圈 |
| 输入 / 预设 | 加载期间 Disabled（见上表 Disabled） |
| 禁止 | `CircularProgressIndicator`、三点跳跃动画依赖 `AnimatedVisibility` |

加载结束：删除占位气泡，插入正式 AI 气泡（结论 + 理由 + 风险）。失败时占位文案改为「暂时无法分析，请换一个预设问题」12sp `ColorWarning`，预设行重新可点。

---

## 6. 布局规范

### 6.1 页面通用结构

所有页面使用 `Column(Modifier.fillMaxSize().background(ColorBgPage))`，**不要**用带 FAB 的 Scaffold 默认槽位玩法；TopAppBar 自行固定在顶部。

```
┌─────────────────────────────────────┐  ← ColorBgPage
│ TopAppBar  56dp  ColorBgElevated    │  标题 18sp SemiBold ColorTextTitle
│            底部分割线 1dp            │  返回热区 44×44dp（详情页）
├─────────────────────────────────────┤
│ AITicker 36dp （仅详情页）            │
├─────────────────────────────────────┤
│ 内容区 LazyColumn                    │  水平 padding 16dp
│  · 顶部 8dp（列表）/ 0dp（详情有 Ticker）/ 32dp（详情无 Ticker）
│  · 卡片间距 12dp                     │
│  · 底部 24dp + 底栏避让              │
├─────────────────────────────────────┤
│ 底栏（仅详情页，问答入口）56dp        │  ColorBgElevated + 顶部分割线 1dp
└─────────────────────────────────────┘
```

**TopAppBar**

| 项 | 值 |
| :--- | :--- |
| 高度 | 56dp |
| 背景 | `ColorBgElevated` |
| 标题 | 首页「行情」居左 16dp；详情页股票名称居中，代码不进标题（已在价格区） |
| 返回 | 详情页左侧 44×44dp。使用本地 PNG：`shared/src/commonMain/assets/common/ic_back.png`（24×24 逻辑像素，颜色 `#F5F7FA`），`Image` + `painterResource(DrawableResource(ImageUri.commonAssets("ic_back.png").toUrl("")))`。热区 44×44，图标居中。**禁止 `Icon`，禁止 `Text("‹")`**（字符在 Android 上宽度与垂直居中不稳定）。不使用 SVG（Kuikly Image 对 SVG 支持不稳定） |
| 滚动折叠 | **不做**。`TopAppBar` 不支持 `scrollBehavior` |

**底部区**

- 列表页：无底栏、无 Tab。
- 详情页：固定 `AIChatEntry` 条，高 56dp；文案「问 AI 关于这只股票」14sp `ColorTextHint`；右侧「提问」主色文字 14sp Medium `ColorPrimary`。圆角 0（贴底）。点击展开 **5.8 AIChatSheet**（顶圆角 16dp）。

### 6.2 卡片间距

| 关系 | 间距 |
| :--- | :--- |
| LazyColumn 列表项（StockCard） | **0**（靠内部底部分割线） |
| 详情页：价格区 → 图表卡片 | 16dp |
| 图表卡片 → AISignalCard | 12dp |
| AISignalCard → 建议卡片 | 12dp |
| 建议卡片 → 趋势/摘要 | 12dp |
| 摘要 → 行情总结 | 12dp |
| 任何卡片 → 页面底 | 24dp（再加底栏 56dp 避让） |

列表外层 **不要**再套圆角大卡片包住全部 StockCard。

### 6.3 列表页与详情页一致性

| 规则 | 规定 |
| :--- | :--- |
| 同一只股票的名称 | 两页都是 18sp SemiBold `ColorTextTitle` |
| 同一只股票的代码 | 两页都是 12sp `ColorTextHint` |
| 同一只股票的最新价颜色 | 两页同一套涨跌色与 2 位小数 |
| 涨跌幅格式 | 列表 `+0.67%` 在胶囊内；详情无胶囊但字符串 **完全相同** |
| 水平边距 | 两页内容都是 16dp |
| 卡片圆角 | 详情页卡片统一 12dp；列表项 0 |
| 背景 | 两页都是 `ColorBgPage`，禁止详情页改浅色 |
| 数字字体 | 同一套等宽规则 |

详情页模块顺序（锁定，对应 AI 规格）：

1. TopAppBar  
2. AITicker  
3. 名称/代码 + PriceChangeGroup 展示型 + 高低成交量  
4. StockChart  
5. AISignalCard  
6. 买卖建议卡片（AdviceChip + 风险 + 点位）  
7. 趋势判断标签行（复用 SignalBadge 规范，文案「短期偏多 / 中期震荡」等）  
8. 行情总结卡片  
9. 底部问答入口（固定，不随列表滚走）

内容 3–8 放在 **一个** `LazyColumn` 里（每个模块一个 `item`），禁止 `verticalScroll`。

---

## 7. Kuikly Compose DSL 能力边界

包名铁律：只有 `androidx.compose.runtime.*`（`@Composable` / `remember` / `mutableStateOf`）可用官方包；其余必须 `com.tencent.kuikly.compose.*`。禁止 `androidx.compose.ui / foundation / material3 / animation`。

### 7.0 高风险 API 编译验证（2026-09-13）

在 `shared` 模块对 Kuikly 2.7.0-2.1.21 执行 `:shared:compileDebugKotlinAndroid`。最小探针页已验证后删除。

| API | 结论 | 必须使用的包 / 签名 |
| :--- | :--- | :--- |
| `remember` / `mutableStateOf` | **可用** | `androidx.compose.runtime.*`。Compose 页面的状态用这套，**不要**改成 Kuikly `observable` |
| `LinearProgressIndicator` | **可用** | `com.tencent.kuikly.compose.material3.LinearProgressIndicator`。确定进度签名为 `progress: () -> Float` |
| `Brush.verticalGradient` 于 Canvas | **可用** | `com.tencent.kuikly.compose.ui.graphics.Brush`，在 `com.tencent.kuikly.compose.foundation.Canvas` 的 `drawRect` / `drawRoundRect` 中作为 `brush` 参数 |

**编译前置（缺一不可，否则 `remember` 会在 IR 阶段报 `couldn't find inline method remember`）：**

- `shared` 依赖 `com.tencent.kuikly-open:compose:${Version.getKuiklyVersion()}`
- 根工程与 `shared` 均应用 `kotlin("plugin.compose")`（版本与 Kotlin **2.1.21** 对齐）

未加 Compose 编译器插件时，前端能解析 `androidx.compose.runtime` 类型，但 JVM backend 无法内联 `remember`。这不是 API 不存在，不要因此改回 `observable`。

**状态管理分层：** Compose DSL 页面（`ComposeContainer` + `@Composable`）用 `remember { mutableStateOf() }`。传统 Kuikly DSL 页面（`Pager` + `attr/event`）继续用 `observable` / `observableList`。本设计系统约束的 UI 全部走前者。

### 7.1 可直接实现（按规范原样做）

| 规范项 | 实现方式 |
| :--- | :--- |
| 颜色、背景、圆角、边距、尺寸 | `Color`、`Modifier.background` / `padding` / `size` / `fillMaxWidth` / `height`、`RoundedCornerShape` |
| 页面结构 | `Column` / `Row` / `Box` + 固定高度 AppBar / Ticker / 底栏 |
| 列表 | `LazyColumn` + `items`；`beyondBoundsItemCount = 3`；**不要**传 `reverseLayout` / `flingBehavior` |
| 卡片容器 | `Box`/`Column` + background + border 1dp，或 Material3 `Card`（颜色按本表覆盖） |
| 文本层级 | `Text(fontSize.sp, fontWeight, color, maxLines, overflow = Ellipsis)` |
| 涨跌胶囊 / Badge / Chip | `Box` + `background` + `padding` + `Text` |
| 买卖实心胶囊 | 同上 |
| 风险幽灵标签 | `Modifier.border(1.dp, color, RoundedCornerShape(4.dp))` |
| 置信度条 | `LinearProgressIndicator(progress = { confidence / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = ColorPrimary, trackColor = ColorDivider)`。`progress` 必须是 lambda，不要传裸 `Float` |
| 点击 | `Modifier.clickable`；Pressed 用 `interactionSource` 或自管 `isPressed` 切 `ColorBgPress` |
| 流式徽章 | `Row` + `horizontalArrangement` 不够时用自定义 Wrap：`Row` 手动分行，或多个 `Row` |
| 页面跳转后的详情滚动 | `LazyColumn` + `nestedScroll(SELF_FIRST, SELF_FIRST)` 仅在需要禁止与父滚动冲突时 |
| 等宽数字降级 | 固定宽度 `Box(Modifier.width(88.dp))` + `TextAlign.End` |
| 阴影（仅 Sheet） | `shadow(8.dp, spotShadowColor = Color(0x66000000))` |
| AI 对话 Sheet | `ModalBottomSheet(visible, onDismissRequest, containerColor, scrimColor, dismissOnDrag, …)`，无 `sheetState` / `dragHandle` |
| 问答输入框 | `TextField(value, onValueChange)` + `Modifier.placeHolder` + `maxLength`；**禁止** `OutlinedTextField` |

### 7.2 必须自定义绘制

| 规范项 | 原因与做法 |
| :--- | :--- |
| StockChart 折线、网格、面积、MA、选中点、浮标 | 无现成图表组件。使用 Compose `Canvas`（或官方 Demo `WeatherCanvasPage` 同模式）+ 触摸回调。浮标可用 Canvas 画，或 `Box` 叠在 Canvas 上按坐标 `offset` |
| 图表选中竖线 / 环状锚点 | 同上，在 `onPointSelected(index)` 后重绘 |
| AI 卡片左竖条随圆角裁剪 | 可用 `Row`：左 `Box(width=3.dp, modifier=background)` + 内容；外层 `clip(RoundedCornerShape(12.dp))`。不必自绘 |
| 面积渐变 | `drawRect(brush = Brush.verticalGradient(colors = listOf(Color(0x2E4C9FFF), Color.Transparent), startY = 0f, endY = size.height), …)`。已验证可用，不必降级为实心半透明 |

### 7.3 必须降级（不要按原生 Compose 理想效果实现）

| 原规范/常见写法 | 降级方案 |
| :--- | :--- |
| `AnimatedVisibility` + `fadeIn/fadeOut` 轮播 AITicker | **直接切换文本**（0ms）。fade 因 `layerBlock` 缺失不可依赖 |
| `AnimatedVisibility` + `scaleIn` | 不用 |
| 盯盘 Emoji 圆点 | 用 6dp `Box` 圆形，不用 🔴🟡🟢 |
| `Icon` / `IconImage` | 不支持。返回键用 `ic_back.png`（见 6.1）；AI 点缀用 `Image` 或几何 `Box` |
| `Modifier.verticalScroll` | 改 `LazyColumn` 单/多 `item` |
| TopAppBar 滚动折叠 | 固定 56dp |
| `Modifier.shadow(ambientShadowColor=…)` | 删 ambient；卡片改描边 |
| `CircularProgressIndicator` 作置信度 | 改 4dp 线性条 |
| 密码/视觉变换类 API | 本 App 无密码框；勿引入 `PasswordVisualTransformation` |
| 图表面积高级渐变 / 虚线网格 | 面积用 `Brush.verticalGradient`（已验证）；网格仍用 1dp 实线，不用虚线 |
| `FontFamily.Monospace` 不生效 | 固定列宽右对齐 |
| 列表项 `Modifier.animateItem()` | **禁止**，会编译失败 |
| 下拉刷新 | 本阶段不做；若做必须用 `pullToRefreshItem()`，禁用 `PullToRefreshBox` |
| 长文本选择 | 默认不选中；总结卡不包 `SelectionContainer`（避免误触） |

### 7.4 明确不要使用的 API

**下面不是「不推荐 / 能用但最好别用」，而是「写入代码会编译失败或在 Kuikly 中不存在」。不要用 androidx 等价物「顶替」，不要加参数碰运气。**

```
androidx.compose.ui.*
androidx.compose.foundation.*
androidx.compose.material3.*
androidx.compose.animation.*
OutlinedTextField
BasicTextField2
PullToRefreshBox
ClickableText
Icon / IconImage
Modifier.verticalScroll / horizontalScroll / scrollable
Modifier.nestedScroll(connection, dispatcher)   // 原生签名
Modifier.imePadding()
PasswordVisualTransformation()
TopAppBar(scrollBehavior = …)
LazyColumn(reverseLayout = …, flingBehavior = …)
ModalBottomSheet(sheetState / shape / dragHandle = …)  // 原生签名，Kuikly 无这些参数
```

嵌套滚动若需要，只用：

```kotlin
Modifier.nestedScroll(
    scrollUp = NestedScrollMode.SELF_FIRST,
    scrollDown = NestedScrollMode.SELF_FIRST
)
```

### 7.5 组件 × 能力对照（开发时逐项勾选）

| 组件 | 直接 Compose | 自定义绘制 | 降级 |
| :--- | :--- | :--- | :--- |
| StockCard | 是（Row/Text/Box） | 否 | Pressed 自行切色 |
| SignalBadge | 是 | 否 | Wrap 用多 Row |
| AISignalCard | 是（`LinearProgressIndicator(progress = { … })`） | 否 | 无环形进度 |
| AIAdviceChip | 是 | 否 | — |
| AITicker | 是（状态切换） | 否 | 无 fade；无 Emoji |
| StockChart | 容器是 | **折线必须 Canvas** | 面积用 `Brush.verticalGradient`；网格无虚线 |
| PriceChangeGroup | 是 | 否 | 等宽→固定列宽 |
| AIChatSheet | 是（`ModalBottomSheet` + `TextField` + `LazyColumn`） | 否 | 无 `OutlinedTextField`；加载用线性不确定条，不转圈 |

---

## 8. 开发检查清单（UI 任务结束前）

- [ ] 未引入 `androidx.compose.ui/foundation/material3/animation`
- [ ] 涨跌色仅为 `#E74C3C` / `#27AE60` / 平盘 `#8B95A8`
- [ ] AI 元素使用蓝/青，未把涨跌色当 AI 装饰
- [ ] 价格均为 2 位小数，上涨带 `+`
- [ ] 间距均为 4 的倍数；页边距 16dp；列表项高 72dp；图表高 220dp
- [ ] 卡片圆角 12dp；徽章 4dp；建议芯片 14dp
- [ ] 详情模块顺序与 6.3 一致
- [ ] 未使用 `verticalScroll`、`Icon`、`animateItem`、AppBar `scrollBehavior`（用了会编译失败，不是「不推荐」）
- [ ] AITicker 36dp、3s 切换、几何圆点而非 Emoji
- [ ] 返回键为 `common/ic_back.png`，不是 `Text("‹")`
- [ ] 问答 Sheet 按 5.8：预设问题用 `PresetQuestionChip` 而非 `AIAdviceChip`；输入为 `TextField` 而非 `OutlinedTextField`

---

*本文件是视觉唯一依据。若实现与本文冲突，改代码，不改口令式变通色值。变更设计必须先改本文件版本号。*
