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
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsPressedAsState
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 行情总结卡片。视觉规格见设计系统 6.3 第 8 项；正文不包 SelectionContainer。
 * 内部「盘中 / 复盘」Tab；对外仍只收 [stock] 与 [modifier]。
 *
 * @param stock 当前个股，盘中取 [Stock.aiSummary]，复盘取 [Stock.reviewSummary]
 */
@Composable
fun AISummaryCard(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    var reviewTab by remember(stock.symbol) { mutableStateOf(false) }
    val body = if (reviewTab) stock.reviewSummary else stock.aiSummary
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.BgCardAI)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
            .padding(AppDimens.Space4)
    ) {
        Text(
            text = "行情总结",
            color = AppColors.TextSecondary,
            fontSize = AppType.Body,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        SummaryModeTab(
            reviewSelected = reviewTab,
            onSelectReview = { reviewTab = it }
        )
        Spacer(modifier = Modifier.height(AppDimens.Space3))
        Text(
            text = body,
            color = AppColors.TextBody,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Normal,
            lineHeight = AppType.CalloutLine
        )
        Spacer(modifier = Modifier.height(AppDimens.Space3))
        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryMetric(
                label = "涨跌幅",
                value = QuoteFormat.percent(stock.changePercent),
                valueColor = QuoteFormat.changeColor(stock.changePercent),
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = "成交量",
                value = QuoteFormat.volume(stock.volume),
                valueColor = AppColors.TextTitle,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryMetric(
                label = "收盘",
                value = QuoteFormat.price(stock.close),
                valueColor = AppColors.TextTitle,
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = "趋势",
                value = stock.shortTrend,
                valueColor = AppColors.TextTitle,
                monospaced = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryModeTab(
    reviewSelected: Boolean,
    onSelectReview: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.HeightChip)
    ) {
        SummaryModeTabItem(
            label = "盘中",
            selected = !reviewSelected,
            onClick = { onSelectReview(false) },
            modifier = Modifier.weight(1f)
        )
        SummaryModeTabItem(
            label = "复盘",
            selected = reviewSelected,
            onClick = { onSelectReview(true) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryModeTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (pressed) AppColors.BgPress else AppColors.BgCardAI)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) AppColors.Primary else AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(AppDimens.StrokeAccent)
                    .background(AppColors.AI)
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    monospaced: Boolean = true
) {
    Row(
        modifier = modifier.height(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space3)
    ) {
        Text(
            text = label,
            color = AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium,
            fontFamily = if (monospaced) FontFamily.Monospace else FontFamily.Default,
            maxLines = 1
        )
    }
}
