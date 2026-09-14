package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.LinearProgressIndicator
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 详情页舆情情绪：温度 0–100 + 正/中/负面新闻计数。
 * 数据取自 [Stock.sentimentScore] 与 news* 字段，须与涨跌情景同向。
 *
 * @param stock 当前个股
 */
@Composable
fun SentimentMeter(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    val score = stock.sentimentScore.coerceIn(0, 100)
    AICardFrame(modifier = modifier) {
            Text(
                text = "市场情绪",
                color = AppColors.TextSecondary,
                fontSize = AppType.Body,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(AppDimens.Space3))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$score",
                    color = AppColors.AI,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = " 情绪温度",
                    color = AppColors.TextHint,
                    fontSize = AppType.Caption,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(AppDimens.Space2))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.HeightProgress)
                    .clip(RoundedCornerShape(2.dp)),
                color = AppColors.Primary,
                trackColor = AppColors.Divider
            )
            Spacer(modifier = Modifier.height(AppDimens.Space3))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NewsCountLabel(label = "正面", count = stock.newsPositive, color = AppColors.Rise)
                NewsCountLabel(label = "中性", count = stock.newsNeutral, color = AppColors.Flat)
                NewsCountLabel(label = "负面", count = stock.newsNegative, color = AppColors.Fall)
            }
    }
}

@Composable
private fun NewsCountLabel(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = AppColors.TextHint,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = " $count",
            color = color,
            fontSize = AppType.Caption,
            fontWeight = FontWeight.Medium
        )
    }
}
