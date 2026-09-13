package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.model.ReasoningStep
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
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.LinearProgressIndicator
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.layout.onSizeChanged
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * AI 信号解读卡片。视觉规格见设计系统 5.3 节。
 *
 * @param stock 当前个股，信号与解读必须与其数据一致
 * @param selectedPointIndex 走势图选中点；非空时解读切换为该时点分析
 */
@Composable
fun AISignalCard(
    stock: Stock,
    selectedPointIndex: Int? = null,
    modifier: Modifier = Modifier
) {
    var selectedSignal by remember(stock.symbol, selectedPointIndex) { mutableStateOf<String?>(null) }
    val visibleSignals = remember(stock.signals) { visibleSignalLabels(stock.signals) }
    val badge = selectedSignal
    val interpretation = when {
        badge != null && badge in stock.signals -> signalExplanation(stock, badge)
        selectedPointIndex != null -> chartPointInterpretation(stock, selectedPointIndex)
        else -> stock.aiInterpretation
    }
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    val confidence = stock.aiConfidence.coerceIn(0, 100)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppDimens.HeightAiSignalCardMin)
            .clip(shape)
            .background(AppColors.BgCardAI)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
    ) {
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .width(AppDimens.StrokeAccent)
                    .fillMaxHeight()
                    .background(AppColors.AI)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppDimens.Space4)
            ) {
                Text(
                    text = "AI 信号解读",
                    color = AppColors.TextSecondary,
                    fontSize = AppType.Body,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(AppDimens.Space3))
                SignalBadgeWrap(
                    labels = visibleSignals,
                    selectedLabel = selectedSignal,
                    onSelect = { label ->
                        selectedSignal = if (selectedSignal == label) null else label
                    }
                )
                val steps = stock.aiReasoningSteps
                if (steps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppDimens.Space3))
                    ReasoningChain(steps = steps)
                }
                Spacer(modifier = Modifier.height(AppDimens.Space3))
                Text(
                    text = interpretation,
                    color = AppColors.TextBody,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Normal,
                    lineHeight = AppType.CalloutLine,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(AppDimens.Space3))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$confidence%",
                        color = AppColors.AI,
                        fontSize = AppType.Caption,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " 置信度",
                        color = AppColors.TextHint,
                        fontSize = AppType.Caption,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.Space2))
                LinearProgressIndicator(
                    progress = { confidence / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimens.HeightProgress)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AppColors.Primary,
                    trackColor = AppColors.Divider
                )
            }
        }
    }
}

/**
 * 分步推理链。插在徽章行与解读正文之间，不改置信度条与徽章。
 */
@Composable
private fun ReasoningChain(steps: List<ReasoningStep>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)
    ) {
        steps.forEach { step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = step.marker,
                    color = reasoningMarkerColor(step.marker),
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = AppDimens.Space2)
                )
                Text(
                    text = step.text,
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Normal,
                    lineHeight = AppType.CaptionLine,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun reasoningMarkerColor(marker: String) = when (marker) {
    ReasoningStep.MARK_OK -> AppColors.AI
    ReasoningStep.MARK_WARN -> AppColors.Warning
    else -> AppColors.Primary
}

@Composable
private fun SignalBadgeWrap(
    labels: List<String>,
    selectedLabel: String?,
    onSelect: (String) -> Unit
) {
    var rowWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val rows = remember(labels, rowWidthPx, density) {
        wrapSignalRows(labels, rowWidthPx, density)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { rowWidthPx = it.width },
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space2)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)) {
                row.forEach { label ->
                    val overflow = label.startsWith("+")
                    SignalBadge(
                        label = label,
                        selected = !overflow && selectedLabel == label,
                        onClick = if (overflow) null else ({ onSelect(label) })
                    )
                }
            }
        }
    }
}

internal fun visibleSignalLabels(signals: List<String>): List<String> {
    if (signals.size <= 4) return signals
    return signals.take(4) + "+${signals.size - 4}"
}

internal fun wrapSignalRows(
    labels: List<String>,
    maxWidthPx: Int,
    density: Density
): List<List<String>> {
    if (labels.isEmpty()) return emptyList()
    if (maxWidthPx <= 0) return listOf(labels)
    val gapPx = with(density) { AppDimens.Space2.roundToPx() }
    val rows = mutableListOf<MutableList<String>>()
    var current = mutableListOf<String>()
    var used = 0
    labels.forEach { label ->
        val width = estimatedBadgeWidthPx(label, density)
        val extra = if (current.isEmpty()) width else width + gapPx
        if (current.isNotEmpty() && used + extra > maxWidthPx) {
            rows += current
            current = mutableListOf()
            used = 0
        }
        current += label
        used += if (current.size == 1) width else extra
    }
    if (current.isNotEmpty()) rows += current
    return rows
}

private fun estimatedBadgeWidthPx(label: String, density: Density): Int {
    val textPx = with(density) { AppType.Caption.toPx() * label.length }
    val paddingPx = with(density) { AppDimens.Space4.toPx() }
    val borderPx = with(density) { (2.dp).toPx() }
    return (textPx + paddingPx + borderPx).toInt()
}

internal fun signalExplanation(stock: Stock, signal: String): String {
    val price = QuoteFormat.price(stock.price)
    val support = QuoteFormat.price(stock.support)
    val resistance = QuoteFormat.price(stock.resistance)
    val ma5 = QuoteFormat.price(stock.ma5)
    val ma10 = QuoteFormat.price(stock.ma10)
    val macd = QuoteFormat.price(stock.macd)
    val rsi = QuoteFormat.price(stock.rsi)
    val volumeRatio = QuoteFormat.price(stock.volumeRatio)
    return when {
        signal.contains("MACD金叉") ->
            "MACD 金叉（$macd），短期动能转强，关注成交量是否持续配合。"
        signal.contains("均线多头") ->
            "MA5 $ma5 位于 MA10 $ma10 上方，均线多头排列，趋势偏强。"
        signal == "放量" ->
            "量比 $volumeRatio，成交较近阶段放大，资金关注度提升。"
        signal.contains("接近压力位") ->
            "价格 $price 接近压力位 $resistance，突破需放量确认。"
        signal.contains("RSI超买") ->
            "RSI $rsi 处于超买区，注意回调风险。"
        signal.contains("高位震荡") ->
            "价格靠近压力 $resistance，短期以震荡消化为主，方向未明。"
        signal.contains("MACD死叉") ->
            "MACD 死叉（$macd），空头动能占优，不宜追涨。"
        signal.contains("跌破支撑") ->
            "价格 $price 跌破支撑 $support，短期风险偏大。"
        signal.contains("均线空头") ->
            "MA5 $ma5 位于 MA10 $ma10 下方，均线空头排列，趋势偏弱。"
        else -> stock.aiInterpretation
    }
}

/**
 * 按选中走势点生成解读，价格/量能取该点，支撑压力用当前个股数据，避免与买卖建议矛盾。
 */
internal fun chartPointInterpretation(stock: Stock, index: Int): String {
    val points = stock.chartPoints
    val point = points.getOrNull(index) ?: return stock.aiInterpretation
    val priceText = QuoteFormat.price(point.price)
    val support = QuoteFormat.price(stock.support)
    val resistance = QuoteFormat.price(stock.resistance)
    val prev = points.getOrNull(index - 1)
    val move = if (prev == null) {
        "为首个观察点"
    } else {
        val delta = point.price - prev.price
        if (delta == 0.0) "较前点持平" else "较前点 ${QuoteFormat.change(delta)}"
    }
    val avgVol = points.map { it.volume.toDouble() }.average().let { avg ->
        if (avg == 0.0) 1.0 else avg
    }
    val volRatio = point.volume / avgVol
    val volHint = when {
        volRatio >= 1.2 -> "该日放量"
        volRatio <= 0.8 -> "该日缩量"
        else -> "量能平稳"
    }
    val band = (stock.resistance - stock.support).let { span ->
        if (span == 0.0) stock.price * 0.02 else span
    }
    val loc = when {
        point.price < stock.support -> "跌破支撑 $support"
        point.price > stock.resistance -> "站上压力 $resistance"
        (stock.resistance - point.price) / band <= 0.12 -> "接近压力 $resistance"
        (point.price - stock.support) / band <= 0.12 -> "接近支撑 $support"
        else -> "位于支撑 $support 与压力 $resistance 之间"
    }
    return "${point.time} 价格 $priceText，$move，$volHint，$loc。"
}

