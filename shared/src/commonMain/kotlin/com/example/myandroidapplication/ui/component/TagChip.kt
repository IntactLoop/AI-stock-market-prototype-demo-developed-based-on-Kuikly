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
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 个股特征标签。详情页只展示；筛选面板可点选。禁止复用 [SignalBadge] / [AIAdviceChip] / [PresetQuestionChip]。
 *
 * @param text 标签文案，来自 [com.example.myandroidapplication.data.model.Stock.tags]
 * @param selected 筛选多选时的选中态；详情页保持默认 false
 * @param onClick 筛选面板点击回调；详情页为 null 表示不可点
 */
@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(8.dp)
    val selectable = onClick != null
    val background = when {
        selectable && selected -> AppColors.PrimaryDim
        selectable -> AppColors.BgCard
        else -> AppColors.AIDim
    }
    val borderColor = when {
        selectable && selected -> AppColors.Primary
        selectable -> AppColors.Border
        else -> AppColors.AI
    }
    val textColor = when {
        selectable && selected -> AppColors.Primary
        selectable -> AppColors.TextHint
        else -> AppColors.AI
    }
    val clickModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    }
    Box(
        modifier = modifier
            .then(clickModifier)
            .height(AppDimens.HeightBadge)
            .background(background, shape)
            .border(AppDimens.StrokeDivider, borderColor, shape)
            .padding(horizontal = AppDimens.Space2),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
