package com.meko.focus.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meko.focus.domain.model.TimerSettings
import com.meko.focus.presentation.theme.usePomodoroTheme
import com.meko.focus.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeState = usePomodoroTheme()
    val isDarkTheme = themeState.isDarkTheme

    BackHandler {
        viewModel.persistAndRun(onNavigateBack)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.persistAndRun(onNavigateBack) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveSettings() },
                        enabled = uiState.hasChanges
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "保存",
                            tint = if (uiState.hasChanges && isDarkTheme) Color.White else Color.Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            color = if (isDarkTheme) Color.Black else Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 计时器设置
                TimerSettingsCard(
                    focusDuration = uiState.draftSettings.focusDurationMinutes,
                    shortBreakDuration = uiState.draftSettings.shortBreakDurationMinutes,
                    longBreakDuration = uiState.draftSettings.longBreakDurationMinutes,
                    onFocusDurationChange = { viewModel.updateFocusDuration(it) },
                    onShortBreakDurationChange = { viewModel.updateShortBreakDuration(it) },
                    onLongBreakDurationChange = { viewModel.updateLongBreakDuration(it) },
                    isDarkTheme = isDarkTheme
                )

                // 通知与声音设置
                NotificationSettingsCard(
                    notificationsEnabled = uiState.draftSettings.notificationsEnabled,
                    soundEnabled = uiState.draftSettings.soundEnabled,
                    vibrationEnabled = uiState.draftSettings.vibrationEnabled,
                    autoSwitch = uiState.draftSettings.autoSwitch,
                    onNotificationsEnabledChange = { viewModel.updateNotificationsEnabled(it) },
                    onSoundEnabledChange = { viewModel.updateSoundEnabled(it) },
                    onVibrationEnabledChange = { viewModel.updateVibrationEnabled(it) },
                    onAutoSwitchChange = { viewModel.updateAutoSwitch(it) },
                    isDarkTheme = isDarkTheme
                )

                // 保存按钮
                if (uiState.hasChanges) {
                    Button(
                        onClick = { viewModel.saveSettings() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("保存设置", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerSettingsCard(
    focusDuration: Int,
    shortBreakDuration: Int,
    longBreakDuration: Int,
    onFocusDurationChange: (Int) -> Unit,
    onShortBreakDurationChange: (Int) -> Unit,
    onLongBreakDurationChange: (Int) -> Unit,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "计时器",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "计时器设置",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }

            DurationMinutesField(
                label = "专注时长 (分钟)",
                minutes = focusDuration,
                min = TimerSettings.MIN_FOCUS_DURATION,
                max = TimerSettings.MAX_FOCUS_DURATION,
                onMinutesCommitted = onFocusDurationChange,
                isDarkTheme = isDarkTheme
            )

            DurationMinutesField(
                label = "短休时长 (分钟)",
                minutes = shortBreakDuration,
                min = TimerSettings.MIN_SHORT_BREAK,
                max = TimerSettings.MAX_SHORT_BREAK,
                onMinutesCommitted = onShortBreakDurationChange,
                isDarkTheme = isDarkTheme
            )

            DurationMinutesField(
                label = "长休时长 (分钟)",
                minutes = longBreakDuration,
                min = TimerSettings.MIN_LONG_BREAK,
                max = TimerSettings.MAX_LONG_BREAK,
                onMinutesCommitted = onLongBreakDurationChange,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun NotificationSettingsCard(
    notificationsEnabled: Boolean,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    autoSwitch: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onAutoSwitchChange: (Boolean) -> Unit,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "通知",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "通知与声音",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }

            SettingSwitch(
                label = "启用通知",
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsEnabledChange,
                isDarkTheme = isDarkTheme
            )

            SettingSwitch(
                label = "启用声音",
                checked = soundEnabled,
                onCheckedChange = onSoundEnabledChange,
                isDarkTheme = isDarkTheme
            )

            SettingSwitch(
                label = "启用振动",
                checked = vibrationEnabled,
                onCheckedChange = onVibrationEnabledChange,
                isDarkTheme = isDarkTheme
            )

            SettingSwitch(
                label = "自动切换",
                description = "计时结束后自动切换到下一个阶段",
                checked = autoSwitch,
                onCheckedChange = onAutoSwitchChange,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationMinutesField(
    label: String,
    minutes: Int,
    min: Int,
    max: Int,
    onMinutesCommitted: (Int) -> Unit,
    isDarkTheme: Boolean
) {
    var text by remember { mutableStateOf(minutes.toString()) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(minutes) {
        if (!isFocused) {
            text = minutes.toString()
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { new ->
            if (new.length <= 3 && new.all { it.isDigit() }) {
                text = new
            }
        },
        label = { Text(label, color = if (isDarkTheme) Color.LightGray else Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (isFocused && !focusState.isFocused) {
                    val parsed = text.toIntOrNull()
                    val committed = if (parsed == null || parsed <= 0) {
                        minutes
                    } else {
                        parsed.coerceIn(min, max)
                    }
                    text = committed.toString()
                    onMinutesCommitted(committed)
                }
                isFocused = focusState.isFocused
            },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
            unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
            cursorColor = if (isDarkTheme) Color.White else Color.Black,
            focusedBorderColor = if (isDarkTheme) Color.White else Color.Black,
            unfocusedBorderColor = if (isDarkTheme) Color.Gray else Color.LightGray,
            focusedLabelColor = if (isDarkTheme) Color.White else Color.Black,
            unfocusedLabelColor = if (isDarkTheme) Color.Gray else Color.LightGray
        )
    )
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color.White else Color.Black
            )
            if (description != null) {
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (isDarkTheme) Color.LightGray else Color.Gray
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}