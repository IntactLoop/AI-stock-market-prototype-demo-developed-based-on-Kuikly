package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.widthIn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 买卖建议实心胶囊。视觉规格见设计系统 5.4 节。同一行只出现一个。
 *
 * @param advice `买入` / `观望` / `卖出`
 */
@Composable
fun AIAdviceChip(
    advice: String,
    modifier: Modifier = Modifier
) {
    val colors = adviceColors(advice)
    Box(
        modifier = modifier
            .height(AppDimens.HeightChip)
            .widthIn(min = 56.dp)
            .background(colors.background, RoundedCornerShape(AppDimens.RadiusChip))
            .padding(horizontal = AppDimens.Space3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = advice,
            color = colors.content,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

/**
 * 风险等级幽灵标签。描边胶囊，不用实心，避免与涨跌色抢焦点。
 *
 * @param risk `低风险` / `中风险` / `高风险`
 */
@Composable
fun RiskGhostBadge(
    risk: String,
    modifier: Modifier = Modifier
) {
    val color = riskColor(risk)
    val shape = RoundedCornerShape(AppDimens.RadiusBadge)
    Box(
        modifier = modifier
            .height(AppDimens.HeightBadge)
            .border(AppDimens.StrokeDivider, color, shape)
            .padding(horizontal = AppDimens.Space2),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = risk,
            color = color,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

private data class AdviceColors(
    val background: Color,
    val content: Color
)

private fun adviceColors(advice: String): AdviceColors = when (advice) {
    Stock.ADVICE_BUY -> AdviceColors(AppColors.Rise, AppColors.TextOnAccent)
    Stock.ADVICE_SELL -> AdviceColors(AppColors.Fall, AppColors.TextOnAccent)
    else -> AdviceColors(AppColors.Warning, AppColors.TextOnWarning)
}

private fun riskColor(risk: String): Color = when (risk) {
    Stock.RISK_LOW -> AppColors.Success
    Stock.RISK_HIGH -> AppColors.Danger
    else -> AppColors.Warning
}
