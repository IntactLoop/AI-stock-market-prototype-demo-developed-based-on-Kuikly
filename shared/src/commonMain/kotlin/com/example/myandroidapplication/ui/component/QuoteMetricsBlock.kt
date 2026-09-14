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
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 详情页高低 / 成交量 / 开盘 / 营收市值。视觉规格见设计系统 5.7 节，最高最低不用涨跌色。
 * 营收、市值入参已是亿元，展示走 [QuoteFormat.revenue] 或同格式加「亿」。
 *
 * @param stock 当前个股
 */
@Composable
fun QuoteMetricsBlock(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = AppDimens.Space4,
                end = AppDimens.Space4,
                top = AppDimens.Space3
            ),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space2)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell("最高", QuoteFormat.price(stock.high), Modifier.weight(1f))
            MetricCell("最低", QuoteFormat.price(stock.low), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell("成交量", QuoteFormat.volume(stock.volume), Modifier.weight(1f))
            MetricCell("开盘", QuoteFormat.price(stock.open), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell("营收", QuoteFormat.revenue(stock.revenue), Modifier.weight(1f))
            MetricCell("市值", "${QuoteFormat.price(stock.marketCap)}亿", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String, modifier: Modifier = Modifier) {
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
            color = AppColors.TextTitle,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
