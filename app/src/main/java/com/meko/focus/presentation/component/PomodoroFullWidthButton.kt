package com.meko.focus.presentation.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meko.focus.R
import com.meko.focus.domain.model.SessionType
import com.meko.focus.domain.model.TimerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun PomodoroFullWidthButton(
    timerState: TimerState,
    sessionType: SessionType,
    onClick: () -> Unit,
    onLongPressAbandon: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val buttonText = when (timerState) {
        TimerState.STOPPED -> when (sessionType) {
            SessionType.FOCUS -> stringResource(R.string.start_focus)
            SessionType.SHORT_BREAK -> stringResource(R.string.start_break)
            SessionType.LONG_BREAK -> stringResource(R.string.start_break)
        }
        TimerState.RUNNING -> stringResource(R.string.pause)
        TimerState.PAUSED -> stringResource(R.string.resume)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(28.dp))
                .pointerInput(onClick, onLongPressAbandon, scope) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        val longPressFired = AtomicBoolean(false)
                        val job = scope.launch {
                            delay(1500)
                            longPressFired.set(true)
                            onLongPressAbandon()
                        }
                        waitForUpOrCancellation()
                        job.cancel()
                        if (!longPressFired.get()) {
                            onClick()
                        }
                    }
                },
            shape = RoundedCornerShape(28.dp),
            color = Color.Black,
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Text(
            text = stringResource(R.string.long_press_abandon),
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            fontSize = 12.sp,
            color = Color(0xFF9E9E9E),
            textAlign = TextAlign.Center
        )
    }
}
