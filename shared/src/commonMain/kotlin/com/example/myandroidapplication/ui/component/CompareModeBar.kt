package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 对比模式开关。放在 SortBar 下方，不改 [StockCard] / [SortBar] / 顶栏高度。
 *
 * @param compareMode 是否处于对比勾选态
 * @param onToggle 进入或退出；退出由调用方清空勾选
 */
@Composable
fun CompareModeBar(
    compareMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.MinTouch)
            .padding(horizontal = AppDimens.Space4),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = if (compareMode) "取消对比" else "对比",
            color = AppColors.Primary,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .height(AppDimens.MinTouch)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
                .padding(horizontal = AppDimens.Space2)
        )
    }
}

/**
 * 叠在 [StockCard] 左侧的勾选标记。几何方框 + `✓` 字符，禁止 `Icon`。
 *
 * @param selected 是否已勾选
 * @param onClick 切换勾选
 */
@Composable
fun CompareSelectMark(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppDimens.RadiusBadge)
    Box(
        modifier = modifier
            .size(AppDimens.MinTouch)
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
                .border(
                    AppDimens.StrokeDivider,
                    if (selected) AppColors.Primary else AppColors.Border,
                    shape
                ),
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
 * 对比模式底部操作条。「已选 N 只，去对比」；不足 2 只时不可点。
 *
 * @param selectedCount 当前勾选数量
 * @param onGoCompare 进入对比页
 */
@Composable
fun CompareActionBar(
    selectedCount: Int,
    onGoCompare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = selectedCount in 2..4
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.BgElevated)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.HeightChatEntry)
                .then(
                    if (enabled) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onGoCompare
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = AppDimens.Space4),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "已选 ${selectedCount} 只，去对比",
                color = if (enabled) AppColors.Primary else AppColors.TextDisabled,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
