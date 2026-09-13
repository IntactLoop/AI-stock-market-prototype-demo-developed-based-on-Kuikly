package com.example.myandroidapplication.data.model

/**
 * AI 信号解读的一步推理。标记用字符，禁止 [androidx] Icon。
 *
 * @property marker `✓` 确认、`⚠` 风险、`→` 推导
 * @property text 与当前股票数据绑定的短句，不可写死成全市场同一文案
 */
data class ReasoningStep(
    val marker: String,
    val text: String
) {
    companion object {
        const val MARK_OK = "✓"
        const val MARK_WARN = "⚠"
        const val MARK_NEXT = "→"
    }
}
