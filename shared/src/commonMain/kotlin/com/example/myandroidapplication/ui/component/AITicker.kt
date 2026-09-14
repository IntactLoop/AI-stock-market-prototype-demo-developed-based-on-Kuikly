package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.model.WatchAlert
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsPressedAsState
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.timer.Timer

/**
 * 详情页盯盘提醒条。视觉规格见设计系统 5.5 节：36dp、3000ms 硬切、几何圆点。
 * 事件类型竖条 / 圆点用 [AppColors.AI]，不占用涨跌红绿。
 *
 * @param alerts 至少 4 条（风险 / 资金 / 趋势 / 事件）；为空时不展示（由调用方隐藏）
 * @param onClick 点击当前条，跳转到对应分析区域
 */
@Composable
fun AITicker(
    alerts: List<WatchAlert>,
    onClick: (WatchAlert) -> Unit,
    modifier: Modifier = Modifier
) {
    val indexState = remember(alerts) { mutableStateOf(0) }
    DisposableEffect(alerts) {
        val timer = Timer()
        if (alerts.size > 1) {
            timer.schedule(delay = 3000, period = 3000) {
                val size = alerts.size
                if (size > 0) {
                    indexState.value = (indexState.value + 1) % size
                }
            }
        }
        onDispose { timer.cancel() }
    }
    val alert = alerts.getOrNull(indexState.value.coerceIn(0, (alerts.size - 1).coerceAtLeast(0)))
    if (alert == null) {
        return
    }
    val accent = tickerAccent(alert.type)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.HeightTicker)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(alert) }
            )
            .background(if (pressed) AppColors.BgPress else AppColors.BgElevated)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(AppDimens.StrokeAccent)
                    .fillMaxHeight()
                    .background(accent)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = AppDimens.Space3, end = AppDimens.Space4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(accent, RoundedCornerShape(3.dp))
                )
                Text(
                    text = alert.message,
                    color = AppColors.TextSecondary,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
    }
}

private fun tickerAccent(type: String): Color = when (type) {
    Stock.ALERT_RISK -> AppColors.Rise
    Stock.ALERT_FUND -> AppColors.Warning
    Stock.ALERT_TREND -> AppColors.Success
    Stock.ALERT_EVENT -> AppColors.AI
    else -> AppColors.Success
}
