package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.model.CandlePoint
import com.example.myandroidapplication.data.model.ChartMark
import com.example.myandroidapplication.data.model.ChartPoint
import com.example.myandroidapplication.data.model.ChartType
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.gestures.detectTapGestures
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawScope
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.layout.onSizeChanged
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.IntSize
import com.tencent.kuikly.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 个股走势图。视觉规格见设计系统 5.6 节。
 *
 * @param candles K 线点列；折线模式用收盘价
 * @param chartType 折线或 K 线，默认折线以保持 Phase 2 视觉
 * @param selectedIndex 选中点，null 表示未选中
 * @param onPointSelected 点击时间点回调，供图表联动
 * @param marks AI 自动标注，坐标对齐当前点列；点击弹出解读气泡
 *
 * 主图高度锁定 [AppDimens.HeightChart] 220dp；成交量副图固定 64dp，间距 [AppDimens.Space2]。
 */
@Composable
fun StockChart(
    candles: List<CandlePoint>,
    chartType: ChartType = ChartType.LINE,
    selectedIndex: Int?,
    onPointSelected: (Int) -> Unit,
    marks: List<ChartMark> = emptyList(),
    modifier: Modifier = Modifier
) {
    val points = remember(candles) {
        candles.map { ChartPoint(time = it.time, price = it.close, volume = it.volume) }
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
        ChartLegend()
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        ChartPlot(
            points = points,
            candles = candles,
            chartType = chartType,
            selectedIndex = selectedIndex,
            onPointSelected = onPointSelected,
            marks = marks
        )
        if (candles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(AppDimens.Space2))
            VolumePlot(
                candles = candles,
                chartType = chartType,
                selectedIndex = selectedIndex,
                onPointSelected = onPointSelected
            )
        }
    }
}

@Composable
private fun ChartLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space3)
    ) {
        LegendItem(color = AppColors.Warning, label = "MA5")
        LegendItem(color = AppColors.Primary, label = "MA10")
        LegendItem(color = AppColors.Ma20, label = "MA20")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space1)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            color = AppColors.TextHint,
            fontSize = AppType.Micro,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ChartPlot(
    points: List<ChartPoint>,
    candles: List<CandlePoint>,
    chartType: ChartType,
    selectedIndex: Int?,
    onPointSelected: (Int) -> Unit,
    marks: List<ChartMark>
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var tipSize by remember { mutableStateOf(IntSize.Zero) }
    var markTipSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val alignedMarks = remember(marks, points.size) { alignChartMarks(marks, points.size) }
    var selectedMark by remember(points, alignedMarks) { mutableStateOf<ChartMark?>(null) }
    val prices = remember(points) { points.map { it.price } }
    val ma5 = remember(prices) { movingAverage(prices, 5) }
    val ma10 = remember(prices) { movingAverage(prices, 10) }
    val ma20 = remember(prices) { movingAverage(prices, 20) }
    val candleExtremes = remember(candles, chartType) {
        if (chartType == ChartType.CANDLE) {
            candles.flatMap { listOf(it.high, it.low) }
        } else {
            emptyList()
        }
    }
    val geometry = remember(boxSize, points, ma5, ma10, ma20, candleExtremes, density) {
        if (boxSize.width == 0 || boxSize.height == 0 || points.isEmpty()) {
            null
        } else {
            ChartGeometry.from(boxSize, points, ma5, ma10, ma20, candleExtremes, density)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.HeightChart)
            .onSizeChanged { boxSize = it }
    ) {
        if (points.isEmpty()) {
            Text(
                text = "暂无走势",
                color = AppColors.TextHint,
                fontSize = AppType.Caption,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (geometry != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points, candles, chartType, geometry, alignedMarks, onPointSelected) {
                        detectTapGestures { offset ->
                            val hitRadius = 16.dp.toPx()
                            val hit = nearestChartMark(
                                tap = offset,
                                marks = alignedMarks,
                                points = points,
                                geometry = geometry,
                                radiusPx = hitRadius
                            )
                            selectedMark = hit
                            if (hit != null) {
                                onPointSelected(hit.pointIndex)
                            } else {
                                onPointSelected(geometry.nearestIndex(offset.x, points.size))
                            }
                        }
                    }
            ) {
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
                if (chartType == ChartType.LINE) {
                    val areaPath = Path()
                    var lineTop = geometry.plotBottom
                    points.forEachIndexed { index, point ->
                        val x = geometry.xOf(index, points.size)
                        val y = geometry.yOf(point.price)
                        lineTop = minOf(lineTop, y)
                        if (index == 0) {
                            areaPath.moveTo(x, y)
                        } else {
                            areaPath.lineTo(x, y)
                        }
                    }
                    areaPath.lineTo(geometry.xOf(points.lastIndex, points.size), geometry.plotBottom)
                    areaPath.lineTo(geometry.xOf(0, points.size), geometry.plotBottom)
                    areaPath.close()
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(AppColors.ChartArea, Color.Transparent),
                            startY = lineTop,
                            endY = geometry.plotBottom
                        )
                    )
                    drawPolyline(geometry, prices, AppColors.Primary, max(2f, 1.5.dp.toPx()))
                } else {
                    drawCandles(geometry, candles)
                }
                drawPolyline(geometry, ma5, AppColors.Warning, 1.dp.toPx())
                drawPolyline(geometry, ma10, AppColors.Primary, 1.dp.toPx())
                drawPolyline(geometry, ma20, AppColors.Ma20, 1.dp.toPx())
                alignedMarks.forEach { mark ->
                    if (mark.pointIndex !in points.indices) return@forEach
                    val markX = geometry.xOf(mark.pointIndex, points.size)
                    val markY = geometry.yOf(points[mark.pointIndex].price)
                    val markColor = chartMarkColor(mark.type)
                    val active = selectedMark?.pointIndex == mark.pointIndex
                    drawCircle(
                        color = markColor,
                        radius = (if (active) 7.dp else 6.dp).toPx(),
                        center = Offset(markX, markY),
                        alpha = 0.28f
                    )
                    drawCircle(
                        color = markColor,
                        radius = 3.dp.toPx(),
                        center = Offset(markX, markY)
                    )
                }
                val selected = selectedIndex?.takeIf { it in points.indices }
                if (selected != null) {
                    val x = geometry.xOf(selected, points.size)
                    val y = geometry.yOf(points[selected].price)
                    drawLine(
                        color = AppColors.Primary,
                        start = Offset(x, geometry.plotTop),
                        end = Offset(x, geometry.plotBottom),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawCircle(
                        color = AppColors.Primary,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = AppColors.BgCard,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
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
            val selected = selectedIndex?.takeIf { it in points.indices }
            val markForBubble = selectedMark?.takeIf { it.pointIndex in points.indices }
            if (selected != null && markForBubble?.pointIndex != selected) {
                val pointX = geometry.xOf(selected, points.size)
                val pointY = geometry.yOf(points[selected].price)
                val tipHeightPx = with(density) { 22.dp.toPx() }
                val tipWidthPx = if (tipSize.width > 0) {
                    tipSize.width.toFloat()
                } else {
                    with(density) { 72.dp.toPx() }
                }
                val gapPx = with(density) { 4.dp.toPx() }
                val outerRadiusPx = with(density) { 5.dp.toPx() }
                val maxX = max(0f, boxSize.width - tipWidthPx)
                val clampedX = (pointX - tipWidthPx / 2f).coerceIn(0f, maxX)
                val aboveY = pointY - outerRadiusPx - gapPx - tipHeightPx
                val tipY = if (aboveY >= 0f) aboveY else pointY + outerRadiusPx + gapPx
                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { clampedX.toDp() },
                            y = with(density) { tipY.toDp() }
                        )
                        .onSizeChanged { tipSize = it }
                        .height(22.dp)
                        .background(AppColors.BgElevated, RoundedCornerShape(AppDimens.RadiusBadge))
                        .border(
                            AppDimens.StrokeDivider,
                            AppColors.Primary,
                            RoundedCornerShape(AppDimens.RadiusBadge)
                        )
                        .padding(horizontal = AppDimens.Space2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = QuoteFormat.price(points[selected].price),
                        color = AppColors.TextTitle,
                        fontSize = AppType.Micro,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            if (markForBubble != null) {
                val pointX = geometry.xOf(markForBubble.pointIndex, points.size)
                val pointY = geometry.yOf(points[markForBubble.pointIndex].price)
                val tipHeightPx = with(density) { 44.dp.toPx() }
                val tipWidthPx = if (markTipSize.width > 0) {
                    markTipSize.width.toFloat()
                } else {
                    with(density) { 168.dp.toPx() }
                }
                val gapPx = with(density) { 6.dp.toPx() }
                val outerRadiusPx = with(density) { 7.dp.toPx() }
                val maxX = max(0f, boxSize.width - tipWidthPx)
                val clampedX = (pointX - tipWidthPx / 2f).coerceIn(0f, maxX)
                val aboveY = pointY - outerRadiusPx - gapPx - tipHeightPx
                val tipY = if (aboveY >= 0f) aboveY else pointY + outerRadiusPx + gapPx
                val markColor = chartMarkColor(markForBubble.type)
                Column(
                    modifier = Modifier
                        .offset(
                            x = with(density) { clampedX.toDp() },
                            y = with(density) { tipY.toDp() }
                        )
                        .onSizeChanged { markTipSize = it }
                        .width(168.dp)
                        .background(AppColors.BgElevated, RoundedCornerShape(AppDimens.RadiusBadge))
                        .border(
                            AppDimens.StrokeDivider,
                            markColor,
                            RoundedCornerShape(AppDimens.RadiusBadge)
                        )
                        .padding(horizontal = AppDimens.Space2, vertical = AppDimens.Space1)
                ) {
                    Text(
                        text = markForBubble.label,
                        color = markColor,
                        fontSize = AppType.Caption,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = markForBubble.note,
                        color = AppColors.TextBody,
                        fontSize = AppType.Caption,
                        fontWeight = FontWeight.Normal,
                        lineHeight = AppType.CaptionLine,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

private val HeightVolume = 64.dp

@Composable
private fun VolumePlot(
    candles: List<CandlePoint>,
    chartType: ChartType,
    selectedIndex: Int?,
    onPointSelected: (Int) -> Unit
) {
    val density = LocalDensity.current
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val maxVolume = remember(candles) { candles.maxOfOrNull { it.volume }?.coerceAtLeast(1L) ?: 1L }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeightVolume)
            .onSizeChanged { boxSize = it }
            .pointerInput(candles, chartType, onPointSelected, boxSize) {
                if (boxSize.width <= 0 || candles.isEmpty()) return@pointerInput
                val left = with(density) { 44.dp.toPx() }
                val right = boxSize.width - with(density) { 8.dp.toPx() }
                val width = (right - left).coerceAtLeast(1f)
                detectTapGestures { offset ->
                    val t = ((offset.x - left) / width).coerceIn(0f, 1f)
                    val index = if (candles.size <= 1) {
                        0
                    } else {
                        (t * (candles.size - 1)).roundToInt()
                    }
                    onPointSelected(index)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (candles.isEmpty() || boxSize.width == 0) return@Canvas
            val left = 44.dp.toPx()
            val right = size.width - 8.dp.toPx()
            val top = 4.dp.toPx()
            val bottom = size.height - 4.dp.toPx()
            val plotWidth = (right - left).coerceAtLeast(1f)
            val plotHeight = (bottom - top).coerceAtLeast(1f)
            val count = candles.size
            val slot = if (count <= 1) plotWidth else plotWidth / (count - 1)
            val half = (slot * 0.32f).coerceIn(1.5f, 6f)
            candles.forEachIndexed { index, candle ->
                val x = if (count <= 1) {
                    (left + right) / 2f
                } else {
                    left + plotWidth * index / (count - 1)
                }
                val barHeight = (candle.volume.toFloat() / maxVolume.toFloat()) * plotHeight
                val color = if (candle.close >= candle.open) AppColors.Rise else AppColors.Fall
                drawRect(
                    color = color,
                    topLeft = Offset(x - half, bottom - barHeight),
                    size = Size(half * 2f, barHeight.coerceAtLeast(1f))
                )
            }
            val selected = selectedIndex?.takeIf { it in candles.indices }
            if (selected != null) {
                val x = if (count <= 1) {
                    (left + right) / 2f
                } else {
                    left + plotWidth * selected / (count - 1)
                }
                drawLine(
                    color = AppColors.Primary,
                    start = Offset(x, top),
                    end = Offset(x, bottom),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

private fun DrawScope.drawCandles(
    geometry: ChartGeometry,
    candles: List<CandlePoint>
) {
    if (candles.isEmpty()) return
    val count = candles.size
    val slot = if (count <= 1) geometry.plotWidth else geometry.plotWidth / (count - 1)
    val half = (slot * 0.32f).coerceIn(1.5f, 6f)
    val wickWidth = max(1f, 1.dp.toPx())
    candles.forEachIndexed { index, candle ->
        val x = geometry.xOf(index, count)
        val yHigh = geometry.yOf(candle.high)
        val yLow = geometry.yOf(candle.low)
        val yOpen = geometry.yOf(candle.open)
        val yClose = geometry.yOf(candle.close)
        val rise = candle.close >= candle.open
        val color = if (rise) AppColors.Rise else AppColors.Fall
        drawLine(
            color = color,
            start = Offset(x, yHigh),
            end = Offset(x, yLow),
            strokeWidth = wickWidth
        )
        val bodyTop = min(yOpen, yClose)
        val bodyHeight = max(1f, abs(yClose - yOpen))
        drawRect(
            color = color,
            topLeft = Offset(x - half, bodyTop),
            size = Size(half * 2f, bodyHeight)
        )
    }
}

private fun DrawScope.drawPolyline(
    geometry: ChartGeometry,
    values: List<Double>,
    color: Color,
    strokeWidth: Float
) {
    if (values.size < 2) return
    val path = Path()
    values.forEachIndexed { index, value ->
        val x = geometry.xOf(index, values.size)
        val y = geometry.yOf(value)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun alignChartMarks(marks: List<ChartMark>, count: Int): List<ChartMark> {
    if (count <= 0 || marks.isEmpty()) return emptyList()
    val sourceLast = marks.maxOf { it.pointIndex }.coerceAtLeast(1)
    val targetLast = (count - 1).coerceAtLeast(1)
    return marks.map { mark ->
        val mapped = if (sourceLast == targetLast) {
            mark.pointIndex
        } else {
            (mark.pointIndex.toFloat() / sourceLast * targetLast).roundToInt()
        }.coerceIn(0, count - 1)
        mark.copy(pointIndex = mapped)
    }.distinctBy { it.pointIndex }
}

private fun nearestChartMark(
    tap: Offset,
    marks: List<ChartMark>,
    points: List<ChartPoint>,
    geometry: ChartGeometry,
    radiusPx: Float
): ChartMark? {
    var best: ChartMark? = null
    var bestDistance = radiusPx
    marks.forEach { mark ->
        if (mark.pointIndex !in points.indices) return@forEach
        val x = geometry.xOf(mark.pointIndex, points.size)
        val y = geometry.yOf(points[mark.pointIndex].price)
        val distance = hypot(tap.x - x, tap.y - y)
        if (distance <= bestDistance) {
            bestDistance = distance
            best = mark
        }
    }
    return best
}

private fun chartMarkColor(type: String): Color = when (type) {
    ChartMark.TYPE_BUY -> AppColors.Rise
    ChartMark.TYPE_BREAK -> AppColors.Fall
    ChartMark.TYPE_RESISTANCE -> AppColors.Warning
    ChartMark.TYPE_SUPPORT -> AppColors.AI
    else -> AppColors.Primary
}

private fun movingAverage(values: List<Double>, window: Int): List<Double> {
    if (values.isEmpty()) return emptyList()
    return values.indices.map { index ->
        val from = max(0, index - window + 1)
        values.subList(from, index + 1).average()
    }
}

private class ChartGeometry(
    val plotLeft: Float,
    val plotRight: Float,
    val plotTop: Float,
    val plotBottom: Float,
    val minPrice: Double,
    val maxPrice: Double,
    val yLabels: List<String>,
    val xLabels: List<String>
) {
    val plotWidth: Float get() = plotRight - plotLeft
    val plotHeight: Float get() = plotBottom - plotTop

    fun xOf(index: Int, count: Int): Float {
        if (count <= 1) return (plotLeft + plotRight) / 2f
        return plotLeft + plotWidth * index / (count - 1)
    }

    fun yOf(price: Double): Float {
        val range = (maxPrice - minPrice).let { if (it == 0.0) 1.0 else it }
        val t = ((price - minPrice) / range).toFloat()
        return plotBottom - plotHeight * t
    }

    fun nearestIndex(x: Float, count: Int): Int {
        if (count <= 1) return 0
        val t = ((x - plotLeft) / plotWidth).coerceIn(0f, 1f)
        return (t * (count - 1)).roundToInt()
    }

    companion object {
        fun from(
            size: IntSize,
            points: List<ChartPoint>,
            ma5: List<Double>,
            ma10: List<Double>,
            ma20: List<Double>,
            extraRange: List<Double>,
            density: Density
        ): ChartGeometry {
            val left = with(density) { 44.dp.toPx() }
            val right = size.width - with(density) { 8.dp.toPx() }
            val top = with(density) { 8.dp.toPx() }
            val bottom = size.height - with(density) { 24.dp.toPx() }
            val prices = points.map { it.price }
            val series = prices + ma5 + ma10 + ma20 + extraRange
            val rawMin = series.minOrNull() ?: 0.0
            val rawMax = series.maxOrNull() ?: 0.0
            val pad = max((rawMax - rawMin) * 0.04, max(rawMax * 0.002, 0.01))
            val minPrice = rawMin - pad
            val maxPrice = rawMax + pad
            val yLabels = (0..3).map { step ->
                val price = maxPrice - (maxPrice - minPrice) * step / 3.0
                QuoteFormat.price(price)
            }
            val last = points.lastIndex
            val xIndexes = if (points.size <= 4) {
                points.indices.toList()
            } else {
                listOf(0, last / 3, last * 2 / 3, last)
            }
            return ChartGeometry(
                plotLeft = left,
                plotRight = right,
                plotTop = top,
                plotBottom = bottom,
                minPrice = minPrice,
                maxPrice = maxPrice,
                yLabels = yLabels,
                xLabels = xIndexes.map { points[it].time }
            )
        }
    }
}
