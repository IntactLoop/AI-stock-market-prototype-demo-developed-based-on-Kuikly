package com.example.myandroidapplication.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.AppPages
import com.example.myandroidapplication.ui.component.AIAdviceCard
import com.example.myandroidapplication.ui.component.AIChatEntry
import com.example.myandroidapplication.ui.component.AIChatSheet
import com.example.myandroidapplication.ui.component.AISignalCard
import com.example.myandroidapplication.ui.component.AISummaryCard
import com.example.myandroidapplication.ui.component.AITicker
import com.example.myandroidapplication.ui.component.AITrendRow
import com.example.myandroidapplication.ui.component.NavBackButton
import com.example.myandroidapplication.ui.component.PriceChangeGroup
import com.example.myandroidapplication.ui.component.QuoteMetricsBlock
import com.example.myandroidapplication.ui.component.QuoteTopBar
import com.example.myandroidapplication.ui.component.StockChart
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.RouterModule

/**
 * 个股详情页：基础行情、盯盘提醒、走势图与 AI 分析；图表选中点联动信号解读。
 */
@Page(AppPages.STOCK_DETAIL)
internal class StockDetailPage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        val symbol = pagerData.params.optString(AppPages.ARG_SYMBOL)
        val stock = AppContainer.stockRepository.getStock(symbol)
        val statusBarDp = pagerData.statusBarHeight.dp
        val pageViewHeight = pagerData.pageViewHeight
        setContent {
            StockDetailScreen(
                stock = stock,
                statusBarHeight = statusBarDp,
                pageViewHeight = pageViewHeight,
                onBack = {
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
                }
            )
        }
    }
}

@Composable
private fun StockDetailScreen(
    stock: Stock?,
    statusBarHeight: Dp,
    pageViewHeight: Float,
    onBack: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetHeight = (pageViewHeight * 0.72f).coerceIn(480f, 640f).dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgPage)
    ) {
        QuoteTopBar(
            title = stock?.name ?: "行情",
            statusBarHeight = statusBarHeight,
            titleCentered = true,
            navigation = { NavBackButton(onBack) }
        )
        if (stock == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "未找到该股票",
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption
                )
            }
        } else {
            val listState = rememberLazyListState()
            val hasTicker = stock.alerts.isNotEmpty()
            if (hasTicker) {
                AITicker(
                    alerts = stock.alerts,
                    onClick = { alert ->
                        val target = when (alert.type) {
                            Stock.ALERT_RISK -> ITEM_ADVICE
                            Stock.ALERT_TREND -> ITEM_TREND
                            else -> ITEM_SIGNAL
                        }
                        listState.requestScrollToItem(target)
                    }
                )
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                beyondBoundsItemCount = 3
            ) {
                if (!hasTicker) {
                    item {
                        Box(modifier = Modifier.height(AppDimens.Space8))
                    }
                }
                item {
                    PriceChangeGroup(stock = stock)
                }
                item {
                    QuoteMetricsBlock(stock = stock)
                }
                item {
                    StockChart(
                        points = stock.chartPoints,
                        selectedIndex = selectedIndex,
                        onPointSelected = { selectedIndex = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimens.Space4)
                            .padding(top = AppDimens.Space4)
                    )
                }
                item {
                    AISignalCard(
                        stock = stock,
                        selectedPointIndex = selectedIndex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimens.Space4)
                            .padding(top = AppDimens.Space3)
                    )
                }
                item {
                    AIAdviceCard(
                        stock = stock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimens.Space4)
                            .padding(top = AppDimens.Space3)
                    )
                }
                item {
                    AITrendRow(
                        stock = stock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimens.Space4)
                            .padding(top = AppDimens.Space3)
                    )
                }
                item {
                    AISummaryCard(
                        stock = stock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimens.Space4)
                            .padding(top = AppDimens.Space3)
                    )
                }
                item {
                    Box(modifier = Modifier.height(AppDimens.Space6 + AppDimens.Space4))
                }
            }
            AIChatEntry(onClick = { showSheet = true })
            if (showSheet) {
                AIChatSheet(
                    stock = stock,
                    sheetHeight = sheetHeight,
                    onDismissRequest = { showSheet = false }
                )
            }
        }
    }
}

/** LazyColumn 在有 AITicker、无顶部 Space8 时的模块下标。 */
private const val ITEM_SIGNAL = 3
private const val ITEM_ADVICE = 4
private const val ITEM_TREND = 5
