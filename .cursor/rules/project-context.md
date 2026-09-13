# 项目上下文
## 项目目标
基于Kuikly开发AI股票行情原型Demo，包含首页行情列表、个股详情、AI分析三大核心页面。

## 技术栈约束
- 框架：Kuikly Compose DSL
- 语言：Kotlin Multiplatform
- 网络：Kuikly NetworkModule
- 状态管理：observable / observableList
- 数据层：StockRepository接口 + MockStockRepository实现

## 代码规范
- 分层：页面/组件/数据/状态四层分离
- 共享代码放在 commonMain，平台特定代码放在 androidMain
- 所有组件必须有KotlinDoc注释

## 架构约束（不可违背）
- 不要使用 androidx.compose.*，所有组件来自 com.tencent.kuikly.compose.*
- 数据层必须抽象为接口，UI 通过 `AppContainer.stockRepository` 取数，不直接依赖 Mock 实现
- 通用组件（如StockCard、SignalBadge）放在 ui/component/ 下