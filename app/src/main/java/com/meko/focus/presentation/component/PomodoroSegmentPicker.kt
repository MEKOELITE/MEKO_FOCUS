package com.meko.focus.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meko.focus.R

enum class PomodoroSegment {
    FOCUS, BREAK
}

@Composable
fun PomodoroSegmentPicker(
    selectedSegment: PomodoroSegment,
    onSegmentSelected: (PomodoroSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp)),
        // 使用主题提供的 surfaceVariant，增加视觉层次感
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SegmentItem(
                text = stringResource(R.string.focus),
                isSelected = selectedSegment == PomodoroSegment.FOCUS,
                onClick = { onSegmentSelected(PomodoroSegment.FOCUS) },
                modifier = Modifier.weight(1f)
            )

            SegmentItem(
                text = stringResource(R.string.break_time),
                isSelected = selectedSegment == PomodoroSegment.BREAK,
                onClick = { onSegmentSelected(PomodoroSegment.BREAK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SegmentItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp)),
        // 选中时应用主色 (Primary)，未选中保持透明
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                // 必须这样写，不能引用 FocusColor 或 ShortBreakColor
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}