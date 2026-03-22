package com.meko.focus.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meko.focus.R
import com.meko.focus.presentation.theme.usePomodoroTheme

enum class PomodoroSegment {
    FOCUS, BREAK
}

@Composable
fun PomodoroSegmentPicker(
    selectedSegment: PomodoroSegment,
    onSegmentSelected: (PomodoroSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = usePomodoroTheme()
    val isDark = themeState.isDarkTheme

    val trackColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE8E8E8)
    val pillColor = if (isDark) Color(0xFF3A3A3A) else Color.White
    val selectedText = if (isDark) Color.White else Color.Black
    val unselectedText = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp)),
        color = trackColor,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SegmentSlot(
                text = stringResource(R.string.focus),
                selected = selectedSegment == PomodoroSegment.FOCUS,
                onClick = { onSegmentSelected(PomodoroSegment.FOCUS) },
                pillColor = pillColor,
                selectedTextColor = selectedText,
                unselectedTextColor = unselectedText,
                modifier = Modifier.weight(1f)
            )
            SegmentSlot(
                text = stringResource(R.string.break_time),
                selected = selectedSegment == PomodoroSegment.BREAK,
                onClick = { onSegmentSelected(PomodoroSegment.BREAK) },
                pillColor = pillColor,
                selectedTextColor = selectedText,
                unselectedTextColor = unselectedText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SegmentSlot(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    pillColor: Color,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Surface(
                modifier = Modifier.matchParentSize(),
                shape = RoundedCornerShape(20.dp),
                color = pillColor,
                shadowElevation = 2.dp
            ) {}
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedTextColor else unselectedTextColor
        )
    }
}
