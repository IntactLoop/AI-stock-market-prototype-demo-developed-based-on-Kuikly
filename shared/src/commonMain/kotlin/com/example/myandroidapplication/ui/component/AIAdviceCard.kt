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
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 买卖建议卡片。视觉规格见设计系统 5.4 节：芯片 + 风险 + 点位 + 查看理由。
 *
 * @param stock 当前个股，建议/风险/点位必须与其数据一致
 */
@Composable
fun AIAdviceCard(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    var reasonExpanded by remember(stock.symbol) { mutableStateOf(false) }
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.BgCardAI)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
            .padding(AppDimens.Space4)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "买卖建议",
                color = AppColors.TextSecondary,
                fontSize = AppType.Body,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .heightIn(min = AppDimens.MinTouch)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { reasonExpanded = !reasonExpanded }
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "查看理由",
                    color = AppColors.Primary,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(AppDimens.Space3))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)
        ) {
            AIAdviceChip(advice = stock.aiAdvice)
            RiskGhostBadge(risk = stock.riskLevel)
        }
        if (stock.adviceReasons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(AppDimens.Space3))
            AdviceSupportPoints(reasons = stock.adviceReasons)
        }
        Spacer(modifier = Modifier.height(AppDimens.Space3))
        PointRow(label = "支撑买入价", value = QuoteFormat.price(stock.buyPoint))
        Spacer(modifier = Modifier.height(AppDimens.Space1))
        PointRow(label = "压力卖出价", value = QuoteFormat.price(stock.sellPoint))
        if (reasonExpanded) {
            Spacer(modifier = Modifier.height(AppDimens.Space2))
            Text(
                text = stock.adviceReason,
                color = AppColors.TextBody,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal,
                lineHeight = AppType.CalloutLine,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 买卖建议支撑点。每行小圆点 + 12sp 正文，行距 4dp。
 *
 * @param reasons 2–3 条与当前股票数据一致的文案
 */
@Composable
fun AdviceSupportPoints(
    reasons: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)
    ) {
        reasons.take(3).forEach { reason ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(6.dp)
                        .background(AppColors.AI, RoundedCornerShape(3.dp))
                )
                Text(
                    text = reason,
                    color = AppColors.TextBody,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Normal,
                    lineHeight = AppType.CaptionLine
                )
            }
        }
    }
}

@Composable
private fun PointRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label ",
            color = AppColors.TextHint,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = AppColors.TextTitle,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
