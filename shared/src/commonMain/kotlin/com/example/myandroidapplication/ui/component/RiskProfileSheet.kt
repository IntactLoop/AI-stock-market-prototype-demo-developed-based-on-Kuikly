package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.AppContainer
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
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 首次进入行情首页时选择风险偏好。选择结果写入 [AppContainer.riskPreference]。
 *
 * @param onDismissRequest 关闭；遮罩关闭时也会标记已提示
 */
@Composable
fun RiskProfileSheet(
    onDismissRequest: () -> Unit
) {
    var selected by remember { mutableStateOf(AppContainer.riskPreference) }
    QuoteBottomSheet(
        sheetHeight = 360.dp,
        onDismissRequest = {
            AppContainer.markRiskProfilePrompted()
            onDismissRequest()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(AppColors.BgElevated)
                .padding(horizontal = AppDimens.Space4)
        ) {
            SheetDragHandle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.MinTouch),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "投顾画像",
                    color = AppColors.TextSecondary,
                    fontSize = AppType.Body,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(AppDimens.MinTouch)
                        .clickable {
                            AppContainer.markRiskProfilePrompted()
                            onDismissRequest()
                        },
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
            Text(
                text = "选择风险偏好后，问答语气会随之变化。未选则保持当前口径。",
                color = AppColors.TextHint,
                fontSize = AppType.Caption,
                fontWeight = FontWeight.Normal,
                lineHeight = AppType.CaptionLine
            )
            Spacer(modifier = Modifier.height(AppDimens.Space3))
            listOf(
                AppContainer.RISK_STEADY,
                AppContainer.RISK_BALANCED,
                AppContainer.RISK_AGGRESSIVE
            ).forEach { option ->
                PreferenceOption(
                    label = option,
                    selected = selected == option,
                    onClick = { selected = option }
                )
                Spacer(modifier = Modifier.height(AppDimens.Space2))
            }
            val confirmSource = remember { MutableInteractionSource() }
            val confirmPressed by confirmSource.collectIsPressedAsState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.HeightButton)
                    .clip(RoundedCornerShape(AppDimens.RadiusCard))
                    .background(if (confirmPressed) AppColors.BgPress else AppColors.PrimaryDim)
                    .border(AppDimens.StrokeDivider, AppColors.Primary, RoundedCornerShape(AppDimens.RadiusCard))
                    .clickable(
                        interactionSource = confirmSource,
                        indication = null,
                        onClick = {
                            AppContainer.chooseRiskPreference(selected)
                            onDismissRequest()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "确认",
                    color = AppColors.Primary,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PreferenceOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember(label) { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(8.dp)
    val background = when {
        selected -> AppColors.PrimaryDim
        pressed -> AppColors.BgPress
        else -> AppColors.BgCard
    }
    val border = if (selected) AppColors.Primary else AppColors.Border
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.MinTouch)
            .clip(shape)
            .background(background, shape)
            .border(AppDimens.StrokeDivider, border, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppDimens.Space4),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            color = if (selected) AppColors.Primary else AppColors.TextSecondary,
            fontSize = AppType.Callout,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
