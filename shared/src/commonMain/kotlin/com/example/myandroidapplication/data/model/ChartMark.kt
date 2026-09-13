package com.example.myandroidapplication.data.model

/**
 * 走势图上的一处 AI 自动标注，坐标用 [pointIndex] 对齐 [Stock.chartPoints]。
 *
 * @property pointIndex 对应 `chartPoints` 下标，绘制时按该点价格定位
 * @property type 标注类别，如买入信号 / 压力位 / 放量点
 * @property label 图上短标签
 * @property note 点击气泡解读
 */
data class ChartMark(
    val pointIndex: Int,
    val type: String,
    val label: String,
    val note: String
) {
    companion object {
        const val TYPE_BUY = "买入信号"
        const val TYPE_RESISTANCE = "压力位"
        const val TYPE_SUPPORT = "支撑位"
        const val TYPE_VOLUME = "放量点"
        const val TYPE_BREAK = "跌破"
    }
}
