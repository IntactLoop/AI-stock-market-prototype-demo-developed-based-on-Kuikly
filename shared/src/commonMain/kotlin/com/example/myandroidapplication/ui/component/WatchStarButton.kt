package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 自选星标。用文字 ☆/★，禁止 [com.tencent.kuikly.compose.material3] Icon。
 * 热区 [AppDimens.MinTouch]；点击由本组件 [clickable] 消费，不要放进 [StockCard]。
 *
 * @param watched 是否已在自选
 * @param onClick 切换自选
 */
@Composable
fun WatchStarButton(
    watched: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        Text(
            text = if (watched) "★" else "☆",
            color = if (watched) AppColors.Warning else AppColors.TextHint,
            fontSize = AppType.Headline,
            fontWeight = FontWeight.Medium
        )
    }
}
