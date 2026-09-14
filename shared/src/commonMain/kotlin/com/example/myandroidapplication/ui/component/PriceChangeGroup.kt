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
import com.tencent.kuikly.compose.foundation.layout.Spacer
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
 * 「最新价」标签在名称行下方、大数字上方，间距 4dp。
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
        Text(
            text = "最新价",
            color = AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = AppDimens.Space1)
        )
        Spacer(modifier = Modifier.height(AppDimens.Space1))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2),
                verticalAlignment = Alignment.Bottom
            ) {
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
