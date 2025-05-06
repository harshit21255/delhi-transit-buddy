package com.example.delhitransit.ui.theme

import androidx.compose.ui.graphics.Color

// High Contrast Colors
val HighContrastWhite = Color(0xFFFFFFFF)
val HighContrastBlack = Color(0xFF000000)
val HighContrastYellow = Color(0xFFFFFF00)
val HighContrastBlue = Color(0xFF0000FF)
val HighContrastRed = Color(0xFFFF0000)
val HighContrastGreen = Color(0xFF00FF00)
val HighContrastOrange = Color(0xFFFF7F00)
val HighContrastPurple = Color(0xFF9F00FF)

// Metro line high contrast colors mapping
fun getHighContrastLineColor(line: String): Color {
    return when (line.lowercase()) {
        "yellow" -> HighContrastYellow
        "blue" -> HighContrastBlue
        "red" -> HighContrastRed
        "green" -> HighContrastGreen
        "violet" -> HighContrastPurple
        "orange" -> HighContrastOrange
        "magenta" -> HighContrastPurple
        "pink" -> HighContrastRed.copy(alpha = 0.7f)
        "aqua" -> HighContrastBlue.copy(alpha = 0.7f)
        "grey" -> Color.LightGray
        "rapid" -> HighContrastBlue
        "greenbranch" -> HighContrastGreen
        "bluebranch" -> HighContrastBlue
        "pinkbranch" -> HighContrastRed.copy(alpha = 0.7f)
        else -> Color.Gray
    }
}