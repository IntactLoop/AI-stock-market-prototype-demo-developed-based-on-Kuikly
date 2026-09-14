package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.ModalBottomSheet
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.timer.Timer

/**
 * 行情底部 Sheet 外壳。圆角 / 阴影 / 遮罩对齐设计系统 5.8；
 * 打开后 80ms 再武装关闭，避免首帧误点遮罩。
 *
 * Kuikly [ModalBottomSheet] 只用 `visible` / `onDismissRequest`，禁止 `sheetState` / `dragHandle`。
 *
 * @param sheetHeight 各业务 Sheet 自定高度
 * @param onDismissRequest 武装后的关闭回调
 * @param content Sheet 内容，不含外壳
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteBottomSheet(
    sheetHeight: Dp,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    var dismissArmed by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val timer = Timer()
        timer.schedule(delay = 80, period = 50_000) {
            dismissArmed = true
            timer.cancel()
        }
        onDispose { timer.cancel() }
    }
    val sheetShape = RoundedCornerShape(
        topStart = AppDimens.RadiusSheet,
        topEnd = AppDimens.RadiusSheet,
        bottomEnd = 0.dp,
        bottomStart = 0.dp
    )
    ModalBottomSheet(
        visible = true,
        onDismissRequest = {
            if (dismissArmed) {
                onDismissRequest()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .shadow(
                elevation = AppDimens.SheetElevation,
                shape = sheetShape,
                clip = true,
                spotColor = AppColors.SheetShadow
            )
            .clip(sheetShape),
        containerColor = AppColors.BgElevated,
        scrimColor = AppColors.SheetScrim
    ) {
        content()
    }
}

/**
 * 设计系统 5.8 顶部拖拽条：外区高 20dp，条 32×4dp，圆角 2dp。
 * 不要使用 [ModalBottomSheet] 的 `dragHandle` 参数。
 */
@Composable
fun SheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(AppDimens.HeightProgress)
                .background(AppColors.Divider, RoundedCornerShape(2.dp))
        )
    }
}
