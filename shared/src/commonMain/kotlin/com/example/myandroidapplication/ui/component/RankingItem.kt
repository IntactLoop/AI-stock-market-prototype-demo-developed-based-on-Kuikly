package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.Ranking
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
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow

/**
 * 榜单行：文字序号 + 股票名称/代码 + 持仓变动。禁止 `Icon`。
 *
 * @param ranking 排行数据，来自 Repository
 * @param onClick 点击进入个股详情
 */
@Composable
fun RankingItem(
    ranking: Ranking,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val changeColor = QuoteFormat.changeColor(ranking.holdingChange)
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ranking.rank.toString(),
                color = AppColors.TextTitle,
                fontSize = AppType.Headline,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(AppDimens.Space8)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppDimens.Space3),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)
            ) {
                Text(
                    text = ranking.name,
                    color = AppColors.TextTitle,
                    fontSize = AppType.Headline,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ranking.symbol,
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
                    text = QuoteFormat.percent(ranking.holdingChange),
                    color = changeColor,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                Text(
                    text = "${QuoteFormat.price(ranking.holdingRatio)}%",
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
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
