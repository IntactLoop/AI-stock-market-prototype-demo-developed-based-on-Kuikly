package com.example.myandroidapplication.ui.chat

import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.util.QuoteFormat

/**
 * 问答模板回答。结论 / 理由 / 风险必须与当前 [Stock] 一致。
 */
data class ChatReply(
    val conclusion: String,
    val reason: String,
    val riskLevel: String
)

internal val PresetQuestions = listOf(
    "适合买入吗？",
    "最近有什么风险？",
    "成交量怎么样？",
    "支撑压力在哪？",
    "今日如何总结？"
)

internal fun answerQuestion(stock: Stock, question: String): ChatReply {
    val price = QuoteFormat.price(stock.price)
    val support = QuoteFormat.price(stock.support)
    val resistance = QuoteFormat.price(stock.resistance)
    val percent = QuoteFormat.percent(stock.changePercent)
    val volume = QuoteFormat.volume(stock.volume)
    val ratio = QuoteFormat.price(stock.volumeRatio)
    return when {
        question.contains("买入") || question.contains("适合") -> ChatReply(
            conclusion = "当前建议${stock.aiAdvice}，最新价 $price。",
            reason = stock.adviceReason,
            riskLevel = stock.riskLevel
        )
        question.contains("风险") -> ChatReply(
            conclusion = "风险等级为${stock.riskLevel}。",
            reason = when (stock.riskLevel) {
                Stock.RISK_HIGH -> "价格 $price 已跌破或贴近支撑 $support，优先控制仓位。"
                Stock.RISK_MID -> "价格靠近压力 $resistance，方向未明，不宜追高。"
                else -> "价格 $price 运行于支撑 $support 附近，风险相对可控。"
            },
            riskLevel = stock.riskLevel
        )
        question.contains("成交") || question.contains("量") -> ChatReply(
            conclusion = "成交量 $volume，量比 $ratio。",
            reason = if (stock.volumeRatio >= 1.2) {
                "量能放大，资金关注度提升，仍需结合${stock.aiAdvice}看待。"
            } else if (stock.volumeRatio <= 0.8) {
                "量能偏弱，突破或反弹的确认度不足。"
            } else {
                "量能大致平稳，等待更明确的方向信号。"
            },
            riskLevel = stock.riskLevel
        )
        question.contains("支撑") || question.contains("压力") -> ChatReply(
            conclusion = "支撑 $support，压力 $resistance。",
            reason = "建议支撑买入价 ${QuoteFormat.price(stock.buyPoint)}，压力卖出价 ${QuoteFormat.price(stock.sellPoint)}。当前价 $price。",
            riskLevel = stock.riskLevel
        )
        question.contains("总结") -> ChatReply(
            conclusion = "今日涨跌幅 $percent，收盘 ${QuoteFormat.price(stock.close)}。",
            reason = stock.aiSummary,
            riskLevel = stock.riskLevel
        )
        else -> ChatReply(
            conclusion = "结合${stock.aiSignal}，当前建议${stock.aiAdvice}。",
            reason = stock.aiInterpretation,
            riskLevel = stock.riskLevel
        )
    }
}
