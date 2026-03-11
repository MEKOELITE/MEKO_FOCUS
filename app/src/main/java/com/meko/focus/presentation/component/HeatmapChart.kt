package com.meko.focus.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meko.focus.util.ChartDataAggregator.WeeklyHeatmapData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HeatmapChart(
    weeklyData: List<WeeklyHeatmapData>,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val maxMinutes = weeklyData.maxOfOrNull { it.totalMinutes } ?: 1
    val colorScale = if (isDarkTheme) darkThemeColorScale else lightThemeColorScale

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标题
        Text(
            text = "52周专注热力图",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 热力图网格
        HeatmapGrid(
            weeklyData = weeklyData,
            maxMinutes = maxMinutes,
            colorScale = colorScale,
            isDarkTheme = isDarkTheme
        )

        // 图例
        HeatmapLegend(
            maxMinutes = maxMinutes,
            colorScale = colorScale,
            isDarkTheme = isDarkTheme
        )

        // 月份标签
        MonthLabels(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun HeatmapGrid(
    weeklyData: List<WeeklyHeatmapData>,
    maxMinutes: Int,
    colorScale: List<Color>,
    isDarkTheme: Boolean
) {
    // 按月份分组数据以便绘制月份标签
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // 创建52周的网格（13列 x 7行，但实际是52个方块）
    // GitHub风格：每行代表一周中的一天（周一至周日），每列代表一周
    // 简化：按周线性排列，每行7个，共52/7≈7.42行，我们使用8行

    val weeks = weeklyData.sortedBy { it.date }
    val cellSize = 12.dp
    val cellSpacing = 2.dp
    val rows = 7 // 每周7天
    val cols = (weeks.size + rows - 1) / rows // 计算需要的列数

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(cellSpacing)
    ) {
        // 星期标签（周一至周日）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            // 空单元格用于对齐
            Canvas(modifier = Modifier.width(cellSize).height(cellSize)) {}

            val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = if (isDarkTheme) Color.LightGray else Color.Gray,
                    modifier = Modifier
                        .width(cellSize)
                        .height(cellSize)
                )
            }
        }

        // 热力图行
        for (col in 0 until cols) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                // 月份标签（只在每月的第一周显示）
                val weekIndex = col * rows
                val weekDate = weeks.getOrNull(weekIndex)?.date
                val monthLabel = if (weekDate != null) {
                    calendar.time = weekDate
                    val month = calendar.get(Calendar.MONTH) + 1 // 1-12
                    "${month}月"
                } else {
                    ""
                }

                Text(
                    text = monthLabel,
                    fontSize = 10.sp,
                    color = if (isDarkTheme) Color.LightGray else Color.Gray,
                    modifier = Modifier
                        .width(cellSize)
                        .height(cellSize)
                )

                // 每周的每天单元格
                for (row in 0 until rows) {
                    val index = col * rows + row
                    val weekData = weeks.getOrNull(index)

                    val color = if (weekData != null && weekData.totalMinutes > 0) {
                        val intensity = (weekData.totalMinutes.toFloat() / maxMinutes).coerceIn(0f, 1f)
                        getColorForIntensity(intensity, colorScale)
                    } else {
                        if (isDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.3f)
                    }

                    Canvas(
                        modifier = Modifier
                            .width(cellSize)
                            .height(cellSize)
                    ) {
                        drawHeatmapCell(color = color)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegend(
    maxMinutes: Int,
    colorScale: List<Color>,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "较少",
            fontSize = 10.sp,
            color = if (isDarkTheme) Color.LightGray else Color.Gray
        )

        // 图例颜色条
        Row(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            for (i in colorScale.indices) {
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                ) {
                    drawRect(color = colorScale[i])
                }
            }
        }

        Text(
            text = "${maxMinutes}分钟",
            fontSize = 10.sp,
            color = if (isDarkTheme) Color.LightGray else Color.Gray
        )
    }
}

@Composable
private fun MonthLabels(modifier: Modifier = Modifier) {
    // 简化：显示月份缩写
    val months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 实际实现中会根据周数动态显示月份标签
        // 这里简化显示
        Text(
            text = "过去一年",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

private fun DrawScope.drawHeatmapCell(color: Color) {
    val cornerRadius = 2.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset.Zero,
        size = Size(size.width, size.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )
}

private fun getColorForIntensity(intensity: Float, colorScale: List<Color>): Color {
    val index = (intensity * (colorScale.size - 1)).toInt().coerceIn(0, colorScale.size - 1)
    return colorScale[index]
}

private val lightThemeColorScale = listOf(
    Color(0xFFEBEDF0), // 最浅
    Color(0xFF9BE9A8),
    Color(0xFF40C463),
    Color(0xFF30A14E),
    Color(0xFF216E39)  // 最深
)

private val darkThemeColorScale = listOf(
    Color(0xFF2D333B), // 最浅
    Color(0xFF0E4429),
    Color(0xFF006D32),
    Color(0xFF26A641),
    Color(0xFF39D353)  // 最深
)