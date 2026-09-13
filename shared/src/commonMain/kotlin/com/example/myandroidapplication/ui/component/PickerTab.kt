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
import com.tencent.kuikly.compose.ui.text.style.TextOverflow

/**
 * 选股分类 Tab：选公司 / 选 ETF / 跟机构 / 跟资金。选中为主色下划线，禁止 `Icon`。
 *
 * @param tabs 分类文案，页面传入 [com.example.myandroidapplication.data.model.StockPickCategory.ALL]
 * @param selected 当前分类
 * @param onSelected 切换后由页面重新调用 Repository
 */
@Composable
fun PickerTab(
    tabs: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.BgElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.MinTouch)
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selected
                val interactionSource = remember(tab) { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (pressed) AppColors.BgPress else AppColors.BgElevated)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelected(tab) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) AppColors.Primary else AppColors.TextHint,
                        fontSize = AppType.Callout,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
