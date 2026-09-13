package com.example.myandroidapplication.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.AppPages
import com.example.myandroidapplication.ui.component.AICompareCard
import com.example.myandroidapplication.ui.component.AdviceSupportPoints
import com.example.myandroidapplication.ui.component.CompareChart
import com.example.myandroidapplication.ui.component.CompareColumn
import com.example.myandroidapplication.ui.component.MetricCompareBar
import com.example.myandroidapplication.ui.component.NavBackButton
import com.example.myandroidapplication.ui.component.QuoteTopBar
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.RouterModule

/**
 * 多股对比页。并排 2–4 列指标，中部归一化走势，底部 [AICompareCard]。
 */
@Page(AppPages.COMPARE)
internal class ComparePage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        val statusBarDp = pagerData.statusBarHeight.dp
        setContent {
            CompareScreen(
                statusBarHeight = statusBarDp,
                onBack = {
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
                }
            )
        }
    }
}

@Composable
private fun CompareScreen(
    statusBarHeight: Dp,
    onBack: () -> Unit
) {
    val rawCount = AppContainer.compareSymbols.size
    val stocks = remember(rawCount, AppContainer.compareSymbols.toList()) {
        AppContainer.resolvedCompareStocks()
    }
    val truncated = rawCount > AppContainer.COMPARE_MAX
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgPage)
    ) {
        QuoteTopBar(
            title = "对比",
            statusBarHeight = statusBarHeight,
            titleCentered = true,
            navigation = { NavBackButton(onBack) }
        )
        if (stocks.size < 2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "请勾选 2–4 只股票再对比",
                    color = AppColors.TextHint,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Normal,
                    lineHeight = AppType.CalloutLine
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = AppDimens.Space4,
                    end = AppDimens.Space4,
                    top = AppDimens.Space3,
                    bottom = AppDimens.Space6
                ),
                beyondBoundsItemCount = 3
            ) {
                if (truncated) {
                    item(key = "truncate_hint") {
                        Text(
                            text = "最多对比 4 只，已截断",
                            color = AppColors.TextHint,
                            fontSize = AppType.Callout,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = AppDimens.Space3)
                        )
                    }
                }
                item(key = "compare_columns") {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        stocks.forEach { stock ->
                            CompareColumn(
                                stock = stock,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                item(key = "compare_gap_chart") {
                    Spacer(modifier = Modifier.height(AppDimens.Space3))
                }
                item(key = "compare_chart") {
                    CompareChart(stocks = stocks)
                }
                item(key = "compare_gap_metrics") {
                    Spacer(modifier = Modifier.height(AppDimens.Space3))
                }
                item(key = "compare_metrics") {
                    MetricCompareBar(stocks = stocks)
                }
                item(key = "compare_gap_ai") {
                    Spacer(modifier = Modifier.height(AppDimens.Space3))
                }
                item(key = "compare_ai") {
                    AICompareCard(stocks = stocks)
                }
                item(key = "compare_gap_reasons") {
                    Spacer(modifier = Modifier.height(AppDimens.Space3))
                }
                item(key = "compare_advice_reasons") {
                    CompareAdviceReasons(stocks = stocks)
                }
            }
        }
    }
}

/**
 * 对比页每只股票建议下方的支撑点，复用 [AdviceSupportPoints]。
 */
@Composable
private fun CompareAdviceReasons(stocks: List<Stock>) {
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.BgCardAI)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
            .padding(AppDimens.Space4),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space3)
    ) {
        Text(
            text = "建议支撑点",
            color = AppColors.TextSecondary,
            fontSize = AppType.Body,
            fontWeight = FontWeight.SemiBold
        )
        stocks.forEach { stock ->
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Space2)) {
                Text(
                    text = "${stock.name}：${stock.aiAdvice}",
                    color = AppColors.TextTitle,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium
                )
                AdviceSupportPoints(reasons = stock.adviceReasons)
            }
        }
    }
}
