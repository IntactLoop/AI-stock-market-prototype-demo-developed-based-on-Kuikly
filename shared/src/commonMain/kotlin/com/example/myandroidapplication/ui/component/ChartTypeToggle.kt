package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.ChartType
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
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 主图类型切换：折线 / K 线。禁止使用 Icon。
 *
 * @param selected 当前类型，默认折线
 * @param onSelected 切换回调
 * @param enabled 分时/五日无可靠蜡烛时为 false，K 线按钮禁用并回退折线
 */
@Composable
fun ChartTypeToggle(
    selected: ChartType,
    onSelected: (ChartType) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(AppDimens.HeightChip),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChartType.entries.forEach { type ->
            TypeChip(
                label = type.label,
                selected = type == selected,
                enabled = enabled || type == ChartType.LINE,
                onClick = { onSelected(type) }
            )
        }
    }
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember(label) { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(AppDimens.RadiusBadge)
    val background = when {
        !enabled -> AppColors.BgCard
        selected -> AppColors.PrimaryDim
        pressed -> AppColors.BgPress
        else -> AppColors.BgCard
    }
    val borderColor = when {
        !enabled -> AppColors.Divider
        selected -> AppColors.Primary
        else -> AppColors.Border
    }
    val textColor = when {
        !enabled -> AppColors.TextDisabled
        selected -> AppColors.Primary
        else -> AppColors.TextHint
    }
    Box(
        modifier = Modifier
            .height(AppDimens.HeightChip)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(background, shape)
            .border(AppDimens.StrokeDivider, borderColor, shape)
            .padding(horizontal = AppDimens.Space3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
