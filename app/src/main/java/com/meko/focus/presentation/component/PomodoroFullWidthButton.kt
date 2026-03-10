package com.meko.focus.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meko.focus.R
import com.meko.focus.domain.model.SessionType
import com.meko.focus.domain.model.TimerState

@Composable
fun PomodoroFullWidthButton(
    timerState: TimerState,
    sessionType: SessionType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonText = when (timerState) {
        TimerState.STOPPED -> when (sessionType) {
            SessionType.FOCUS -> stringResource(R.string.start_focus)
            SessionType.SHORT_BREAK -> stringResource(R.string.start_focus)
            SessionType.LONG_BREAK -> stringResource(R.string.start_focus)
        }
        TimerState.RUNNING -> stringResource(R.string.pause)
        TimerState.PAUSED -> stringResource(R.string.resume)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        )
    ) {
        Text(
            text = buttonText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}