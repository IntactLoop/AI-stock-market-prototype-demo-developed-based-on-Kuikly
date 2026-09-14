package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 行情列表列头。左右 16dp 对齐列表；再内缩 16dp 对齐 [StockCard] 内容。
 * [startGutter] / [endGutter] 用于对比勾选或自选星标占用的额外宽度。
 */
@Composable
fun ListHeader(
    modifier: Modifier = Modifier,
    startGutter: Dp = 0.dp,
    endGutter: Dp = 0.dp,
    priceLabel: String = "最新价",
    changeLabel: String = "涨跌幅"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .padding(
                start = AppDimens.Space4 + startGutter + AppDimens.Space4,
                end = AppDimens.Space4 + endGutter + AppDimens.Space4
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "股票",
            color = AppColors.TextHint,
            fontSize = AppType.Micro,
            fontWeight = FontWeight.Normal
        )
        Row(
            modifier = Modifier.width(AppDimens.PriceColumnWidth),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = priceLabel,
                color = AppColors.TextHint,
                fontSize = AppType.Micro,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = changeLabel,
                color = AppColors.TextHint,
                fontSize = AppType.Micro,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End
            )
        }
    }
}
