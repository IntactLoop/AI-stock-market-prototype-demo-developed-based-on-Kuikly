package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Dp

/**
 * 固定高度顶栏。无 scrollBehavior，规格见设计系统 6.1 节。
 *
 * @param title 标题文案
 * @param statusBarHeight 沉浸式状态栏高度
 * @param titleCentered 详情页居中；首页居左
 * @param navigation 左侧导航槽，宽高至少 [AppDimens.MinTouch]
 */
@Composable
fun QuoteTopBar(
    title: String,
    statusBarHeight: Dp,
    titleCentered: Boolean = false,
    navigation: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.BgElevated)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(statusBarHeight))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.HeightAppBar)
        ) {
            if (navigation != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(AppDimens.MinTouch),
                    contentAlignment = Alignment.CenterStart
                ) {
                    navigation()
                }
            }
            Text(
                text = title,
                color = AppColors.TextTitle,
                fontSize = AppType.Headline,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (titleCentered) {
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = AppDimens.MinTouch)
                } else {
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = AppDimens.Space4)
                }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(AppDimens.StrokeDivider)
                    .background(AppColors.Divider)
            )
        }
    }
}
