package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.model.StockFilter
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
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.onSizeChanged
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 首页自定义筛选面板。外壳走 [QuoteBottomSheet]；
 * 首次合成可能误触发 dismiss，由外壳忽略首次回调。
 *
 * @param applied 当前已生效条件，用于回填
 * @param tagOptions 当前市场可选标签，由页面从 Repository 列表提取，不在此过滤股票
 * @param sheetHeight 与问答 Sheet 相同的高度策略
 * @param onConfirm 确定后把 [StockFilter] 交给页面，再调用 `filterStocks`
 * @param onDismissRequest 关闭
 */
@Composable
fun FilterPanel(
    applied: StockFilter,
    tagOptions: List<String>,
    sheetHeight: Dp,
    onConfirm: (StockFilter) -> Unit,
    onDismissRequest: () -> Unit
) {
    QuoteBottomSheet(
        sheetHeight = sheetHeight,
        onDismissRequest = onDismissRequest
    ) {
        FilterPanelBody(
            applied = applied,
            tagOptions = tagOptions,
            onConfirm = onConfirm,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun FilterPanelBody(
    applied: StockFilter,
    tagOptions: List<String>,
    onConfirm: (StockFilter) -> Unit,
    onDismissRequest: () -> Unit
) {
    var priceMin by remember(applied) { mutableStateOf(applied.minPrice) }
    var priceMax by remember(applied) { mutableStateOf(applied.maxPrice) }
    var changeMin by remember(applied) { mutableStateOf(applied.minChangePercent) }
    var changeMax by remember(applied) { mutableStateOf(applied.maxChangePercent) }
    var capMin by remember(applied) { mutableStateOf(applied.minMarketCap) }
    var capMax by remember(applied) { mutableStateOf(applied.maxMarketCap) }
    var tags by remember(applied) { mutableStateOf(applied.tags) }
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        SheetDragHandle()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.MinTouch)
                .padding(horizontal = AppDimens.Space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "筛选",
                color = AppColors.TextSecondary,
                fontSize = AppType.Body,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "关闭",
                color = AppColors.Primary,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .height(AppDimens.MinTouch)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = AppDimens.Space4,
                vertical = AppDimens.Space3
            ),
            beyondBoundsItemCount = 3
        ) {
            item {
                FilterSectionTitle("价格")
                RangeChipRow(
                    options = PRICE_OPTIONS,
                    selectedMin = priceMin,
                    selectedMax = priceMax,
                    onSelected = { min, max ->
                        priceMin = min
                        priceMax = max
                    }
                )
            }
            item {
                FilterSectionTitle("涨跌幅")
                RangeChipRow(
                    options = CHANGE_OPTIONS,
                    selectedMin = changeMin,
                    selectedMax = changeMax,
                    onSelected = { min, max ->
                        changeMin = min
                        changeMax = max
                    }
                )
            }
            item {
                FilterSectionTitle("市值（亿元）")
                RangeChipRow(
                    options = CAP_OPTIONS,
                    selectedMin = capMin,
                    selectedMax = capMax,
                    onSelected = { min, max ->
                        capMin = min
                        capMax = max
                    }
                )
            }
            item {
                FilterSectionTitle("标签")
                FilterTagFlow(
                    tagOptions = tagOptions,
                    selectedTags = tags,
                    onToggle = { tag ->
                        tags = if (tag in tags) tags - tag else tags + tag
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(AppDimens.Space4))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Space4),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space3)
        ) {
            FilterActionButton(
                text = "清空",
                filled = false,
                modifier = Modifier.weight(1f),
                onClick = {
                    onConfirm(StockFilter.EMPTY)
                    onDismissRequest()
                }
            )
            FilterActionButton(
                text = "确定",
                filled = true,
                modifier = Modifier.weight(1f),
                onClick = {
                    onConfirm(
                        StockFilter(
                            minPrice = priceMin,
                            maxPrice = priceMax,
                            minChangePercent = changeMin,
                            maxChangePercent = changeMax,
                            minMarketCap = capMin,
                            maxMarketCap = capMax,
                            tags = tags
                        )
                    )
                    onDismissRequest()
                }
            )
        }
    }
}

/**
 * 标签左对齐流式换行：按容器宽度决定每行个数，行末不拉伸。
 * 徽章间距与行距均为 [AppDimens.Space2]（8dp），与 SignalBadge 规范一致。
 */
@Composable
private fun FilterTagFlow(
    tagOptions: List<String>,
    selectedTags: Set<String>,
    onToggle: (String) -> Unit
) {
    var rowWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val rows = remember(tagOptions, rowWidthPx, density) {
        wrapTagRows(tagOptions, rowWidthPx, density)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { rowWidthPx = it.width },
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space2)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)) {
                row.forEach { tag ->
                    val selected = tag in selectedTags
                    TagChip(
                        text = tag,
                        selected = selected,
                        onClick = { onToggle(tag) }
                    )
                }
            }
        }
    }
}

private fun wrapTagRows(
    labels: List<String>,
    maxWidthPx: Int,
    density: Density
): List<List<String>> {
    if (labels.isEmpty()) return emptyList()
    if (maxWidthPx <= 0) return listOf(labels)
    val gapPx = with(density) { AppDimens.Space2.roundToPx() }
    val rows = mutableListOf<MutableList<String>>()
    var current = mutableListOf<String>()
    var used = 0
    labels.forEach { label ->
        val width = estimatedTagWidthPx(label, density)
        val extra = if (current.isEmpty()) width else width + gapPx
        if (current.isNotEmpty() && used + extra > maxWidthPx) {
            rows += current
            current = mutableListOf()
            used = 0
        }
        current += label
        used += if (current.size == 1) width else extra
    }
    if (current.isNotEmpty()) rows += current
    return rows
}

private fun estimatedTagWidthPx(label: String, density: Density): Int {
    val textPx = with(density) { AppType.Caption.toPx() * label.length }
    val paddingPx = with(density) { AppDimens.Space4.toPx() }
    val borderPx = with(density) { (2.dp).toPx() }
    return (textPx + paddingPx + borderPx).toInt()
}

@Composable
private fun FilterSectionTitle(text: String) {
    Text(
        text = text,
        color = AppColors.TextSecondary,
        fontSize = AppType.Callout,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = AppDimens.Space3, bottom = AppDimens.Space2)
    )
}

@Composable
private fun RangeChipRow(
    options: List<RangeOption>,
    selectedMin: Double?,
    selectedMax: Double?,
    onSelected: (Double?, Double?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)
    ) {
        options.forEach { option ->
            val selected = option.min == selectedMin && option.max == selectedMax
            FilterChoiceChip(
                label = option.label,
                selected = selected,
                onClick = { onSelected(option.min, option.max) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FilterChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember(label) { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(AppDimens.RadiusBadge)
    Box(
        modifier = modifier
            .height(AppDimens.HeightChip)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(
                when {
                    selected -> AppColors.PrimaryDim
                    pressed -> AppColors.BgPress
                    else -> AppColors.BgCard
                },
                shape
            )
            .border(
                AppDimens.StrokeDivider,
                if (selected) AppColors.Primary else AppColors.Border,
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) AppColors.Primary else AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun FilterActionButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    Box(
        modifier = modifier
            .height(AppDimens.HeightButton)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .background(if (filled) AppColors.Primary else AppColors.BgCard, shape)
            .border(AppDimens.StrokeDivider, if (filled) AppColors.Primary else AppColors.Border, shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (filled) AppColors.TextOnAccent else AppColors.TextSecondary,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class RangeOption(val label: String, val min: Double?, val max: Double?)

private val PRICE_OPTIONS = listOf(
    RangeOption("不限", null, null),
    RangeOption("20以下", null, 20.0),
    RangeOption("20–100", 20.0, 100.0),
    RangeOption("100以上", 100.0, null)
)

private val CHANGE_OPTIONS = listOf(
    RangeOption("不限", null, null),
    RangeOption("上涨", 0.0, null),
    RangeOption("下跌", null, 0.0)
)

private val CAP_OPTIONS = listOf(
    RangeOption("不限", null, null),
    RangeOption("千亿内", null, 1000.0),
    RangeOption("千亿+", 1000.0, null)
)
