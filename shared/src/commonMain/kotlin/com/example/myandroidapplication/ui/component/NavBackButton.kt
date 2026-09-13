package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.ui.theme.AppDimens
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.base.attr.ImageUri

/**
 * 详情页返回键。视觉规格见设计系统 6.1 节：热区 44×44，PNG 24×24，禁止 `Icon`。
 *
 * @param onClick 关闭当前页
 */
@OptIn(InternalResourceApi::class)
@Composable
fun NavBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawable = DrawableResource(ImageUri.commonAssets("ic_back.png").toUrl(""))
    Box(
        modifier = modifier
            .size(AppDimens.MinTouch)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawable),
            contentDescription = "返回",
            modifier = Modifier.size(24.dp)
        )
    }
}
