package com.example.myandroidapplication.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.base.BridgeModule
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.model.StockFilter
import com.example.myandroidapplication.ui.AppPages
import com.example.myandroidapplication.ui.component.AIBriefingSheet
import com.example.myandroidapplication.ui.component.CompareActionBar
import com.example.myandroidapplication.ui.component.FilterPanel
import com.example.myandroidapplication.ui.component.IndexCard
import com.example.myandroidapplication.ui.component.ListHeader
import com.example.myandroidapplication.ui.component.MarketTab
import com.example.myandroidapplication.ui.component.QuoteBottomSheet
import com.example.myandroidapplication.ui.component.QuoteSort
import com.example.myandroidapplication.ui.component.QuoteTopBar
import com.example.myandroidapplication.ui.component.QuoteTopBarAction
import com.example.myandroidapplication.ui.component.RiskProfileSheet
import com.example.myandroidapplication.ui.component.SectorRow
import com.example.myandroidapplication.ui.component.SortBar
import com.example.myandroidapplication.ui.component.StockCard
import com.example.myandroidapplication.ui.component.collectBriefingItems
import com.example.myandroidapplication.ui.component.sortedByQuote
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 首页行情列表。数据来自 [AppContainer.stockRepository]。
 * TopBar → MarketTab → 指数 → 板块 → SortBar（含筛选与对比）→ 列表或空态。
 * 早报以 [AIBriefingSheet] 弹出，不占主内容区。
 */
@Page(AppPages.QUOTE_LIST)
internal class QuoteListPage : ComposeContainer() {
    override fun createExternalModules(): Map<String, Module>? {
        val modules = (super.createExternalModules() as? HashMap) ?: hashMapOf()
        modules[BridgeModule.MODULE_NAME] = BridgeModule()
        return modules
    }

    override fun willInit() {
        super.willInit()
        val statusBarDp = pagerData.statusBarHeight.dp
        val pageViewHeight = pagerData.pageViewHeight
        setContent {
            QuoteListScreen(
                statusBarHeight = statusBarDp,
                pageViewHeight = pageViewHeight,
                onStockClick = { symbol ->
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(
                        AppPages.STOCK_DETAIL,
                        JSONObject().put(AppPages.ARG_SYMBOL, symbol)
                    )
                },
                onWatchlistClick = {
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(
                        AppPages.WATCHLIST
                    )
                },
                onPickerClick = {
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(
                        AppPages.STOCK_PICKER
                    )
                },
                onRankingsClick = {
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(
                        AppPages.RANKINGS
                    )
                },
                onCompareClick = {
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(
                        AppPages.COMPARE
                    )
                },
                onCompareLimit = {
                    acquireModule<BridgeModule>(BridgeModule.MODULE_NAME).toast(
                        "最多对比 4 只股票，请先取消一只"
                    )
                }
            )
        }
    }
}

@Composable
private fun QuoteListScreen(
    statusBarHeight: Dp,
    pageViewHeight: Float,
    onStockClick: (String) -> Unit,
    onWatchlistClick: () -> Unit,
    onPickerClick: () -> Unit,
    onRankingsClick: () -> Unit,
    onCompareClick: () -> Unit,
    onCompareLimit: () -> Unit
) {
    var market by remember { mutableStateOf(Stock.MARKET_CN) }
    var sort by remember { mutableStateOf(QuoteSort.CHANGE_PERCENT) }
    var sortAscending by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(StockFilter.EMPTY) }
    var showFilter by remember { mutableStateOf(false) }
    var showOpenAccount by remember { mutableStateOf(false) }
    var showRiskProfile by remember { mutableStateOf(!AppContainer.riskProfilePrompted) }
    val repository = AppContainer.stockRepository
    val stocks = remember(market, filter) { repository.filterStocks(market, filter) }
    val sortedStocks = remember(stocks, sort, sortAscending) {
        stocks.sortedByQuote(sort, sortAscending)
    }
    val indexes = remember(market) { repository.getIndexes(market) }
    val sectors = remember(market) { repository.getSectors(market) }
    val tagOptions = remember(market) {
        repository.filterStocks(market, StockFilter.EMPTY).flatMap { it.tags }.distinct()
    }
    val listState = rememberLazyListState()
    val sheetHeight = (pageViewHeight * 0.72f).coerceIn(480f, 640f).dp
    val watchSymbols = AppContainer.watchlistRepository.getAll()
    val briefingItems = remember(watchSymbols) {
        val watched = watchSymbols.mapNotNull { symbol -> repository.getStock(symbol) }
        collectBriefingItems(watched)
    }
    var showBriefing by remember {
        val shouldAuto = briefingItems.isNotEmpty() &&
            AppContainer.riskProfilePrompted &&
            !AppContainer.briefingAutoShownThisSession
        if (shouldAuto) {
            AppContainer.markBriefingAutoShown()
        }
        mutableStateOf(shouldAuto)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgPage)
    ) {
        QuoteTopBar(
            title = "行情",
            statusBarHeight = statusBarHeight,
            titleCentered = false,
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuoteTopBarAction(text = "早报", onClick = { showBriefing = true })
                    QuoteTopBarAction(text = "选股", onClick = onPickerClick)
                    QuoteTopBarAction(text = "榜单", onClick = onRankingsClick)
                    QuoteTopBarAction(text = "自选", onClick = onWatchlistClick)
                    QuoteTopBarAction(text = "开户", onClick = { showOpenAccount = true })
                }
            }
        )
        MarketTab(
            selectedMarket = market,
            onMarketSelected = { market = it }
        )
        OverviewSectionTitle("指数")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = AppDimens.Space4),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space3),
            beyondBoundsItemCount = 3
        ) {
            items(
                items = indexes,
                key = { it.symbol },
                contentType = { "index_card" }
            ) { index ->
                IndexCard(index = index)
            }
        }
        OverviewSectionTitle("板块")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = AppDimens.Space4),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space3),
            beyondBoundsItemCount = 3
        ) {
            items(
                items = sectors,
                key = { it.name },
                contentType = { "sector_row" }
            ) { sector ->
                SectorRow(sector = sector)
            }
        }
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        SortBar(
            selected = sort,
            ascending = sortAscending,
            onSelected = { option ->
                if (option == sort) {
                    sortAscending = !sortAscending
                } else {
                    sort = option
                    sortAscending = false
                }
            },
            filterActive = !filter.isEmpty(),
            onFilterClick = { showFilter = true },
            compareMode = AppContainer.compareMode,
            onCompareToggle = {
                if (AppContainer.compareMode) {
                    AppContainer.exitCompareMode()
                } else {
                    AppContainer.enterCompareMode()
                }
            }
        )
        if (sortedStocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "没有符合条件的股票",
                    color = AppColors.TextHint,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Normal,
                    lineHeight = AppType.CalloutLine
                )
            }
        } else {
            ListHeader(
                startGutter = if (AppContainer.compareMode) AppDimens.Space8 else 0.dp
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(
                    start = AppDimens.Space4,
                    end = AppDimens.Space4,
                    top = AppDimens.Space2,
                    bottom = if (AppContainer.compareMode) {
                        AppDimens.Space6 + AppDimens.HeightChatEntry
                    } else {
                        AppDimens.Space6
                    }
                ),
                beyondBoundsItemCount = 3
            ) {
                items(
                    items = sortedStocks,
                    key = { it.symbol },
                    contentType = { "stock_card" }
                ) { stock ->
                    val compareMode = AppContainer.compareMode
                    val selected = AppContainer.compareSymbols.contains(stock.symbol)
                    val atMax = AppContainer.compareSymbols.size >= AppContainer.COMPARE_MAX
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = if (compareMode) AppDimens.Space8 else 0.dp)
                        ) {
                            StockCard(
                                stock = stock,
                                onClick = {
                                    if (compareMode) {
                                        toggleCompareOrToast(
                                            symbol = stock.symbol,
                                            selected = selected,
                                            atMax = atMax,
                                            onCompareLimit = onCompareLimit
                                        )
                                    } else {
                                        onStockClick(stock.symbol)
                                    }
                                }
                            )
                        }
                        if (compareMode) {
                            CompareGutterMark(
                                selected = selected,
                                atLimit = !selected && atMax,
                                onClick = {
                                    toggleCompareOrToast(
                                        symbol = stock.symbol,
                                        selected = selected,
                                        atMax = atMax,
                                        onCompareLimit = onCompareLimit
                                    )
                                },
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                    }
                }
            }
        }
        if (AppContainer.compareMode) {
            CompareActionBar(
                selectedCount = AppContainer.compareSymbols.size,
                onGoCompare = onCompareClick
            )
        }
        if (showFilter) {
            FilterPanel(
                applied = filter,
                tagOptions = tagOptions,
                sheetHeight = sheetHeight,
                onConfirm = { filter = it },
                onDismissRequest = { showFilter = false }
            )
        }
        if (showOpenAccount) {
            OpenAccountPlaceholder(
                onDismissRequest = { showOpenAccount = false }
            )
        }
        if (showRiskProfile) {
            RiskProfileSheet(
                onDismissRequest = {
                    showRiskProfile = false
                    if (briefingItems.isNotEmpty() && !AppContainer.briefingAutoShownThisSession) {
                        AppContainer.markBriefingAutoShown()
                        showBriefing = true
                    }
                }
            )
        }
        if (showBriefing && !showRiskProfile) {
            AIBriefingSheet(
                items = briefingItems,
                pageViewHeight = pageViewHeight,
                onDismissRequest = { showBriefing = false }
            )
        }
    }
}

private fun toggleCompareOrToast(
    symbol: String,
    selected: Boolean,
    atMax: Boolean,
    onCompareLimit: () -> Unit
) {
    if (!selected && atMax) {
        onCompareLimit()
        return
    }
    AppContainer.toggleCompareSymbol(symbol)
}

/**
 * 对比模式勾选框。画在卡片左侧 32dp 留白内，不改 [StockCard]。
 * 已达 4 只且未勾选时边框用 [AppColors.TextDisabled]。
 */
@Composable
private fun CompareGutterMark(
    selected: Boolean,
    atLimit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppDimens.RadiusBadge)
    val borderColor = when {
        selected -> AppColors.Primary
        atLimit -> AppColors.TextDisabled
        else -> AppColors.Border
    }
    Box(
        modifier = modifier
            .width(AppDimens.Space8)
            .height(AppDimens.HeightListItem)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(AppDimens.Space5)
                .background(if (selected) AppColors.Primary else AppColors.BgCard, shape)
                .border(AppDimens.StrokeDivider, borderColor, shape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    color = AppColors.TextOnAccent,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 首页横滑区小标题。12sp / [AppColors.TextHint] / 左对齐 / 上下 8dp。
 */
@Composable
private fun OverviewSectionTitle(text: String) {
    Text(
        text = text,
        color = AppColors.TextHint,
        fontSize = AppType.Caption,
        fontWeight = FontWeight.Normal,
        lineHeight = AppType.CaptionLine,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.Space4, vertical = AppDimens.Space2)
    )
}

/**
 * 开户占位。复用 [QuoteBottomSheet]，不引入新依赖。
 */
@Composable
private fun OpenAccountPlaceholder(onDismissRequest: () -> Unit) {
    QuoteBottomSheet(
        sheetHeight = 280.dp,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(AppDimens.Space4)
        ) {
            Text(
                text = "开户",
                color = AppColors.TextSecondary,
                fontSize = AppType.Body,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(AppDimens.Space3))
            Text(
                text = "开户为占位功能，尚未接入。",
                color = AppColors.TextHint,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal,
                lineHeight = AppType.CalloutLine
            )
            Spacer(modifier = Modifier.weight(1f))
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
                    )
            )
        }
    }
}
