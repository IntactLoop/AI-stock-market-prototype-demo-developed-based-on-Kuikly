package com.example.myandroidapplication.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.RankingType
import com.example.myandroidapplication.ui.AppPages
import com.example.myandroidapplication.ui.component.NavBackButton
import com.example.myandroidapplication.ui.component.PickerTab
import com.example.myandroidapplication.ui.component.QuoteTopBar
import com.example.myandroidapplication.ui.component.RankingItem
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 基金榜单页。四个榜来自 [AppContainer.stockRepository.getRankings]，不写死在页面。
 */
@Page(AppPages.RANKINGS)
internal class RankingsPage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        val statusBarDp = pagerData.statusBarHeight.dp
        setContent {
            RankingsScreen(
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
private fun RankingsScreen(
    statusBarHeight: Dp,
    onBack: () -> Unit,
    onStockClick: (String) -> Unit
) {
    val repository = AppContainer.stockRepository
    var type by remember { mutableStateOf(RankingType.FUND_HEAVY) }
    val rankings = remember(type) { repository.getRankings(type) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgPage)
    ) {
        QuoteTopBar(
            title = "榜单",
            statusBarHeight = statusBarHeight,
            titleCentered = true,
            navigation = { NavBackButton(onBack) }
        )
        PickerTab(
            tabs = RankingType.ALL,
            selected = type,
            onSelected = { type = it }
        )
        if (rankings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无榜单数据",
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
                    items = rankings,
                    key = { "${it.rank}-${it.symbol}" },
                    contentType = { "ranking_item" }
                ) { item ->
                    RankingItem(
                        ranking = item,
                        onClick = { onStockClick(item.symbol) }
                    )
                }
            }
        }
    }
}
