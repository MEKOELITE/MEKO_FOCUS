package com.meko.focus.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meko.focus.presentation.component.FocusChart
import com.meko.focus.presentation.theme.usePomodoroTheme
import com.meko.focus.presentation.viewmodel.ChartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeState = usePomodoroTheme()
    val isDarkTheme = themeState.isDarkTheme

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (isDarkTheme) Color.Black else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                }

                Text(
                    text = "专注统计",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )

                // 占位符，保持对称
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 统计卡片
            StatsCard(
                totalSessions = uiState.totalSessions,
                totalMinutes = uiState.totalMinutes,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 图表类型切换
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = uiState.chartType is ChartViewModel.ChartType.Weekly,
                    onClick = { viewModel.switchChartType(ChartViewModel.ChartType.Weekly) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = if (isDarkTheme) Color.White else Color.Black,
                        activeContentColor = if (isDarkTheme) Color.Black else Color.White,
                        inactiveContainerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray,
                        inactiveContentColor = if (isDarkTheme) Color.White else Color.Black
                    )
                ) {
                    Text("本周")
                }
                SegmentedButton(
                    selected = uiState.chartType is ChartViewModel.ChartType.Monthly,
                    onClick = { viewModel.switchChartType(ChartViewModel.ChartType.Monthly) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = if (isDarkTheme) Color.White else Color.Black,
                        activeContentColor = if (isDarkTheme) Color.Black else Color.White,
                        inactiveContainerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray,
                        inactiveContentColor = if (isDarkTheme) Color.White else Color.Black
                    )
                ) {
                    Text("本月")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 刷新按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { viewModel.refreshData() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 图表区域
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "加载图表数据...",
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error ?: "加载失败",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.refreshData() }
                    ) {
                        Text("重试")
                    }
                }
            } else {
                FocusChart(
                    dailyData = uiState.dailyData,
                    weeklyData = uiState.weeklyData,
                    chartType = uiState.chartType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    isDarkTheme = isDarkTheme
                )
            }

            // 图表说明
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (uiState.chartType) {
                    is ChartViewModel.ChartType.Weekly -> "显示最近7天的专注时长"
                    is ChartViewModel.ChartType.Monthly -> "显示最近4周的专注时长"
                },
                fontSize = 12.sp,
                color = if (isDarkTheme) Color.LightGray else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatsCard(
    totalSessions: Int,
    totalMinutes: Int,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 总番茄数
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalSessions",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                Text(
                    text = "完成番茄",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) Color.LightGray else Color.Gray
                )
            }

            // 分隔线
            Surface(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp),
                color = if (isDarkTheme) Color.Gray else Color.LightGray
            ) {}

            // 总时长
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                val displayText = if (hours > 0) {
                    "${hours}小时${minutes}分钟"
                } else {
                    "${minutes}分钟"
                }

                Text(
                    text = displayText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "累计专注",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) Color.LightGray else Color.Gray
                )
            }
        }
    }
}