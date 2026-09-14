package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.BriefingItem
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
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
 * 盘前早报正文。每只股票一条结构化行：名称/价格 + 信号标签/辅助说明。
 * 作为 [AIBriefingSheet] 内容；不再用圆点长文本，也不包 [AICardFrame]。
 *
 * @param items 已按股票合并的早报条目，无自选时传空列表
 * @param emptyMessage 无自选时的提示
 */
@Composable
fun AIBriefingCard(
    items: List<BriefingItem>,
    modifier: Modifier = Modifier,
    emptyMessage: String = "暂无自选股早报，请先添加自选"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (items.isEmpty()) {
            Text(
                text = emptyMessage,
                color = AppColors.TextHint,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal,
                lineHeight = AppType.CalloutLine,
                modifier = Modifier.padding(vertical = AppDimens.Space4)
            )
        } else {
            items.forEachIndexed { index, item ->
                BriefingItemRow(item = item)
                if (index != items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppDimens.StrokeDivider)
                            .background(AppColors.Divider)
                    )
                }
            }
        }
    }
}

@Composable
private fun BriefingItemRow(item: BriefingItem) {
    val changeColor = QuoteFormat.changeColor(item.changePercent)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.Space2),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                color = AppColors.TextTitle,
                fontSize = AppType.Body,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = AppDimens.Space3)
            )
            Text(
                text = QuoteFormat.price(item.price),
                color = changeColor,
                fontSize = AppType.Body,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space1),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item.tags.filter { it.isNotBlank() }.distinct().take(3).forEach { tag ->
                SignalBadge(
                    label = tag.take(6),
                    selected = false,
                    onClick = null
                )
            }
            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Normal,
                    lineHeight = AppType.CaptionLine,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 从自选股收集早报。同一只股票的多个要点合并为一条；无自选返回空列表。
 */
fun collectBriefingItems(
    watchlistStocks: List<Stock>,
    maxItems: Int = 5
): List<BriefingItem> {
    return watchlistStocks.map { stock ->
        mergeBriefingItem(stock)
    }.take(maxItems)
}

private fun mergeBriefingItem(stock: Stock): BriefingItem {
    val items = stock.briefingItems
    if (items.isEmpty()) {
        return BriefingItem(
            symbol = stock.symbol,
            name = stock.name,
            price = stock.price,
            changePercent = stock.changePercent,
            tags = listOfNotNull(
                stock.aiSignal.takeIf { it.isNotBlank() },
                "建议${stock.aiAdvice}"
            ),
            note = stock.briefingBullets.getOrNull(1).orEmpty()
        )
    }
    return BriefingItem(
        symbol = stock.symbol,
        name = stock.name,
        price = stock.price,
        changePercent = stock.changePercent,
        tags = items.flatMap { it.tags }.filter { it.isNotBlank() }.distinct(),
        note = items.map { it.note }.filter { it.isNotBlank() }.distinct().joinToString("；")
    )
}
