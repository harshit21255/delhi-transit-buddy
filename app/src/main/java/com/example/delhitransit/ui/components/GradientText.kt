package com.example.delhitransit.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.delhitransit.ui.theme.DelhiBlue
import com.example.delhitransit.ui.theme.DelhiRed

@OptIn(ExperimentalTextApi::class)
@Composable
fun GradientTextSimple(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    startColor: Color = DelhiBlue,
    endColor: Color = DelhiRed
) {
    val gradientColors = listOf(startColor, endColor)

    Text(
        text = text,
        style = TextStyle(
            brush = Brush.horizontalGradient(colors = gradientColors),
            fontSize = fontSize,
            fontWeight = fontWeight
        ),
        modifier = modifier
    )
}