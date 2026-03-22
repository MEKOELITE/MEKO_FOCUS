package com.meko.focus.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meko.focus.R
import com.meko.focus.presentation.component.PomodoroFullWidthButton
import com.meko.focus.presentation.component.PomodoroLargeTimer
import com.meko.focus.presentation.component.PomodoroSegmentPicker
import com.meko.focus.presentation.component.TimerHomeHeader
import com.meko.focus.presentation.theme.usePomodoroTheme
import com.meko.focus.presentation.viewmodel.TimerViewModel

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    onNavigateToChart: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeState = usePomodoroTheme()
    val muted = if (themeState.isDarkTheme) Color(0xFF9E9E9E) else Color(0xFF9E9E9E)
    val dot = if (themeState.isDarkTheme) Color(0xFF757575) else Color(0xFFBDBDBD)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimerHomeHeader(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        PomodoroSegmentPicker(
            selectedSegment = uiState.selectedSegment,
            onSegmentSelected = { viewModel.selectSegment(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        PomodoroLargeTimer(
            remainingTimeMs = uiState.remainingTimeMs,
            sessionType = uiState.sessionType,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PomodoroFullWidthButton(
            timerState = uiState.timerState,
            sessionType = uiState.sessionType,
            onClick = { viewModel.toggleTimer() },
            onLongPressAbandon = { viewModel.resetTimer() },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateToChart) {
                Text(
                    text = stringResource(R.string.stats_link),
                    color = muted,
                    fontSize = 13.sp
                )
            }
            Text(text = " · ", color = dot, fontSize = 13.sp)
            TextButton(onClick = onNavigateToSettings) {
                Text(
                    text = stringResource(R.string.settings_link),
                    color = muted,
                    fontSize = 13.sp
                )
            }
        }
    }
}
