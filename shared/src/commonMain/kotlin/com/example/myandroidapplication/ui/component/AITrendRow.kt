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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 趋势判断标签行。视觉规格见设计系统 6.3 第 7 项，徽章复用 5.2 [SignalBadge]。
 *
 * @param stock 当前个股，文案取 [Stock.shortTrend] / [Stock.midTrend]
 */
@Composable
fun AITrendRow(
    stock: Stock,
    modifier: Modifier = Modifier
) {
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
            text = "趋势判断",
            color = AppColors.TextSecondary,
            fontSize = AppType.Body,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppDimens.Space3))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)
        ) {
            SignalBadge(
                label = stock.shortTrend,
                selected = false,
                onClick = null
            )
            SignalBadge(
                label = stock.midTrend,
                selected = false,
                onClick = null
            )
        }
    }
}
