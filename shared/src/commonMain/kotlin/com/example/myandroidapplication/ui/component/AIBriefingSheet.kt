package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.BriefingItem
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp

private const val BriefingHandleDp = 20f
private const val BriefingTitleDp = 44f
private const val BriefingDividerDp = 1f
private const val BriefingVPadDp = 16f
private const val BriefingRowDp = 68f
private const val BriefingEmptyBodyDp = 56f
private const val BriefingMinSheetDp = 160f

/**
 * 盘前早报底部 Sheet。外壳复用 [QuoteBottomSheet]（可见才组合、80ms 武装关闭）。
 * 高度按内容估算，上限为页面高度的 60%，避免固定 60% 留下大片空白。
 *
 * @param items 已按股票合并的早报；空列表展示「暂无自选」
 * @param pageViewHeight 页面高度，用于限制最大高度
 * @param onDismissRequest 关闭；遮罩与「关闭」共用
 */
@Composable
fun AIBriefingSheet(
    items: List<BriefingItem>,
    pageViewHeight: Float,
    onDismissRequest: () -> Unit
) {
    QuoteBottomSheet(
        sheetHeight = briefingSheetHeight(items.size, pageViewHeight),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.BgElevated)
        ) {
            SheetDragHandle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.MinTouch)
                    .padding(horizontal = AppDimens.Space4),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI 盘前早报",
                    color = AppColors.TextTitle,
                    fontSize = AppType.Body,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(AppDimens.MinTouch)
                        .clickable(onClick = onDismissRequest),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "关闭",
                        color = AppColors.Primary,
                        fontSize = AppType.Callout,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.StrokeDivider)
                    .background(AppColors.Divider)
            )
            AIBriefingCard(
                items = items,
                modifier = Modifier.padding(
                    start = AppDimens.Space4,
                    end = AppDimens.Space4,
                    bottom = AppDimens.Space3
                )
            )
        }
    }
}

internal fun briefingSheetHeight(itemCount: Int, pageViewHeight: Float): Dp {
    val header = BriefingHandleDp + BriefingTitleDp + BriefingDividerDp + BriefingVPadDp
    val body = if (itemCount <= 0) BriefingEmptyBodyDp else itemCount * BriefingRowDp
    val content = header + body
    val maxHeight = (pageViewHeight * 0.6f).coerceAtLeast(BriefingMinSheetDp)
    return content.coerceIn(BriefingMinSheetDp, maxHeight).dp
}
