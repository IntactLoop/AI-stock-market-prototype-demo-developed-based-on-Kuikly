package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow

/**
 * 对比页单列指标。展示价格、涨跌、市值、营收、PE、PB、均线，不改 [StockCard]。
 *
 * @param stock 被对比的股票
 */
@Composable
fun CompareColumn(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    val changeColor = QuoteFormat.changeColor(stock.changePercent)
    Column(
        modifier = modifier
            .background(AppColors.BgCard)
            .padding(horizontal = AppDimens.Space2, vertical = AppDimens.Space3),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space2)
    ) {
        Text(
            text = stock.name,
            color = AppColors.TextTitle,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stock.symbol,
            color = AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal,
            maxLines = 1
        )
        Metric("最新价", QuoteFormat.price(stock.price), changeColor)
        Metric("涨跌幅", QuoteFormat.percent(stock.changePercent), changeColor)
        Metric("市值", "${QuoteFormat.price(stock.marketCap)}亿")
        Metric("营收", QuoteFormat.revenue(stock.revenue))
        Metric("PE", QuoteFormat.price(stock.pe))
        Metric("PB", QuoteFormat.price(stock.pb))
        Metric("MA5", QuoteFormat.price(stock.ma5))
        Metric("MA10", QuoteFormat.price(stock.ma10))
        Metric("MA20", QuoteFormat.price(stock.ma20))
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
    valueColor: Color = AppColors.TextTitle
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)) {
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
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
