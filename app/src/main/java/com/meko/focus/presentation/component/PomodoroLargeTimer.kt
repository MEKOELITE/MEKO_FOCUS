package com.meko.focus.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meko.focus.domain.model.SessionType
import com.meko.focus.presentation.theme.FluorescentYellow
import com.meko.focus.presentation.theme.usePomodoroTheme

@Composable
fun PomodoroLargeTimer(
    remainingTimeMs: Long,
    sessionType: SessionType,
    modifier: Modifier = Modifier
) {
    val timeText = formatTime(remainingTimeMs)
    val themeState = usePomodoroTheme()
    val isDarkTheme = themeState.isDarkTheme

    val textColor = when {
        isDarkTheme -> {
            // 夜间模式：所有文字都用荧光黄，确保在黑色背景上可见
            FluorescentYellow
        }
        else -> {
            // 日间模式：根据会话类型使用不同颜色
            when (sessionType) {
                SessionType.FOCUS -> Color.Black
                SessionType.SHORT_BREAK -> Color(0xFF4CAF50) // 绿色
                SessionType.LONG_BREAK -> Color(0xFF2196F3)  // 蓝色
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeText,
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}