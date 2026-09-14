package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.ColumnScope
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip

/**
 * AI 卡片外壳：`ColorBgCardAI` + 左侧 3dp 青色竖条 + 1dp 描边。
 * 仅用于结构相同的早报 / 对比综合 / 舆情 / 产业链，不改各卡对外签名。
 *
 * @param modifier 外层修饰
 * @param verticalArrangement 内容列间距，默认顶对齐
 * @param content 卡片正文
 */
@Composable
fun AICardFrame(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.BgCardAI)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape)
    ) {
        Box(
            modifier = Modifier
                .width(AppDimens.StrokeAccent)
                .fillMaxHeight()
                .background(AppColors.AI)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(AppDimens.Space4),
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}
