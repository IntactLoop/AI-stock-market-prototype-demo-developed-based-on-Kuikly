package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.ChainPeer
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow

/**
 * 产业链关联公司列表，不是可拖拽图谱。
 * 数据取自 [Stock.chainPeers]，涨跌幅须与仓库中该代码一致。
 *
 * @param stock 当前个股
 */
@Composable
fun IndustryChainCard(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    if (stock.chainPeers.isEmpty()) return
    AICardFrame(modifier = modifier) {
            Text(
                text = "产业链",
                color = AppColors.TextSecondary,
                fontSize = AppType.Body,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(AppDimens.Space3))
            stock.chainPeers.forEach { peer ->
                ChainPeerRow(peer = peer)
                Spacer(modifier = Modifier.height(AppDimens.Space2))
            }
    }
}

@Composable
private fun ChainPeerRow(peer: ChainPeer) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = peer.relation,
                color = if (peer.relation == ChainPeer.UPSTREAM) AppColors.AI else AppColors.Primary,
                fontSize = AppType.Caption,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = AppDimens.Space2)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer.name,
                    color = AppColors.TextTitle,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = peer.symbol,
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
        Text(
            text = QuoteFormat.percent(peer.changePercent),
            color = QuoteFormat.changeColor(peer.changePercent),
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium
        )
    }
}
