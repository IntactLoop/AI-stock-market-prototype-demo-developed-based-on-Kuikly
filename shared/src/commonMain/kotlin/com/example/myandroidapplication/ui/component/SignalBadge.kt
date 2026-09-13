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
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 技术信号标签。视觉规格见设计系统 5.2 节。
 *
 * @param label 信号文案，不超过 6 个汉字
 * @param selected 选中时增加 1dp 同色描边
 * @param onClick 点击展开解释；为 null 时不可点（如「+N」）
 */
@Composable
fun SignalBadge(
    label: String,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val tone = signalTone(label)
    val textColor = tone.textColor()
    val shape = RoundedCornerShape(AppDimens.RadiusBadge)
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
            .background(tone.backgroundColor(), shape)
            .border(
                width = AppDimens.StrokeDivider,
                color = if (selected) textColor else Color.Transparent,
                shape = shape
            )
            .padding(horizontal = AppDimens.Space2),
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

/**
 * 信号方向，对应设计系统 5.2 颜色映射。
 */
enum class SignalTone {
    Bullish,
    Bearish,
    Warning,
    Neutral
}

/**
 * 按信号文案判定方向。看空/跌破优先于「支撑」等临界词，避免误判。
 */
fun signalTone(label: String): SignalTone = when {
    label.startsWith("+") -> SignalTone.Neutral
    label.contains("死叉") ||
        label.contains("空头") ||
        label.contains("偏空") ||
        label.contains("超卖") ||
        label.contains("下跌") ||
        label.contains("跌破") -> SignalTone.Bearish
    label.contains("金叉") ||
        label.contains("多头") ||
        label.contains("偏多") ||
        label.contains("上涨") ||
        label == "放量" -> SignalTone.Bullish
    label.contains("超买") ||
        label.contains("压力") ||
        label.contains("支撑") ||
        label.contains("接近") -> SignalTone.Warning
    else -> SignalTone.Neutral
}

private fun SignalTone.textColor(): Color = when (this) {
    SignalTone.Bullish -> AppColors.Rise
    SignalTone.Bearish -> AppColors.Fall
    SignalTone.Warning -> AppColors.Warning
    SignalTone.Neutral -> AppColors.Primary
}

private fun SignalTone.backgroundColor(): Color = when (this) {
    SignalTone.Bullish -> AppColors.RiseDim
    SignalTone.Bearish -> AppColors.FallDim
    SignalTone.Warning -> AppColors.WarningDim
    SignalTone.Neutral -> AppColors.PrimaryDim
}
