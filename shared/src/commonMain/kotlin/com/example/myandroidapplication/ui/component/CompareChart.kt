package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.layout.onSizeChanged
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.IntSize
import com.tencent.kuikly.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

private const val COMPARE_WINDOW = 20
private const val BASE_INDEX = 100.0

/**
 * 对比页归一化走势。近 20 日以各股第一日收盘为 100，Canvas 折线模式对齐 [StockChart]。
 *
 * @param stocks 2–4 只对比股票
 */
@Composable
fun CompareChart(
    stocks: List<Stock>,
    modifier: Modifier = Modifier
) {
    val series = remember(stocks.map { it.symbol }) {
        stocks.take(4).mapIndexed { index, stock ->
            CompareSeries(
                name = stock.name,
                color = compareSeriesColor(index),
                values = normalizeLastDays(stock, COMPARE_WINDOW),
                times = stock.chartPoints.takeLast(COMPARE_WINDOW).map { it.time }
            )
        }.filter { it.values.size >= 2 }
    }
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.BgCard)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
            .padding(AppDimens.Space3)
    ) {
        Text(
            text = "近20日归一化走势",
            color = AppColors.TextSecondary,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            series.forEach { item ->
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.Space1)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(item.color, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = item.name,
                        color = AppColors.TextHint,
                        fontSize = AppType.Micro,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        ComparePlot(series = series)
    }
}

/**
 * 对比折线配色：主色 + 设计系统内三个辅助色，最多 4 条。
 */
fun compareSeriesColor(index: Int): Color = when (index) {
    0 -> AppColors.Primary
    1 -> AppColors.AI
    2 -> AppColors.Warning
    else -> AppColors.Ma20
}

@Composable
private fun ComparePlot(series: List<CompareSeries>) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val geometry = remember(boxSize, series, density) {
        if (boxSize.width == 0 || boxSize.height == 0 || series.isEmpty()) {
            null
        } else {
            CompareGeometry.from(boxSize, series, density)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .onSizeChanged { boxSize = it }
    ) {
        if (series.isEmpty()) {
            Text(
                text = "暂无对比走势",
                color = AppColors.TextHint,
                fontSize = AppType.Caption,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (geometry != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridCount = 4
                for (i in 0 until gridCount) {
                    val y = geometry.plotTop + geometry.plotHeight * i / (gridCount - 1)
                    drawLine(
                        color = AppColors.Divider,
                        start = Offset(geometry.plotLeft, y),
                        end = Offset(geometry.plotRight, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                val baseY = geometry.yOf(BASE_INDEX)
                drawLine(
                    color = AppColors.TextDisabled,
                    start = Offset(geometry.plotLeft, baseY),
                    end = Offset(geometry.plotRight, baseY),
                    strokeWidth = 1.dp.toPx()
                )
                series.forEach { item ->
                    val path = Path()
                    item.values.forEachIndexed { index, value ->
                        val x = geometry.xOf(index, item.values.size)
                        val y = geometry.yOf(value)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = item.color,
                        style = Stroke(width = max(2f, 1.5.dp.toPx()), cap = StrokeCap.Round)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                geometry.yLabels.forEach { label ->
                    Text(
                        text = label,
                        color = AppColors.TextHint,
                        fontSize = AppType.Micro,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(start = 44.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                geometry.xLabels.forEach { label ->
                    Text(
                        text = label,
                        color = AppColors.TextHint,
                        fontSize = AppType.Micro,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

private data class CompareSeries(
    val name: String,
    val color: Color,
    val values: List<Double>,
    val times: List<String>
)

private class CompareGeometry(
    val plotLeft: Float,
    val plotRight: Float,
    val plotTop: Float,
    val plotBottom: Float,
    val minValue: Double,
    val maxValue: Double,
    val yLabels: List<String>,
    val xLabels: List<String>
) {
    val plotWidth: Float get() = plotRight - plotLeft
    val plotHeight: Float get() = plotBottom - plotTop

    fun xOf(index: Int, count: Int): Float {
        if (count <= 1) return (plotLeft + plotRight) / 2f
        return plotLeft + plotWidth * index / (count - 1)
    }

    fun yOf(value: Double): Float {
        val range = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }
        val t = ((value - minValue) / range).toFloat()
        return plotBottom - plotHeight * t
    }

    companion object {
        fun from(size: IntSize, series: List<CompareSeries>, density: Density): CompareGeometry {
            val left = with(density) { 44.dp.toPx() }
            val right = size.width - with(density) { 8.dp.toPx() }
            val top = with(density) { 8.dp.toPx() }
            val bottom = size.height - with(density) { 24.dp.toPx() }
            val values = series.flatMap { it.values }
            val rawMin = values.minOrNull() ?: BASE_INDEX
            val rawMax = values.maxOrNull() ?: BASE_INDEX
            val pad = ((rawMax - rawMin) * 0.08).coerceAtLeast(1.0)
            val minValue = minOf(rawMin - pad, BASE_INDEX - 1.0)
            val maxValue = maxOf(rawMax + pad, BASE_INDEX + 1.0)
            val yLabels = (0..3).map { step ->
                val t = 1.0 - step / 3.0
                (minValue + (maxValue - minValue) * t).roundToInt().toString()
            }
            val times = series.maxByOrNull { it.times.size }?.times.orEmpty()
            val xLabels = if (times.size < 2) {
                times
            } else {
                listOf(times.first(), times[times.size / 2], times.last())
            }
            return CompareGeometry(
                plotLeft = left,
                plotRight = right,
                plotTop = top,
                plotBottom = bottom,
                minValue = minValue,
                maxValue = maxValue,
                yLabels = yLabels,
                xLabels = xLabels
            )
        }
    }
}

private fun normalizeLastDays(stock: Stock, count: Int): List<Double> {
    val window = stock.chartPoints.takeLast(count)
    val base = window.firstOrNull()?.price ?: return emptyList()
    if (base == 0.0) return window.map { BASE_INDEX }
    return window.map { it.price / base * BASE_INDEX }
}
