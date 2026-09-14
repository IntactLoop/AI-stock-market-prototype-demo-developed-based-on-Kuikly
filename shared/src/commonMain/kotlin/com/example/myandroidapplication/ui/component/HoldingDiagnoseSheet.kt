package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.example.myandroidapplication.ui.util.QuoteFormat
import com.tencent.kuikly.compose.extension.placeHolder
import com.tencent.kuikly.compose.extension.setProp
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsPressedAsState
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.foundation.text.KeyboardOptions
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TextField
import com.tencent.kuikly.compose.material3.TextFieldDefaults
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.input.KeyboardType
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 详情建议卡附近的持仓诊断入口。不改 [AIAdviceCard] 对外接口。
 *
 * @param onClick 打开 [HoldingDiagnoseSheet]
 */
@Composable
fun HoldingDiagnoseEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.MinTouch)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(if (pressed) AppColors.BgPress else Color.Transparent)
            .padding(horizontal = AppDimens.Space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "持仓诊断",
            color = AppColors.Primary,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "输入成本与数量",
            color = AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * 持仓诊断 Sheet。成本/数量为本地输入，不写入 [Stock]。
 * 盈亏与解套点位用现价 / 支撑 / 压力 / 买卖点 / 建议模板计算。
 *
 * @param stock 当前个股
 * @param sheetHeight 与问答 Sheet 相同的高度策略
 * @param onDismissRequest 关闭
 */
@Composable
fun HoldingDiagnoseSheet(
    stock: Stock,
    sheetHeight: Dp,
    onDismissRequest: () -> Unit
) {
    QuoteBottomSheet(
        sheetHeight = sheetHeight,
        onDismissRequest = onDismissRequest
    ) {
        HoldingDiagnoseBody(
            stock = stock,
            onClose = onDismissRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingDiagnoseBody(
    stock: Stock,
    onClose: () -> Unit
) {
    var costText by remember(stock.symbol) { mutableStateOf("") }
    var qtyText by remember(stock.symbol) { mutableStateOf("") }
    val diagnosis = remember(stock.symbol, stock.price, costText, qtyText) {
        val cost = parsePositiveNumber(costText)
        val qty = parsePositiveNumber(qtyText)
        if (cost == null || qty == null) null else diagnoseHolding(stock, cost, qty)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(AppColors.BgElevated)
    ) {
        SheetDragHandle()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.MinTouch)
                .padding(horizontal = AppDimens.Space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "持仓诊断",
                color = AppColors.TextSecondary,
                fontSize = AppType.Body,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .size(AppDimens.MinTouch)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "关闭",
                    color = AppColors.Primary,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = AppDimens.Space4,
                vertical = AppDimens.Space3
            ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Space3),
            beyondBoundsItemCount = 3
        ) {
            item(key = "holding_cost") {
                DiagnoseNumberField(
                    label = "成本价",
                    value = costText,
                    placeholder = "例如 ${QuoteFormat.price(stock.price)}",
                    onValueChange = { costText = it }
                )
            }
            item(key = "holding_qty") {
                DiagnoseNumberField(
                    label = "数量（股）",
                    value = qtyText,
                    placeholder = "例如 100",
                    onValueChange = { qtyText = it }
                )
            }
            item(key = "holding_result") {
                if (diagnosis == null) {
                    Text(
                        text = "输入大于 0 的成本价和数量后，展示盈亏与解套策略。",
                        color = AppColors.TextHint,
                        fontSize = AppType.Caption,
                        fontWeight = FontWeight.Normal,
                        lineHeight = AppType.CaptionLine
                    )
                } else {
                    DiagnosisResult(diagnosis = diagnosis)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiagnoseNumberField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    val fieldShape = RoundedCornerShape(8.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(AppDimens.Space1))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.MinTouch)
                .clip(fieldShape)
                .background(AppColors.BgPage, fieldShape)
                .placeHolder(placeholder, AppColors.TextHint)
                .setProp("maxTextLength", 12),
            textStyle = TextStyle(
                color = AppColors.TextTitle,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = fieldShape,
            colors = TextFieldDefaults.colors(
                focusedTextColor = AppColors.TextTitle,
                unfocusedTextColor = AppColors.TextTitle,
                disabledTextColor = AppColors.TextDisabled,
                focusedContainerColor = AppColors.BgPage,
                unfocusedContainerColor = AppColors.BgPage,
                disabledContainerColor = AppColors.BgPage,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = AppColors.Primary
            )
        )
    }
}

@Composable
private fun DiagnosisResult(diagnosis: HoldingDiagnosis) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space2)
    ) {
        Text(
            text = diagnosis.statusLabel,
            color = QuoteFormat.changeColor(diagnosis.pnlPercent),
            fontSize = AppType.Callout,
            fontWeight = FontWeight.SemiBold
        )
        ResultRow("现价", QuoteFormat.price(diagnosis.lastPrice))
        ResultRow("成本", QuoteFormat.price(diagnosis.cost))
        ResultRow("数量", QuoteFormat.price(diagnosis.quantity))
        ResultRow("市值", QuoteFormat.price(diagnosis.marketValue))
        ResultRow(
            label = "盈亏",
            value = "${QuoteFormat.change(diagnosis.pnl)}（${QuoteFormat.percent(diagnosis.pnlPercent)}）",
            valueColor = QuoteFormat.changeColor(diagnosis.pnlPercent)
        )
        ResultRow("解套/保本", QuoteFormat.price(diagnosis.breakEven))
        ResultRow("支撑", QuoteFormat.price(diagnosis.support))
        ResultRow("压力", QuoteFormat.price(diagnosis.resistance))
        ResultRow("买入点", QuoteFormat.price(diagnosis.buyPoint))
        ResultRow("卖出点", QuoteFormat.price(diagnosis.sellPoint))
        Text(
            text = diagnosis.strategy,
            color = AppColors.TextBody,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Normal,
            lineHeight = AppType.CalloutLine
        )
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueColor: Color = AppColors.TextTitle
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = AppType.Callout,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 持仓诊断结果。成本与数量来自用户输入，点位来自 [Stock]。
 *
 * @property cost 成本价
 * @property quantity 持仓数量
 * @property lastPrice 现价
 * @property marketValue 市值
 * @property pnl 浮动盈亏金额
 * @property pnlPercent 浮动盈亏比例
 * @property statusLabel 浮盈 / 浮亏 / 持平
 * @property strategy 与建议同向的操作口径
 * @property breakEven 解套参考（成本）
 * @property support 支撑
 * @property resistance 压力
 * @property buyPoint 建议买入点
 * @property sellPoint 建议卖出点
 */
data class HoldingDiagnosis(
    val cost: Double,
    val quantity: Double,
    val lastPrice: Double,
    val marketValue: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val statusLabel: String,
    val strategy: String,
    val breakEven: Double,
    val support: Double,
    val resistance: Double,
    val buyPoint: Double,
    val sellPoint: Double
)

/**
 * 解析大于 0 的成本或数量。非法、非正、NaN 返回 null。
 */
fun parsePositiveNumber(raw: String): Double? {
    val value = raw.trim().toDoubleOrNull() ?: return null
    if (value <= 0.0 || value.isNaN() || value.isInfinite()) return null
    return value
}

/**
 * 用现价与成本计算盈亏，策略文案与 [Stock.aiAdvice] 同向。
 */
fun diagnoseHolding(stock: Stock, cost: Double, quantity: Double): HoldingDiagnosis? {
    if (cost <= 0.0 || quantity <= 0.0) return null
    val last = stock.price
    val marketValue = last * quantity
    val pnl = (last - cost) * quantity
    val pnlPercent = (last - cost) / cost * 100.0
    val statusLabel = when {
        pnl > 0.0 -> "浮盈"
        pnl < 0.0 -> "浮亏"
        else -> "持平"
    }
    return HoldingDiagnosis(
        cost = cost,
        quantity = quantity,
        lastPrice = last,
        marketValue = marketValue,
        pnl = pnl,
        pnlPercent = pnlPercent,
        statusLabel = statusLabel,
        strategy = holdingStrategy(stock, cost, pnl),
        breakEven = cost,
        support = stock.support,
        resistance = stock.resistance,
        buyPoint = stock.buyPoint,
        sellPoint = stock.sellPoint
    )
}

/**
 * 持仓策略模板。数字来自 [Stock] 与成本，建议口吻与买卖建议一致。
 */
internal fun holdingStrategy(stock: Stock, cost: Double, pnl: Double): String {
    val price = QuoteFormat.price(stock.price)
    val support = QuoteFormat.price(stock.support)
    val resistance = QuoteFormat.price(stock.resistance)
    val buy = QuoteFormat.price(stock.buyPoint)
    val sell = QuoteFormat.price(stock.sellPoint)
    val costText = QuoteFormat.price(cost)
    return when {
        pnl < 0.0 && stock.aiAdvice == Stock.ADVICE_SELL ->
            "现价 $price 低于成本 $costText，建议${stock.aiAdvice}。解套优先看压力 $resistance，不宜在支撑失守后加仓。"
        pnl < 0.0 && stock.aiAdvice == Stock.ADVICE_BUY ->
            "现价 $price 浮亏，支撑 $support、买入点 $buy 附近可分批，止盈看卖出点 $sell。"
        pnl < 0.0 ->
            "现价 $price 浮亏，建议${stock.aiAdvice}。解套看回到成本 $costText，压力 $resistance。"
        pnl > 0.0 && stock.aiAdvice == Stock.ADVICE_SELL ->
            "现价 $price 已有浮盈，建议${stock.aiAdvice}，可在压力 $resistance / 卖出点 $sell 附近兑现。"
        pnl > 0.0 && stock.aiAdvice == Stock.ADVICE_BUY ->
            "现价 $price 浮盈，建议${stock.aiAdvice}，继续持有看卖出点 $sell，回撤关注支撑 $support。"
        pnl > 0.0 ->
            "现价 $price 浮盈，建议${stock.aiAdvice}，压力 $resistance 附近注意锁定。"
        else ->
            "现价 $price 与成本持平，建议${stock.aiAdvice}，支撑 $support、压力 $resistance。"
    }
}
