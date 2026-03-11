package com.meko.focus.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meko.focus.MainActivity
import com.meko.focus.presentation.component.PomodoroFullWidthButton
import com.meko.focus.presentation.component.PomodoroLargeTimer
import com.meko.focus.presentation.component.PomodoroSegmentPicker
import com.meko.focus.presentation.component.PomodoroTitleBar
import com.meko.focus.presentation.viewmodel.TimerViewModel

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    onNavigateToChart: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val onPictureInPictureClick = {
        if (context is MainActivity) {
            context.enterPictureInPictureMode()
        }
    }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 顶部标题栏
        PomodoroTitleBar(
            onChartClick = onNavigateToChart,
            onSettingsClick = onNavigateToSettings,
            onPictureInPictureClick = onPictureInPictureClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 分段选择器
        PomodoroSegmentPicker(
            selectedSegment = uiState.selectedSegment,
            onSegmentSelected = { viewModel.selectSegment(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 超大计时器显示（占据屏幕1/3）
        PomodoroLargeTimer(
            remainingTimeMs = uiState.remainingTimeMs,
            sessionType = uiState.sessionType,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 全宽控制按钮
        PomodoroFullWidthButton(
            timerState = uiState.timerState,
            sessionType = uiState.sessionType,
            onClick = { viewModel.toggleTimer() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}