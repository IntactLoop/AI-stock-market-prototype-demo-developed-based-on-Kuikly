package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.MarketIndex
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 首页指数卡。横滑展示点位与涨跌幅；点击不进详情。
 *
 * @param index 当前市场的一只指数
 */
@Composable
fun IndexCard(
    index: MarketIndex,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    val changeColor = QuoteFormat.changeColor(index.changePercent)
    Column(
        modifier = modifier
            .width(IndexCardWidth)
            .height(HeightOverviewCard)
            .clip(shape)
            .background(AppColors.BgCard)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
            .padding(AppDimens.Space3),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = index.name,
            color = AppColors.TextSecondary,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = QuoteFormat.price(index.price),
            color = changeColor,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
        Text(
            text = QuoteFormat.percent(index.changePercent),
            color = changeColor,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

private val IndexCardWidth = 128.dp
/** 首页指数卡与板块卡共用高度，对齐 [AppDimens] 的 Height* 命名。 */
internal val HeightOverviewCard = 80.dp
