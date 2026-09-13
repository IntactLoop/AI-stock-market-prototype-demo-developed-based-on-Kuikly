package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsPressedAsState
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 首页市场切换：沪深 / 港股 / 美股。视觉用主色下划线表示选中，不改 [StockCard]。
 *
 * @param selectedMarket 当前市场，取值 [Stock.MARKET_CN] / [Stock.MARKET_HK] / [Stock.MARKET_US]
 * @param onMarketSelected 切换回调；列表由页面按市场重新取数
 */
@Composable
fun MarketTab(
    selectedMarket: String,
    onMarketSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.BgElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.MinTouch)
        ) {
            MARKETS.forEach { market ->
                val selected = market == selectedMarket
                val interactionSource = remember(market) { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            when {
                                pressed -> AppColors.BgPress
                                else -> AppColors.BgElevated
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onMarketSelected(market) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = market,
                        color = if (selected) AppColors.Primary else AppColors.TextHint,
                        fontSize = AppType.Callout,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(AppDimens.StrokeAccent)
                                .background(AppColors.Primary)
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
    }
}

private val MARKETS = listOf(Stock.MARKET_CN, Stock.MARKET_HK, Stock.MARKET_US)
