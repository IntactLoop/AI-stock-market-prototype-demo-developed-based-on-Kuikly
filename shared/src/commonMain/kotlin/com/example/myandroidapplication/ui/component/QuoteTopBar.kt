package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
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
 * @param actions 右侧文字入口槽；不改 56dp 高度与标题字号
 */
@Composable
fun QuoteTopBar(
    title: String,
    statusBarHeight: Dp,
    titleCentered: Boolean = false,
    navigation: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
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
                        .padding(
                            start = AppDimens.MinTouch,
                            end = if (actions != null) AppDimens.MinTouch * 2 else AppDimens.MinTouch
                        )
                } else {
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(
                            start = AppDimens.Space4,
                            end = if (actions != null) AppDimens.MinTouch * 4 else AppDimens.Space4
                        )
                }
            )
            if (actions != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(AppDimens.MinTouch)
                        .padding(end = AppDimens.Space2),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    actions()
                }
            }
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

/**
 * 顶栏右侧文字入口。14sp Medium [AppColors.Primary]，禁止 `Icon`。
 */
@Composable
fun QuoteTopBarAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(AppDimens.MinTouch)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppDimens.Space1),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AppColors.Primary,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
