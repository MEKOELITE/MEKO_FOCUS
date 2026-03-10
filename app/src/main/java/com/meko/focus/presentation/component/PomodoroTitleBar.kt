package com.meko.focus.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meko.focus.R
import com.meko.focus.presentation.theme.usePomodoroTheme

@Composable
fun PomodoroTitleBar(
    modifier: Modifier = Modifier
) {
    val themeState = usePomodoroTheme()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (themeState.isDarkTheme) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
        )

        IconButton(
            onClick = { themeState.toggleTheme() }
        ) {
            Icon(
                imageVector = if (themeState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = stringResource(R.string.theme_switch),
                tint = if (themeState.isDarkTheme) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
            )
        }
    }
}