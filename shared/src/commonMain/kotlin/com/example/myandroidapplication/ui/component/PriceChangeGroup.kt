package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow

/**
 * 详情页展示型涨跌数字组。规格见设计系统 5.7 节。列表紧凑型已内嵌于 [StockCard]。
 */
@Composable
fun PriceChangeGroup(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    val changeColor = QuoteFormat.changeColor(stock.changePercent)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.HeightListItem)
            .padding(horizontal = AppDimens.Space4)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stock.name,
                color = AppColors.TextTitle,
                fontSize = AppType.Headline,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stock.symbol,
                color = AppColors.TextHint,
                fontSize = AppType.Caption,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = AppDimens.Space2)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = QuoteFormat.price(stock.price),
                color = changeColor,
                fontSize = AppType.Display,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                lineHeight = AppType.DisplayLine,
                maxLines = 1
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)) {
                Text(
                    text = QuoteFormat.change(stock.change),
                    color = changeColor,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = QuoteFormat.percent(stock.changePercent),
                    color = changeColor,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
