package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsPressedAsState
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.widthIn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow

/**
 * 首页行情列表项。视觉规格见设计系统 5.1 节。
 *
 * @param stock 列表项数据
 * @param onClick 点击进入详情
 */
@Composable
fun StockCard(
    stock: Stock,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val changeColor = QuoteFormat.changeColor(stock.changePercent)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.HeightListItem)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(if (pressed) AppColors.BgPress else AppColors.BgCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimens.Space4, vertical = AppDimens.Space3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = AppDimens.Space3),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)
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
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier.width(AppDimens.PriceColumnWidth),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)
            ) {
                Text(
                    text = QuoteFormat.price(stock.price),
                    color = changeColor,
                    fontSize = AppType.Headline,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .widthIn(min = AppDimens.PercentChipMinWidth)
                        .height(AppDimens.PercentChipHeight)
                        .background(
                            QuoteFormat.percentChipBackground(stock.changePercent),
                            RoundedCornerShape(AppDimens.RadiusBadge)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = QuoteFormat.percent(stock.changePercent),
                        color = changeColor,
                        fontSize = AppType.Caption,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = AppDimens.Space4)
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
    }
}
