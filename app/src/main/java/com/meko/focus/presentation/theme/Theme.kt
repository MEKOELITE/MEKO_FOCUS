package com.meko.focus.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 定义你想要的荧光黄 (Lime Green / Fluorescent Yellow)


// Theme.kt 内部的颜色分配

// 浅色主题：背景白，字黑
private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    surface = White,
    onSurface = Black,      // 日间模式文字为黑色
    background = White,
    onBackground = Black
)

// 深色主题：背景黑，字是荧光黄
private val DarkColorScheme = darkColorScheme(
    primary = White,        // 选中状态的背景（白色胶囊）
    onPrimary = Black,      // 选中状态的文字（黑字）
    surface = Black,
    onSurface = FluorescentYellow,   // ⭐ 关键：未选中文字统一为荧光黄
    background = Black,
    onBackground = FluorescentYellow // 屏幕其他地方的文字也跟随
)
// 自定义主题状态
data class PomodoroThemeState(
    val isDarkTheme: Boolean = false,
    val toggleTheme: () -> Unit = {}
)

val LocalPomodoroThemeState = staticCompositionLocalOf { PomodoroThemeState() }

@Composable
fun PomodoroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 监听系统切换，确保状态同步
    var isDarkThemeState by remember(darkTheme) { mutableStateOf(darkTheme) }

    val colorScheme = if (isDarkThemeState) DarkColorScheme else LightColorScheme

    val themeState = remember(isDarkThemeState) {
        PomodoroThemeState(
            isDarkTheme = isDarkThemeState,
            toggleTheme = { isDarkThemeState = !isDarkThemeState }
        )
    }

    CompositionLocalProvider(LocalPomodoroThemeState provides themeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun usePomodoroTheme(): PomodoroThemeState {
    return LocalPomodoroThemeState.current
}