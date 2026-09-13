package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.LinearProgressIndicator
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
 * 趋势判断。短期 / 中期 / 长期三行置信度，下方解读取 [Stock.trendNarrative]。
 * 对外仍只接收 [stock]，不改调用方。
 *
 * @param stock 当前个股
 */
@Composable
fun AITrendRow(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    val longTrend = longTrendLabel(stock)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.BgCardAI)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
            .padding(AppDimens.Space4)
    ) {
        Text(
            text = "趋势判断",
            color = AppColors.TextSecondary,
            fontSize = AppType.Body,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppDimens.Space3))
        TrendConfidenceRow(
            label = "短期",
            trend = stock.shortTrend,
            confidence = stock.trendConfidenceShort
        )
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        TrendConfidenceRow(
            label = "中期",
            trend = stock.midTrend,
            confidence = stock.trendConfidenceMid
        )
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        TrendConfidenceRow(
            label = "长期",
            trend = longTrend,
            confidence = stock.trendConfidenceLong
        )
        if (stock.trendNarrative.isNotBlank()) {
            Spacer(modifier = Modifier.height(AppDimens.Space3))
            Text(
                text = stock.trendNarrative,
                color = AppColors.TextBody,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal,
                lineHeight = AppType.CalloutLine,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrendConfidenceRow(
    label: String,
    trend: String,
    confidence: Int
) {
    val arrow = trendArrow(trend)
    val arrowColor = trendArrowColor(trend)
    val clamped = confidence.coerceIn(0, 100)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)
    ) {
        Text(
            text = label,
            color = AppColors.TextSecondary,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(36.dp)
        )
        Text(
            text = arrow,
            color = arrowColor,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(16.dp)
        )
        LinearProgressIndicator(
            progress = { clamped / 100f },
            modifier = Modifier
                .weight(1f)
                .height(AppDimens.HeightProgress)
                .clip(RoundedCornerShape(2.dp)),
            color = AppColors.Primary,
            trackColor = AppColors.Divider
        )
        Text(
            text = "$clamped%",
            color = AppColors.AI,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp)
        )
    }
}

private fun longTrendLabel(stock: Stock): String = when {
    stock.midTrend.contains("偏多") -> "长期偏多"
    stock.midTrend.contains("偏空") -> "长期偏空"
    else -> "长期震荡"
}

private fun trendArrow(trend: String): String = when {
    trend.contains("偏多") -> "↑"
    trend.contains("偏空") -> "↓"
    else -> "→"
}

private fun trendArrowColor(trend: String): Color = when {
    trend.contains("偏多") -> AppColors.Rise
    trend.contains("偏空") -> AppColors.Fall
    else -> AppColors.Flat
}
