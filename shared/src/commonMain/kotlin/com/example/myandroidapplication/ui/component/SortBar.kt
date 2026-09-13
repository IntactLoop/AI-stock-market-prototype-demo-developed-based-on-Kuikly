package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsPressedAsState
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow

/**
 * 首页列表排序：涨跌幅 / 人气 / 换手率，右侧为筛选与对比入口。只在内存排序，不改 Repository 签名。
 *
 * @param selected 当前排序项
 * @param ascending 当前是否升序；同项再点切换方向
 * @param onSelected 切换排序项或方向后的回调
 * @param filterActive 是否已启用筛选，用于高亮「筛选」
 * @param onFilterClick 打开筛选面板
 * @param compareMode 是否处于对比勾选态，用于高亮最右侧「对比 / 取消对比」
 * @param onCompareToggle 进入或退出对比模式
 */
@Composable
fun SortBar(
    selected: QuoteSort,
    ascending: Boolean,
    onSelected: (QuoteSort) -> Unit,
    filterActive: Boolean,
    onFilterClick: () -> Unit,
    compareMode: Boolean,
    onCompareToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arrow = if (ascending) "↑" else "↓"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.MinTouch)
            .padding(horizontal = AppDimens.Space4),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuoteSort.entries.forEach { option ->
            val selectedOption = option == selected
            SortChip(
                label = option.label,
                selected = selectedOption,
                onClick = { onSelected(option) },
                modifier = Modifier.weight(1f),
                arrow = arrow
            )
        }
        SortChip(
            label = "筛选",
            selected = filterActive,
            onClick = onFilterClick,
            modifier = Modifier.weight(1f)
        )
        SortChip(
            label = if (compareMode) "取消对比" else "对比",
            selected = compareMode,
            onClick = onCompareToggle,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 当前市场列表的排序方式。默认按涨跌幅从高到低。
 */
enum class QuoteSort(val label: String) {
    CHANGE_PERCENT("涨跌幅"),
    POPULARITY("人气"),
    TURNOVER_RATE("换手率")
}

/**
 * 对当前市场股票做内存排序，不调用 Repository 新方法。
 * [ascending] 为 false 时从高到低（默认），为 true 时从低到高。
 */
fun List<Stock>.sortedByQuote(sort: QuoteSort, ascending: Boolean = false): List<Stock> {
    val ordered = when (sort) {
        QuoteSort.CHANGE_PERCENT ->
            if (ascending) sortedBy { it.changePercent } else sortedByDescending { it.changePercent }
        QuoteSort.POPULARITY ->
            if (ascending) sortedBy { it.popularity } else sortedByDescending { it.popularity }
        QuoteSort.TURNOVER_RATE ->
            if (ascending) sortedBy { it.turnoverRate } else sortedByDescending { it.turnoverRate }
    }
    return ordered
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    arrow: String? = null
) {
    val interactionSource = remember(label) { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(AppDimens.RadiusBadge)
    val background = when {
        selected -> AppColors.PrimaryDim
        pressed -> AppColors.BgPress
        else -> AppColors.BgCard
    }
    val borderColor = if (selected) AppColors.Primary else AppColors.Border
    val textColor = if (selected) AppColors.Primary else AppColors.TextHint
    val arrowColor = if (selected) AppColors.Primary else AppColors.TextHint
    Box(
        modifier = modifier
            .height(AppDimens.HeightChip)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(background, shape)
            .border(AppDimens.StrokeDivider, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = AppType.Caption,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (arrow != null) {
                Text(
                    text = arrow,
                    color = arrowColor,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
