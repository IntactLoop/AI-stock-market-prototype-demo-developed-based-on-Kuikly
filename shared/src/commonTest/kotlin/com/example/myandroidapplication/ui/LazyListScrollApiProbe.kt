package com.example.myandroidapplication.ui

import com.tencent.kuikly.compose.foundation.lazy.LazyListState

/**
 * Phase 13.2 开始前的滚动 API 预检。
 *
 * Kuikly Compose 可编程滚动是 [LazyListState.requestScrollToItem]，
 * 不是 Jetpack Compose 的 suspend `scrollToItem` / `animateScrollToItem`。
 *
 * 现有调用（已随 `:shared:compileDebugKotlinAndroid` 通过）：
 * - 详情页盯盘点击：`listState.requestScrollToItem(target)`
 * - 问答 Sheet 发消息后：`listState.requestScrollToItem(index)`
 *
 * 13.2 旅程条点击应复用同一 API；若运行时滚动无效，再降级为只高亮当前段、不滚动。
 */
internal object LazyListScrollApiProbe {
    fun requestScroll(state: LazyListState, index: Int) {
        state.requestScrollToItem(index)
    }
}
