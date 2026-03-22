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
    // 防御性处理：确保时间非负
    val safeTimeMs = remainingTimeMs.coerceAtLeast(0L)
    val timeText = formatTime(safeTimeMs)
    val themeState = usePomodoroTheme()
    val isDarkTheme = themeState.isDarkTheme

    val textColor = when {
        isDarkTheme -> FluorescentYellow
        else -> Color.Black
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

/**
 * 格式化时间显示
 *
 * @param milliseconds 毫秒数
 * @return 格式化的时间字符串 "MM:SS"
 */
private fun formatTime(milliseconds: Long): String {
    // 确保输入非负
    val safeMs = milliseconds.coerceAtLeast(0L)
    val totalSeconds = safeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}