package com.example.myandroidapplication.ui.theme

import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * 设计系统色板，HEX 与 `.cursor/rules/design-system.md` 第 2 章一一对应。
 */
object AppColors {
    val BgPage = Color(0xFF0B0E14)
    val BgElevated = Color(0xFF121722)
    val BgCard = Color(0xFF161C28)
    val BgCardAI = Color(0xFF1A2433)
    val BgPress = Color(0xFF1E2736)
    val Divider = Color(0xFF2A3344)
    val Border = Color(0xFF2A3344)
    val Primary = Color(0xFF4C9FFF)
    val PrimaryDim = Color(0x294C9FFF)
    val AI = Color(0xFF5CE1C5)
    val AIDim = Color(0x295CE1C5)
    val Rise = Color(0xFFE74C3C)
    val RiseDim = Color(0x29E74C3C)
    val Fall = Color(0xFF27AE60)
    val FallDim = Color(0x2927AE60)
    val Flat = Color(0xFF8B95A8)
    val FlatDim = Color(0xFF2A3344)
    val Warning = Color(0xFFF5A623)
    val WarningDim = Color(0x29F5A623)
    val Success = Color(0xFF27AE60)
    val Danger = Color(0xFFE74C3C)
    val TextTitle = Color(0xFFF5F7FA)
    val TextSecondary = Color(0xFFC5CDD8)
    val TextBody = Color(0xFFA8B2C1)
    val TextHint = Color(0xFF6B7687)
    val TextDisabled = Color(0xFF3D4654)
    val TextOnAccent = Color(0xFFFFFFFF)
    val TextOnWarning = Color(0xFF0B0E14)
    /** 设计系统 5.6：MA20 折线专用色，色板无对应 Token。 */
    val Ma20 = Color(0xFFB388FF)
    /** 设计系统 5.6：主折线面积起点 `#2E4C9FFF`（约 18%）。 */
    val ChartArea = Color(0x2E4C9FFF)
    /** 设计系统 5.8：底部 Sheet 遮罩 `#0B0E14` 60%。 */
    val SheetScrim = Color(0x990B0E14)
    /** 设计系统 5.8：底部 Sheet 浮起阴影（仅 spot，禁止 ambient）。 */
    val SheetShadow = Color(0x66000000)
}

/**
 * 间距 / 圆角 / 高度，单位 dp，与设计系统第 4 章对齐。
 */
object AppDimens {
    val Space1 = 4.dp
    val Space2 = 8.dp
    val Space3 = 12.dp
    val Space4 = 16.dp
    val Space5 = 20.dp
    val Space6 = 24.dp
    val Space8 = 32.dp
    val RadiusBadge = 4.dp
    val RadiusChip = 14.dp
    val RadiusCard = 12.dp
    val RadiusSheet = 16.dp
    val HeightListItem = 72.dp
    val HeightTicker = 36.dp
    val HeightAppBar = 56.dp
    val HeightButton = 44.dp
    val HeightChip = 28.dp
    val HeightBadge = 24.dp
    val HeightChatEntry = 56.dp
    val HeightChart = 220.dp
    val HeightAiSignalCardMin = 148.dp
    val HeightProgress = 4.dp
    val StrokeDivider = 1.dp
    val StrokeAccent = 3.dp
    val MinTouch = 44.dp
    /** 设计系统 5.8：底部 Sheet `shadow` elevation。 */
    val SheetElevation = 8.dp
    val PriceColumnWidth = 88.dp
    val PercentChipMinWidth = 64.dp
    val PercentChipHeight = 20.dp
}

/**
 * 字号阶梯，与设计系统第 3 章对齐。
 */
object AppType {
    val Display = 32.sp
    val DisplayLine = 40.sp
    val Title = 20.sp
    val TitleLine = 28.sp
    val Headline = 18.sp
    val HeadlineLine = 24.sp
    val Body = 16.sp
    val BodyLine = 24.sp
    val Callout = 14.sp
    val CalloutLine = 22.sp
    val Caption = 12.sp
    val CaptionLine = 16.sp
    val Micro = 10.sp
    val MicroLine = 14.sp
}
