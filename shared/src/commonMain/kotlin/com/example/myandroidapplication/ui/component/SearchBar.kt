package com.example.myandroidapplication.ui.component

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.example.myandroidapplication.ui.theme.AppType
import com.tencent.kuikly.compose.extension.placeHolder
import com.tencent.kuikly.compose.extension.setProp
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.TextField
import com.tencent.kuikly.compose.material3.TextFieldDefaults
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp

/** Material 搜索框标准高度；大于设计系统 [AppDimens.MinTouch] 44dp，避免 14sp 文字被裁。 */
private val SearchBarHeight = 48.dp

/**
 * 选股页搜索框。外层 [Box] 固定 48dp 并垂直居中；输入与占位左右各 12dp。
 *
 * @param value 当前关键词
 * @param onValueChange 仅更新本地输入，不要在此请求 Repository
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索股票名称或代码"
) {
    val shape = RoundedCornerShape(AppDimens.RadiusCard)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SearchBarHeight)
            .clip(shape)
            .background(AppColors.BgCard, shape)
            .border(AppDimens.StrokeDivider, AppColors.Border, shape),
        contentAlignment = Alignment.CenterStart
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.Space3)
                .placeHolder(placeholder, AppColors.TextHint)
                .setProp("maxTextLength", 40),
            textStyle = TextStyle(
                color = AppColors.TextTitle,
                fontSize = AppType.Callout,
                lineHeight = AppType.CalloutLine,
                fontWeight = FontWeight.Normal
            ),
            singleLine = true,
            maxLines = 1,
            shape = shape,
            colors = TextFieldDefaults.colors(
                focusedTextColor = AppColors.TextTitle,
                unfocusedTextColor = AppColors.TextTitle,
                disabledTextColor = AppColors.TextDisabled,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = AppColors.Primary
            )
        )
    }
}
