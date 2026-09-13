package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 对比页关键指标横向条。估值 / 成长 / 动量 / 波动 / 流动性，数值与对比表格同源。
 *
 * @param stocks 2–4 只对比股票
 */
@Composable
fun MetricCompareBar(
    stocks: List<Stock>,
    modifier: Modifier = Modifier
) {
    val groups = remember(stocks.map { it.symbol }.joinToString()) {
        buildMetricGroups(stocks.take(4))
    }
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.BgCard)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
            .padding(AppDimens.Space4),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space3)
    ) {
        Text(
            text = "关键指标对比",
            color = AppColors.TextSecondary,
            fontSize = AppType.Body,
            fontWeight = FontWeight.SemiBold
        )
        groups.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Space2)) {
                Text(
                    text = group.label,
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Medium
                )
                group.rows.forEach { row ->
                    MetricBarRow(row = row)
                }
            }
        }
    }
}

@Composable
private fun MetricBarRow(row: MetricBarRowData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)
    ) {
        Text(
            text = row.name,
            color = AppColors.TextTitle,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(56.dp)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.Divider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(row.ratio)
                    .background(row.color)
            )
            val rest = 1f - row.ratio
            if (rest > 0.001f) {
                Spacer(modifier = Modifier.weight(rest))
            }
        }
        Text(
            text = row.valueText,
            color = AppColors.TextTitle,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(64.dp)
        )
    }
}

private data class MetricBarRowData(
    val name: String,
    val valueText: String,
    val ratio: Float,
    val color: Color
)

private data class MetricGroup(
    val label: String,
    val rows: List<MetricBarRowData>
)

private fun buildMetricGroups(stocks: List<Stock>): List<MetricGroup> {
    if (stocks.isEmpty()) return emptyList()
    return listOf(
        metricGroup("估值", stocks) { QuoteFormat.price(it.pe) to it.pe },
        metricGroup("成长", stocks) { QuoteFormat.percent(it.revenueGrowth) to it.revenueGrowth },
        metricGroup("动量", stocks) { QuoteFormat.percent(it.changePercent) to it.changePercent },
        metricGroup("波动", stocks) {
            val range = if (it.close == 0.0) 0.0 else (it.high - it.low) / it.close * 100.0
            QuoteFormat.percent(range) to range
        },
        metricGroup("流动性", stocks) {
            "${QuoteFormat.price(it.turnoverRate)}%" to it.turnoverRate
        }
    )
}

private fun metricGroup(
    label: String,
    stocks: List<Stock>,
    metric: (Stock) -> Pair<String, Double>
): MetricGroup {
    val values = stocks.map { metric(it).second }
    val ratios = proportionalRatios(values)
    return MetricGroup(
        label = label,
        rows = stocks.mapIndexed { index, stock ->
            MetricBarRowData(
                name = stock.name,
                valueText = metric(stock).first,
                ratio = ratios[index],
                color = compareSeriesColor(index)
            )
        }
    )
}

private fun proportionalRatios(values: List<Double>): List<Float> {
    if (values.isEmpty()) return emptyList()
    val min = values.min()
    val max = values.max()
    if (max == min) return values.map { 0.72f }
    return values.map { value ->
        (((value - min) / (max - min)).toFloat() * 0.88f + 0.12f).coerceIn(0.12f, 1f)
    }
}
