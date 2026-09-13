package com.example.myandroidapplication.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.AppPages
import com.example.myandroidapplication.ui.component.NavBackButton
import com.example.myandroidapplication.ui.component.QuoteTopBar
import com.example.myandroidapplication.ui.component.QuoteTopBarAction
import com.example.myandroidapplication.ui.component.StockCard
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.ModalBottomSheet
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.timer.Timer

/**
 * 自选股页。复用 [StockCard]，取消自选走外层文字按钮；空态 14sp Hint。
 * 加/取消状态来自 [AppContainer.watchlistRepository]，进程内保留。
 */
@Page(AppPages.WATCHLIST)
internal class WatchlistPage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        val statusBarDp = pagerData.statusBarHeight.dp
        val pageViewHeight = pagerData.pageViewHeight
        setContent {
            WatchlistScreen(
                statusBarHeight = statusBarDp,
                pageViewHeight = pageViewHeight,
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
private fun WatchlistScreen(
    statusBarHeight: Dp,
    pageViewHeight: Float,
    onBack: () -> Unit,
    onStockClick: (String) -> Unit
) {
    val stockRepository = AppContainer.stockRepository
    val watchlistRepository = AppContainer.watchlistRepository
    var revision by remember { mutableStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    val symbols = remember(revision) { watchlistRepository.getAll() }
    val stocks = remember(symbols) {
        symbols.mapNotNull { symbol -> stockRepository.getStock(symbol) }
    }
    val sheetHeight = (pageViewHeight * 0.72f).coerceIn(480f, 640f).dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgPage)
    ) {
        QuoteTopBar(
            title = "自选",
            statusBarHeight = statusBarHeight,
            titleCentered = true,
            navigation = { NavBackButton(onBack) },
            actions = {
                QuoteTopBarAction(
                    text = "添加",
                    onClick = { showAdd = true }
                )
            }
        )
        if (stocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无自选股票",
                    color = AppColors.TextHint,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Normal,
                    lineHeight = AppType.CalloutLine
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = AppDimens.Space4,
                    end = AppDimens.Space4,
                    top = AppDimens.Space2,
                    bottom = AppDimens.Space6
                ),
                beyondBoundsItemCount = 3
            ) {
                items(
                    items = stocks,
                    key = { it.symbol },
                    contentType = { "watchlist_card" }
                ) { stock ->
                    WatchlistRow(
                        stock = stock,
                        onClick = { onStockClick(stock.symbol) },
                        onRemove = {
                            watchlistRepository.remove(stock.symbol)
                            revision += 1
                        }
                    )
                }
            }
        }
        if (showAdd) {
            AddWatchlistSheet(
                sheetHeight = sheetHeight,
                revision = revision,
                onAdded = { revision += 1 },
                onDismissRequest = { showAdd = false }
            )
        }
    }
}

/**
 * 外层 [Row] 叠「取消」文字，不改 [StockCard] 本身。
 */
@Composable
private fun WatchlistRow(
    stock: Stock,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            StockCard(stock = stock, onClick = onClick)
        }
        Box(
            modifier = Modifier
                .height(AppDimens.HeightListItem)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRemove
                )
                .padding(horizontal = AppDimens.Space2),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "取消",
                color = AppColors.Primary,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 从全市场列表挑选加入自选。Kuikly [ModalBottomSheet] 仅 `visible` API。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWatchlistSheet(
    sheetHeight: Dp,
    revision: Int,
    onAdded: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var dismissArmed by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val timer = Timer()
        timer.schedule(delay = 80, period = 50_000) {
            dismissArmed = true
            timer.cancel()
        }
        onDispose { timer.cancel() }
    }
    val watchlistRepository = AppContainer.watchlistRepository
    val candidates = remember(revision) {
        val repo = AppContainer.stockRepository
        listOf(Stock.MARKET_CN, Stock.MARKET_HK, Stock.MARKET_US)
            .flatMap { market -> repo.getStocksByMarket(market) }
            .filterNot { stock -> watchlistRepository.contains(stock.symbol) }
    }
    val sheetShape = RoundedCornerShape(
        topStart = AppDimens.RadiusSheet,
        topEnd = AppDimens.RadiusSheet,
        bottomEnd = 0.dp,
        bottomStart = 0.dp
    )
    ModalBottomSheet(
        visible = true,
        onDismissRequest = {
            if (dismissArmed) {
                onDismissRequest()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .shadow(
                elevation = 8.dp,
                shape = sheetShape,
                clip = true,
                spotColor = Color(0x66000000)
            )
            .clip(sheetShape),
        containerColor = AppColors.BgElevated,
        scrimColor = Color(0x990B0E14)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = AppDimens.Space4)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.MinTouch),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "添加自选",
                    color = AppColors.TextSecondary,
                    fontSize = AppType.Body,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
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
                        )
                        .padding(horizontal = AppDimens.Space2)
                )
            }
            if (candidates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "没有可添加的股票",
                        color = AppColors.TextHint,
                        fontSize = AppType.Callout,
                        fontWeight = FontWeight.Normal,
                        lineHeight = AppType.CalloutLine
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = AppDimens.Space6),
                    beyondBoundsItemCount = 3
                ) {
                    items(
                        items = candidates,
                        key = { it.symbol },
                        contentType = { "add_watchlist_card" }
                    ) { stock ->
                        StockCard(
                            stock = stock,
                            onClick = {
                                watchlistRepository.add(stock.symbol)
                                onAdded()
                            }
                        )
                    }
                }
            }
        }
    }
}
