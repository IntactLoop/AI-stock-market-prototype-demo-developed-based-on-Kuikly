package com.example.myandroidapplication.ui.util

import com.example.myandroidapplication.ui.theme.AppColors
import com.tencent.kuikly.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.round

/**
 * 金融数字格式化，规则见设计系统 3.4 节。
 */
object QuoteFormat {

    /**
     * 固定两位小数，如 `178.50`。
     */
    fun price(value: Double): String = twoDecimals(value)

    /**
     * 涨跌额。上涨带 `+`，平盘不带符号。
     */
    fun change(value: Double): String = signed(value)

    /**
     * 涨跌幅，无空格百分号，如 `+0.67%`。
     */
    fun percent(value: Double): String = "${signed(value)}%"

    /**
     * 与价格同组的涨跌色。
     */
    fun changeColor(changePercent: Double): Color = when {
        changePercent > 0.0 -> AppColors.Rise
        changePercent < 0.0 -> AppColors.Fall
        else -> AppColors.Flat
    }

    fun percentChipBackground(changePercent: Double): Color = when {
        changePercent > 0.0 -> AppColors.RiseDim
        changePercent < 0.0 -> AppColors.FallDim
        else -> AppColors.FlatDim
    }

    /**
     * 成交量：不足万用整数，万/亿保留两位小数。
     */
    fun volume(value: Long): String = when {
        value < 10_000L -> value.toString()
        value < 100_000_000L -> "${twoDecimals(value / 10_000.0)}万"
        else -> "${twoDecimals(value / 100_000_000.0)}亿"
    }

    private fun signed(value: Double): String {
        val body = twoDecimals(abs(value))
        return when {
            value > 0.0 -> "+$body"
            value < 0.0 -> "-$body"
            else -> twoDecimals(0.0)
        }
    }

    private fun twoDecimals(value: Double): String {
        val cents = round(value * 100.0).toLong()
        val negative = cents < 0
        val absCents = abs(cents)
        val text = "${absCents / 100}.${(absCents % 100).toString().padStart(2, '0')}"
        return if (negative) "-$text" else text
    }
}
