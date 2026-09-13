package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myandroidapplication.data.model.ChartPeriod
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsPressedAsState
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight

/**
 * 详情走势周期：分时 / 五日 / 日K / 周K / 月K。
 *
 * @param selected 当前周期
 * @param onSelected 切换后由页面调用 [com.example.myandroidapplication.data.repository.StockRepository.getChartData]
 */
@Composable
fun PeriodTab(
    selected: ChartPeriod,
    onSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.MinTouch)
        ) {
            ChartPeriod.entries.forEach { period ->
                val isSelected = period == selected
                val interactionSource = remember(period) { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (pressed) AppColors.BgPress else AppColors.BgPage)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelected(period) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.label,
                        color = if (isSelected) AppColors.Primary else AppColors.TextHint,
                        fontSize = AppType.Caption,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(AppDimens.StrokeAccent)
                                .background(AppColors.Primary)
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
    }
}
