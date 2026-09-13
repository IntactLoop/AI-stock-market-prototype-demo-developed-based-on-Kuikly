package com.example.myandroidapplication.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.AdjustType
import com.example.myandroidapplication.data.model.ChartPeriod
import com.example.myandroidapplication.data.model.ChartType
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.AppPages
import com.example.myandroidapplication.ui.component.AIAdviceCard
import com.example.myandroidapplication.ui.component.AIChatEntry
import com.example.myandroidapplication.ui.component.AIChatSheet
import com.example.myandroidapplication.ui.component.AISignalCard
import com.example.myandroidapplication.ui.component.AISummaryCard
import com.example.myandroidapplication.ui.component.AITicker
import com.example.myandroidapplication.ui.component.AITrendRow
import com.example.myandroidapplication.ui.component.AdjustSelector
import com.example.myandroidapplication.ui.component.ChartTypeToggle
import com.example.myandroidapplication.ui.component.NavBackButton
import com.example.myandroidapplication.ui.component.PeriodTab
import com.example.myandroidapplication.ui.component.PriceChangeGroup
import com.example.myandroidapplication.ui.component.QuoteMetricsBlock
import com.example.myandroidapplication.ui.component.QuoteTopBar
import com.example.myandroidapplication.ui.component.StockChart
import com.example.myandroidapplication.ui.component.TagChip
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
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
            val hasTags = stock.tags.isNotEmpty()
            val itemSignal = if (hasTags) 4 else 3
            val itemAdvice = itemSignal + 1
            val itemTrend = itemAdvice + 1
            var period by remember(stock.symbol) { mutableStateOf(ChartPeriod.DAILY) }
            var chartType by remember(stock.symbol) { mutableStateOf(ChartType.LINE) }
            var adjust by remember(stock.symbol) { mutableStateOf(AdjustType.NONE) }
            val candleEnabled = period != ChartPeriod.INTRADAY && period != ChartPeriod.FIVE_DAY
            val effectiveType = if (candleEnabled) chartType else ChartType.LINE
            val candles = remember(stock.symbol, period, effectiveType, adjust) {
                AppContainer.stockRepository.getChartData(
                    symbol = stock.symbol,
                    period = period,
                    type = effectiveType,
                    adjust = adjust
                )
            }
            if (hasTicker) {
                AITicker(
                    alerts = stock.alerts,
                    onClick = { alert ->
                        val target = when (alert.type) {
                            Stock.ALERT_RISK -> itemAdvice
                            Stock.ALERT_TREND -> itemTrend
                            else -> itemSignal
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
                if (hasTags) {
                    item {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = AppDimens.Space4,
                                end = AppDimens.Space4,
                                top = AppDimens.Space2
                            ),
                            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2),
                            beyondBoundsItemCount = 3
                        ) {
                            items(
                                items = stock.tags,
                                key = { it }
                            ) { tag ->
                                TagChip(text = tag)
                            }
                        }
                    }
                }
                item {
                    QuoteMetricsBlock(stock = stock)
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimens.Space4)
                            .padding(top = AppDimens.Space4)
                    ) {
                        PeriodTab(
                            selected = period,
                            onSelected = {
                                period = it
                                selectedIndex = null
                                if (it == ChartPeriod.INTRADAY || it == ChartPeriod.FIVE_DAY) {
                                    chartType = ChartType.LINE
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(AppDimens.Space2))
                        AdjustSelector(
                            selected = adjust,
                            onSelected = { adjust = it }
                        )
                        Spacer(modifier = Modifier.height(AppDimens.Space2))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ChartTypeToggle(
                                selected = effectiveType,
                                enabled = candleEnabled,
                                onSelected = { chartType = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(AppDimens.Space2))
                        StockChart(
                            candles = candles,
                            chartType = effectiveType,
                            selectedIndex = selectedIndex,
                            onPointSelected = { selectedIndex = it },
                            marks = stock.aiChartMarks,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
