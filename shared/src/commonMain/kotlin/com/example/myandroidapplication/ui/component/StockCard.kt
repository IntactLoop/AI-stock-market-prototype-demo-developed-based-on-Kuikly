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
import com.tencent.kuikly.compose.foundation.layout.Spacer
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
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * 首页行情列表项。视觉规格见设计系统 5.1 节。
 * 行 3 追加成交额与换手率；高度仍为 [AppDimens.HeightListItem]。
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimens.Space4, vertical = AppDimens.Space1),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.name,
                    color = AppColors.TextTitle,
                    fontSize = AppType.Headline,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = AppDimens.Space3)
                )
                Text(
                    text = QuoteFormat.price(stock.price),
                    color = changeColor,
                    fontSize = AppType.Headline,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    lineHeight = 20.sp,
                    modifier = Modifier.width(AppDimens.PriceColumnWidth),
                    maxLines = 1
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimens.Space1),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.symbol,
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = AppDimens.Space3)
                )
                Box(
                    modifier = Modifier.width(AppDimens.PriceColumnWidth),
                    contentAlignment = Alignment.CenterEnd
                ) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimens.Space1),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "成交 ${QuoteFormat.turnoverAmount(stock.turnover)} · 换手 ${QuoteFormat.turnoverRate(stock.turnoverRate)}",
                    color = AppColors.TextHint,
                    fontSize = AppType.Micro,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = AppDimens.Space3)
                )
                Spacer(modifier = Modifier.width(AppDimens.PriceColumnWidth))
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
