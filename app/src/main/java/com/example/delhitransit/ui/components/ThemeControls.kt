package com.example.delhitransit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.delhitransit.ui.theme.HighContrastBlack
import com.example.delhitransit.ui.theme.HighContrastWhite
import com.example.delhitransit.ui.theme.HighContrastYellow
import com.example.delhitransit.ui.theme.LocalThemeManager

@Composable
fun ThemeControlsBar() {
    val themeManager = LocalThemeManager.current
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val isHighContrastMode by themeManager.isHighContrastMode.collectAsState()
    val isAutoMode by themeManager.isAutoMode.collectAsState()

    // Animate background color based on high contrast mode
    val bgColor by animateColorAsState(
        targetValue = if (isHighContrastMode) {
            if (isDarkMode) HighContrastBlack else HighContrastWhite
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(durationMillis = 300),
        label = "bgColor"
    )

    // Animate text color based on high contrast mode
    val textColor by animateColorAsState(
        targetValue = if (isHighContrastMode) {
            if (isDarkMode) HighContrastYellow else HighContrastBlack
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High Contrast Mode Toggle
            ThemeToggleButton(
                isActive = isHighContrastMode,
                icon = Icons.Default.Accessibility,
                contentDescription = "High Contrast Mode",
                onClick = { themeManager.toggleHighContrastMode() },
                color = if (isHighContrastMode) HighContrastYellow else MaterialTheme.colorScheme.primary,
                textColor = textColor
            )

            Spacer(modifier = Modifier.width(24.dp))

            // Auto Mode Toggle
            ThemeToggleButton(
                isActive = isAutoMode,
                icon = Icons.Default.Sensors,
                contentDescription = "Auto Mode",
                onClick = { themeManager.toggleAutoMode() },
                color = if (isHighContrastMode) {
                        if (isAutoMode) HighContrastYellow else HighContrastWhite
                } else {
                    MaterialTheme.colorScheme.primary
                },
                textColor = textColor
            )
        }
    }
}

@Composable
fun ThemeToggleButton(
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    color: Color,
    textColor: Color,
    enabled: Boolean = true
) {


    Box(
        modifier = Modifier.padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (isActive) color.copy(alpha = 0.2f) else Color.Transparent
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) color else textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = contentDescription,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = textColor.copy(alpha = if (isActive) 1f else 0.7f),
            modifier = Modifier.padding(top = 40.dp)
        )
    }
}