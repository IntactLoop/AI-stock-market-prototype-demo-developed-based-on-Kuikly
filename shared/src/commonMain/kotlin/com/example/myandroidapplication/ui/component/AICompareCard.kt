package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow

/**
 * 多股 AI 综合分析。独立卡片，不复用 [AIAdviceCard] 对外接口。
 * 推荐、建议与风险文案随对比股票组合变化。
 *
 * @param stocks 2–4 只已勾选股票
 */
@Composable
fun AICompareCard(
    stocks: List<Stock>,
    modifier: Modifier = Modifier
) {
    val analysis = buildCompareAnalysis(stocks)
    AICardFrame(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space3)
    ) {
            Text(
                text = "AI 综合分析",
                color = AppColors.TextSecondary,
                fontSize = AppType.Body,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = analysis.recommend,
                color = AppColors.TextTitle,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Medium,
                lineHeight = AppType.CalloutLine,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Space2)) {
                analysis.adviceLines.forEach { line ->
                    Text(
                        text = line,
                        color = AppColors.TextBody,
                        fontSize = AppType.Callout,
                        fontWeight = FontWeight.Normal,
                        lineHeight = AppType.CalloutLine,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = analysis.risk,
                color = AppColors.TextBody,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal,
                lineHeight = AppType.CalloutLine,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
    }
}

private data class CompareAnalysis(
    val recommend: String,
    val adviceLines: List<String>,
    val risk: String
)

private fun buildCompareAnalysis(stocks: List<Stock>): CompareAnalysis {
    if (stocks.isEmpty()) {
        return CompareAnalysis("暂无对比标的。", emptyList(), "请返回列表勾选 2–4 只股票。")
    }
    val ranked = stocks.sortedByDescending { compareScore(it) }
    val best = ranked.first()
    val worst = ranked.last()
    val recommend = "综合置信度 ${best.aiConfidence}%、短期${best.shortTrend}与风险后，更看好 ${best.name}（${best.symbol}）。"
    val adviceLines = stocks.map { stock ->
        "${stock.name}：${stock.aiAdvice}，${stock.riskLevel}，短期${stock.shortTrend}"
    }
    val highRisk = stocks.filter { it.riskLevel == Stock.RISK_HIGH }
    val risk = if (highRisk.isNotEmpty()) {
        "横向风险：${highRisk.joinToString("、") { it.name }} 为高风险，关注支撑 ${QuoteFormat.price(highRisk.first().support)}。"
    } else if (best.symbol != worst.symbol) {
        "横向风险：${worst.name} 相对偏弱（${QuoteFormat.percent(worst.changePercent)}），组合整体风险可控。"
    } else {
        "横向风险：样本过少，建议再选一只对照。"
    }
    return CompareAnalysis(recommend, adviceLines, risk)
}

private fun compareScore(stock: Stock): Int {
    val adviceBoost = when (stock.aiAdvice) {
        Stock.ADVICE_BUY -> 12
        Stock.ADVICE_HOLD -> 0
        else -> -12
    }
    val riskPenalty = when (stock.riskLevel) {
        Stock.RISK_HIGH -> -10
        Stock.RISK_MID -> 0
        else -> 6
    }
    return stock.aiConfidence + adviceBoost + riskPenalty + stock.changePercent.toInt()
}
