package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.AdjustType
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 复权方式：不复权 / 前复权 / 后复权。Mock 可用价格缩放，但 UI 状态必须真实切换。
 *
 * @param selected 当前复权
 * @param onSelected 切换后由页面把 [AdjustType] 传给 getChartData
 */
@Composable
fun AdjustSelector(
    selected: AdjustType,
    onSelected: (AdjustType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.HeightChip),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdjustType.entries.forEach { type ->
            AdjustChip(
                label = type.label,
                selected = type == selected,
                onClick = { onSelected(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AdjustChip(
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
