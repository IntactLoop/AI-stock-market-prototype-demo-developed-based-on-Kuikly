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

/**
 * 上一轮回答后的追问，最多 3 条，且不含刚问过的问题。
 */
internal fun followUpQuestions(stock: Stock, lastQuestion: String): List<String> {
    val count = if (stock.aiAdvice == Stock.ADVICE_HOLD) 2 else 3
    return PresetQuestions
        .filter { candidate -> !sameQuestionTopic(candidate, lastQuestion) }
        .take(count)
}

/**
 * 追问回答：复用已有模板，仅在结论前加上下文前缀。
 */
internal fun answerFollowUp(stock: Stock, question: String, previousQuestion: String): ChatReply {
    val base = answerQuestion(stock, question)
    return base.copy(conclusion = followUpPrefix(previousQuestion) + base.conclusion)
}

internal fun followUpPrefix(previousQuestion: String): String = when {
    previousQuestion.contains("支撑") || previousQuestion.contains("压力") ->
        "基于刚才关于支撑位的讨论，"
    previousQuestion.contains("买入") || previousQuestion.contains("适合") ->
        "基于刚才关于买卖建议的讨论，"
    previousQuestion.contains("风险") ->
        "基于刚才关于风险的讨论，"
    previousQuestion.contains("成交") || previousQuestion.contains("量") ->
        "基于刚才关于成交量的讨论，"
    previousQuestion.contains("总结") ->
        "基于刚才的行情总结，"
    else -> "基于刚才的讨论，"
}

private fun sameQuestionTopic(candidate: String, lastQuestion: String): Boolean = when {
    lastQuestion.contains("买入") || lastQuestion.contains("适合") ->
        candidate.contains("买入") || candidate.contains("适合")
    lastQuestion.contains("风险") -> candidate.contains("风险")
    lastQuestion.contains("成交") || lastQuestion.contains("量") ->
        candidate.contains("成交") || candidate.contains("量")
    lastQuestion.contains("支撑") || lastQuestion.contains("压力") ->
        candidate.contains("支撑") || candidate.contains("压力")
    lastQuestion.contains("总结") -> candidate.contains("总结")
    else -> candidate == lastQuestion
}
