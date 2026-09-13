package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsPressedAsState
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 问答预设问题芯片。视觉规格见设计系统 5.8.3 节，禁止复用 [AIAdviceChip]。
 *
 * @param text 问题文案，不超过 12 个汉字
 * @param selected 发送中 / 选中
 * @param enabled 加载期间为 false
 * @param onClick 点击后等同发送该文案
 */
@Composable
fun PresetQuestionChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(8.dp)
    val background = when {
        !enabled && !selected -> AppColors.BgCard
        selected -> AppColors.PrimaryDim
        pressed -> AppColors.BgPress
        else -> AppColors.BgCard
    }
    val borderColor = when {
        !enabled && !selected -> AppColors.Divider
        selected -> AppColors.Primary
        else -> AppColors.Border
    }
    val textColor = when {
        !enabled && !selected -> AppColors.TextDisabled
        selected -> AppColors.Primary
        else -> AppColors.TextSecondary
    }
    Box(
        modifier = modifier
            .height(AppDimens.MinTouch)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .background(background, shape)
                .border(AppDimens.StrokeDivider, borderColor, shape)
                .padding(horizontal = AppDimens.Space3),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = AppType.Caption,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
