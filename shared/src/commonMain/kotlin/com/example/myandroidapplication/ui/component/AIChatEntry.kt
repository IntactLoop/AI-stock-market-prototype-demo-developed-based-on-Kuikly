package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 详情页底部问答入口。视觉规格见设计系统 6.1 节，高 56dp、贴底圆角 0。
 *
 * @param onClick 打开 [AIChatSheet]
 */
@Composable
fun AIChatEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.HeightChatEntry)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(if (pressed) AppColors.BgPress else AppColors.BgElevated)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimens.Space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "问 AI 关于这只股票",
                color = AppColors.TextHint,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "提问",
                color = AppColors.Primary,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
