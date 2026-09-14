package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.chat.ChatReply
import com.example.myandroidapplication.ui.chat.PresetQuestions
import com.example.myandroidapplication.ui.chat.answerFollowUp
import com.example.myandroidapplication.ui.chat.answerQuestion
import com.example.myandroidapplication.ui.chat.followUpQuestions
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.extension.placeHolder
import com.tencent.kuikly.compose.extension.setProp
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.LinearProgressIndicator
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TextField
import com.tencent.kuikly.compose.material3.TextFieldDefaults
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.timer.Timer

/**
 * 个股问答底部 Sheet。视觉规格见设计系统 5.8 节。
 *
 * Kuikly 2.7.0 的 [ModalBottomSheet] 只有 `visible` / `onDismissRequest` / `containerColor` /
 * `scrimColor` 等参数，禁止传入 `sheetState`、`dragHandle`、`dismissOnDrag`。
 *
 * @param stock 当前个股，回答必须与其行情/建议/风险一致
 * @param sheetHeight 页面高度 × 0.72 后夹在 480–640dp
 * @param onDismissRequest 关闭 Sheet
 */
@Composable
fun AIChatSheet(
    stock: Stock,
    sheetHeight: Dp,
    onDismissRequest: () -> Unit
) {
    QuoteBottomSheet(
        sheetHeight = sheetHeight,
        onDismissRequest = onDismissRequest
    ) {
        ChatSheetBody(
            stock = stock,
            onClose = onDismissRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatSheetBody(
    stock: Stock,
    onClose: () -> Unit
) {
    var messages by remember(stock.symbol) { mutableStateOf(listOf<ChatLine>()) }
    var input by remember(stock.symbol) { mutableStateOf("") }
    var loading by remember(stock.symbol) { mutableStateOf(false) }
    var sendingPreset by remember(stock.symbol) { mutableStateOf<String?>(null) }
    var pendingQuestion by remember(stock.symbol) { mutableStateOf<String?>(null) }
    var pendingPrevious by remember(stock.symbol) { mutableStateOf<String?>(null) }
    var requestId by remember(stock.symbol) { mutableStateOf(0) }
    var nextId by remember(stock.symbol) { mutableStateOf(0) }
    val listState = rememberLazyListState()

    fun send(text: String, preset: String?, previousQuestion: String? = null) {
        val question = text.trim()
        if (question.isEmpty() || loading) {
            return
        }
        loading = true
        sendingPreset = preset
        input = ""
        val userId = nextId
        val loadId = nextId + 1
        nextId += 2
        messages = messages + ChatLine.User(userId, question) + ChatLine.Loading(loadId)
        pendingQuestion = question
        pendingPrevious = previousQuestion
        requestId += 1
        listState.requestScrollToItem(messages.lastIndex)
    }

    DisposableEffect(stock.symbol, requestId) {
        val question = pendingQuestion
        if (question == null) {
            return@DisposableEffect onDispose { }
        }
        val timer = Timer()
        timer.schedule(delay = 700, period = 10_000) {
            timer.cancel()
            val withoutLoading = messages.filter { it !is ChatLine.Loading }
            messages = try {
                val previous = pendingPrevious
                val chosen = AppContainer.riskPreferenceChosen
                val preference = AppContainer.riskPreference
                val reply = if (previous.isNullOrBlank()) {
                    if (chosen) {
                        answerQuestion(stock, question, preference)
                    } else {
                        answerQuestion(stock, question)
                    }
                } else {
                    if (chosen) {
                        answerFollowUp(stock, question, previous, preference)
                    } else {
                        answerFollowUp(stock, question, previous)
                    }
                }
                withoutLoading + ChatLine.Ai(nextId, reply, question).also { nextId += 1 }
            } catch (_: Throwable) {
                withoutLoading + ChatLine.Failed(nextId).also { nextId += 1 }
            }
            loading = false
            sendingPreset = null
            pendingQuestion = null
            pendingPrevious = null
            val last = messages.lastIndex
            if (last >= 0) {
                listState.requestScrollToItem(last)
            }
        }
        onDispose { timer.cancel() }
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
                text = "问 AI",
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
        if (messages.isEmpty()) {
            EmptyChatHint(modifier = Modifier.weight(1f).fillMaxWidth())
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(
                    horizontal = AppDimens.Space4,
                    vertical = AppDimens.Space3
                ),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Space2),
                beyondBoundsItemCount = 3
            ) {
                items(messages, key = { it.id }) { line ->
                    when (line) {
                        is ChatLine.User -> UserBubble(text = line.text)
                        is ChatLine.Loading -> LoadingBubble()
                        is ChatLine.Failed -> FailedBubble()
                        is ChatLine.Ai -> {
                            AIBubble(reply = line.reply)
                            val lastAi = messages.lastOrNull { it is ChatLine.Ai } as? ChatLine.Ai
                            if (!loading && lastAi?.id == line.id) {
                                FollowUpSuggestions(
                                    questions = followUpQuestions(stock, line.question),
                                    enabled = !loading,
                                    selected = sendingPreset,
                                    onClick = { followUp ->
                                        send(followUp, followUp, previousQuestion = line.question)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.MinTouch),
            contentPadding = PaddingValues(horizontal = AppDimens.Space4),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2),
            verticalAlignment = Alignment.CenterVertically,
            beyondBoundsItemCount = 3
        ) {
            items(PresetQuestions, key = { it }) { question ->
                PresetQuestionChip(
                    text = question,
                    selected = sendingPreset == question,
                    enabled = !loading,
                    onClick = { send(question, question) }
                )
            }
        }
        Spacer(modifier = Modifier.height(AppDimens.Space2))
        ChatInputBar(
            value = input,
            enabled = !loading,
            onValueChange = { input = it },
            onSend = { send(input, null) }
        )
    }
}

@Composable
private fun EmptyChatHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "选择下方问题，或输入后提问",
                color = AppColors.TextHint,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(AppDimens.Space2))
            Text(
                text = "回答基于当前行情数据",
                color = AppColors.TextDisabled,
                fontSize = AppType.Caption,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * 最近一条 AI 回答下的追问。最多 3 个，复用 [PresetQuestionChip]，禁止新依赖。
 */
@Composable
private fun FollowUpSuggestions(
    questions: List<String>,
    enabled: Boolean,
    selected: String?,
    onClick: (String) -> Unit
) {
    val visible = questions.take(3)
    if (visible.isEmpty()) {
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppDimens.Space1),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Space1)
    ) {
        Text(
            text = "你可能还想问",
            color = AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal
        )
        visible.forEach { question ->
            PresetQuestionChip(
                text = question,
                selected = selected == question,
                enabled = enabled,
                onClick = { onClick(question) }
            )
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .background(
                    AppColors.Primary,
                    RoundedCornerShape(
                        topStart = AppDimens.RadiusCard,
                        topEnd = AppDimens.RadiusCard,
                        bottomEnd = AppDimens.RadiusBadge,
                        bottomStart = AppDimens.RadiusCard
                    )
                )
                .padding(horizontal = AppDimens.Space3, vertical = AppDimens.Space2)
        ) {
            Text(
                text = text,
                color = AppColors.TextOnAccent,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal,
                lineHeight = AppType.CalloutLine
            )
        }
    }
}

@Composable
private fun AIBubble(reply: ChatReply) {
    val shape = RoundedCornerShape(
        topStart = AppDimens.RadiusCard,
        topEnd = AppDimens.RadiusCard,
        bottomEnd = AppDimens.RadiusCard,
        bottomStart = AppDimens.RadiusBadge
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .background(AppColors.BgCardAI, shape)
                .border(AppDimens.StrokeDivider, AppColors.Border, shape)
                .padding(horizontal = AppDimens.Space3, vertical = AppDimens.Space2)
        ) {
            Text(
                text = reply.conclusion,
                color = AppColors.TextTitle,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                lineHeight = AppType.CalloutLine,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(AppDimens.Space2))
            Text(
                text = reply.reason,
                color = AppColors.TextBody,
                fontSize = AppType.Callout,
                fontWeight = FontWeight.Normal,
                lineHeight = AppType.CalloutLine,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(AppDimens.Space2))
            RiskGhostBadge(risk = reply.riskLevel)
        }
    }
}

@Composable
private fun LoadingBubble() {
    val shape = RoundedCornerShape(
        topStart = AppDimens.RadiusCard,
        topEnd = AppDimens.RadiusCard,
        bottomEnd = AppDimens.RadiusCard,
        bottomStart = AppDimens.RadiusBadge
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .height(48.dp)
                .background(AppColors.BgCardAI, shape)
                .border(AppDimens.StrokeDivider, AppColors.Border, shape)
                .padding(horizontal = AppDimens.Space3),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "正在分析这只股票…",
                color = AppColors.TextHint,
                fontSize = AppType.Caption,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(AppDimens.Space1))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.HeightProgress),
                color = AppColors.Primary,
                trackColor = AppColors.Divider
            )
        }
    }
}

@Composable
private fun FailedBubble() {
    val shape = RoundedCornerShape(
        topStart = AppDimens.RadiusCard,
        topEnd = AppDimens.RadiusCard,
        bottomEnd = AppDimens.RadiusCard,
        bottomStart = AppDimens.RadiusBadge
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .height(48.dp)
                .background(AppColors.BgCardAI, shape)
                .border(AppDimens.StrokeDivider, AppColors.Border, shape)
                .padding(horizontal = AppDimens.Space3),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "暂时无法分析，请换一个预设问题",
                color = AppColors.Warning,
                fontSize = AppType.Caption,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val canSend = enabled && value.isNotBlank()
    val fieldShape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.HeightChatEntry)
            .background(AppColors.BgElevated)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.StrokeDivider)
                .background(AppColors.Divider)
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimens.Space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Space2)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .height(AppDimens.MinTouch)
                    .clip(fieldShape)
                    .background(AppColors.BgPage, fieldShape)
                    .placeHolder("输入关于这只股票的问题", AppColors.TextHint)
                    .setProp("maxTextLength", 120),
                enabled = enabled,
                textStyle = TextStyle(
                    color = AppColors.TextTitle,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Normal
                ),
                singleLine = true,
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
            Box(
                modifier = Modifier
                    .size(AppDimens.MinTouch)
                    .clickable(
                        enabled = canSend,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSend
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "发送",
                    color = if (canSend) AppColors.Primary else AppColors.TextDisabled,
                    fontSize = AppType.Callout,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private sealed class ChatLine {
    abstract val id: Int

    data class User(override val id: Int, val text: String) : ChatLine()
    data class Loading(override val id: Int) : ChatLine()
    data class Failed(override val id: Int) : ChatLine()
    data class Ai(override val id: Int, val reply: ChatReply, val question: String) : ChatLine()
}
