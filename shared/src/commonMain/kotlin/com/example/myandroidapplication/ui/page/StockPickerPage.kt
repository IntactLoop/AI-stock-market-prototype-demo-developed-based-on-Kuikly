package com.example.myandroidapplication.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.ETF
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.model.StockPickCategory
import com.example.myandroidapplication.ui.AppPages
import com.example.myandroidapplication.ui.component.NavBackButton
import com.example.myandroidapplication.ui.component.PickerTab
import com.example.myandroidapplication.ui.component.ListHeader
import com.example.myandroidapplication.ui.component.QuoteTopBar
import com.example.myandroidapplication.ui.component.SearchBar
import com.example.myandroidapplication.ui.component.StockCard
import com.example.myandroidapplication.ui.component.TagChip
import com.example.myandroidapplication.ui.component.WatchStarButton
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.widthIn
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 选股分类页。四个 Tab 的列表来自 Repository，Tab 内筛选为分类芯片，不复用首页 [com.example.myandroidapplication.ui.component.FilterPanel]。
 * 搜索为本地过滤；自选星标叠在 [StockCard] 右侧，不改卡片本身。
 */
@Page(AppPages.STOCK_PICKER)
internal class StockPickerPage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        val statusBarDp = pagerData.statusBarHeight.dp
        setContent {
            StockPickerScreen(
                statusBarHeight = statusBarDp,
                onBack = {
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
                },
                onStockClick = { symbol ->
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(
                        AppPages.STOCK_DETAIL,
                        JSONObject().put(AppPages.ARG_SYMBOL, symbol)
                    )
                }
            )
        }
    }
}

@Composable
private fun StockPickerScreen(
    statusBarHeight: Dp,
    onBack: () -> Unit,
    onStockClick: (String) -> Unit
) {
    val repository = AppContainer.stockRepository
    val watchlistRepository = AppContainer.watchlistRepository
    val watchedSymbols = remember {
        mutableStateListOf<String>().also { it.addAll(watchlistRepository.getAll()) }
    }
    var category by remember { mutableStateOf(StockPickCategory.COMPANY) }
    var chip by remember { mutableStateOf(FILTER_ALL) }
    var query by remember { mutableStateOf("") }
    val picks = remember(category) { repository.getStockPicks(category) }
    val etfs = remember { repository.getETFs() }
    val chipOptions = remember(category, picks, etfs) {
        when (category) {
            StockPickCategory.ETF ->
                listOf(FILTER_ALL) + listOf(
                    ETF.CATEGORY_BROAD,
                    ETF.CATEGORY_SECTOR,
                    ETF.CATEGORY_BOND
                )
            StockPickCategory.COMPANY ->
                listOf(FILTER_ALL) + picks.map { it.sector }.distinct()
            else ->
                listOf(FILTER_ALL) + picks.flatMap { it.tags }.distinct()
        }
    }
    val visibleStocks = remember(category, chip, picks) {
        if (category == StockPickCategory.ETF) {
            emptyList()
        } else if (chip == FILTER_ALL) {
            picks
        } else if (category == StockPickCategory.COMPANY) {
            picks.filter { it.sector == chip }
        } else {
            picks.filter { it.tags.contains(chip) }
        }
    }
    val visibleEtfs = remember(category, chip, etfs) {
        if (category != StockPickCategory.ETF) {
            emptyList()
        } else if (chip == FILTER_ALL) {
            etfs
        } else {
            etfs.filter { it.category == chip }
        }
    }
    val keyword = query.trim()
    val searchedStocks = remember(visibleStocks, keyword) {
        filterByKeyword(visibleStocks, keyword) { stock -> stock.name to stock.symbol }
    }
    val searchedEtfs = remember(visibleEtfs, keyword) {
        filterByKeyword(visibleEtfs, keyword) { etf -> etf.name to etf.symbol }
    }
    val empty = if (category == StockPickCategory.ETF) {
        searchedEtfs.isEmpty()
    } else {
        searchedStocks.isEmpty()
    }
    val emptyText = if (keyword.isNotEmpty()) {
        "未找到匹配的股票"
    } else {
        "没有符合条件的股票"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgPage)
    ) {
        QuoteTopBar(
            title = "选股",
            statusBarHeight = statusBarHeight,
            titleCentered = true,
            navigation = { NavBackButton(onBack) }
        )
        SearchBar(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.padding(
                start = AppDimens.Space4,
                end = AppDimens.Space4,
                top = AppDimens.Space2,
                bottom = AppDimens.Space2
            )
        )
        PickerTab(
            tabs = StockPickCategory.ALL,
            selected = category,
            onSelected = { next ->
                category = next
                chip = FILTER_ALL
            }
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = AppDimens.Space4,
                vertical = AppDimens.Space2
            ),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2),
            beyondBoundsItemCount = 3
        ) {
            items(
                items = chipOptions,
                key = { it },
                contentType = { "pick_chip" }
            ) { option ->
                TagChip(
                    text = option,
                    selected = option == chip,
                    onClick = { chip = option }
                )
            }
        }
        if (empty) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyText,
                    color = AppColors.TextHint,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Normal,
                    lineHeight = AppType.CalloutLine
                )
            }
        } else if (category == StockPickCategory.ETF) {
            ListHeader(
                priceLabel = "净值",
                changeLabel = "涨跌幅"
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = AppDimens.Space4,
                    end = AppDimens.Space4,
                    bottom = AppDimens.Space6
                ),
                beyondBoundsItemCount = 3
            ) {
                items(
                    items = searchedEtfs,
                    key = { it.symbol },
                    contentType = { "etf_row" }
                ) { etf ->
                    ETFPickRow(etf = etf)
                }
            }
        } else {
            ListHeader(endGutter = AppDimens.MinTouch)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = AppDimens.Space4,
                    end = AppDimens.Space4,
                    bottom = AppDimens.Space6
                ),
                beyondBoundsItemCount = 3
            ) {
                items(
                    items = searchedStocks,
                    key = { it.symbol },
                    contentType = { "stock_card" }
                ) { stock ->
                    val watched = watchedSymbols.contains(stock.symbol)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = AppDimens.MinTouch)
                        ) {
                            StockCard(
                                stock = stock,
                                onClick = { onStockClick(stock.symbol) }
                            )
                        }
                        WatchStarButton(
                            watched = watched,
                            onClick = {
                                if (watched) {
                                    watchlistRepository.remove(stock.symbol)
                                    watchedSymbols.remove(stock.symbol)
                                } else {
                                    watchlistRepository.add(stock.symbol)
                                    if (!watchedSymbols.contains(stock.symbol)) {
                                        watchedSymbols.add(stock.symbol)
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ETF 简化列表项：净值 + 涨跌幅，不复用 [StockCard]（ETF 无涨跌额）。
 */
@Composable
private fun ETFPickRow(etf: ETF) {
    val changeColor = QuoteFormat.changeColor(etf.changePercent)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.HeightListItem)
            .background(AppColors.BgCard)
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
                    text = etf.name,
                    color = AppColors.TextTitle,
                    fontSize = AppType.Headline,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${etf.symbol}  IOPV ${QuoteFormat.price(etf.iopv)}",
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                modifier = Modifier.width(AppDimens.PriceColumnWidth),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)
            ) {
                Text(
                    text = QuoteFormat.price(etf.nav),
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
                            QuoteFormat.percentChipBackground(etf.changePercent),
                            RoundedCornerShape(AppDimens.RadiusBadge)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = QuoteFormat.percent(etf.changePercent),
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

private const val FILTER_ALL = "全部"

/**
 * 名称或代码包含关键词（忽略大小写）。空关键词视为不过滤。
 */
private fun <T> filterByKeyword(
    items: List<T>,
    keyword: String,
    nameAndSymbol: (T) -> Pair<String, String>
): List<T> {
    if (keyword.isEmpty()) return items
    return items.filter { item ->
        val (name, symbol) = nameAndSymbol(item)
        name.contains(keyword, ignoreCase = true) ||
            symbol.contains(keyword, ignoreCase = true)
    }
}
