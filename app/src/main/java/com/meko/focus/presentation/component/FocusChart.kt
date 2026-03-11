package com.meko.focus.presentation.component

import android.content.Context
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.meko.focus.presentation.theme.FluorescentYellow
import com.meko.focus.util.ChartDataAggregator.DailyData
import com.meko.focus.util.ChartDataAggregator.WeeklyData
import java.text.SimpleDateFormat
import java.util.Locale
import com.meko.focus.presentation.viewmodel.ChartViewModel
@Composable
fun FocusChart(
    dailyData: List<DailyData>,
    weeklyData: List<WeeklyData>,
    chartType: ChartViewModel.ChartType, // 假设这是一个 Sealed Class
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current

    AndroidView(
        factory = { context ->
            createBarChart(context, isDarkTheme)
        },
        modifier = modifier,
        update = { chart ->
            // 这里是修复的核心
            when (chartType) {
                is ChartViewModel.ChartType.Weekly -> {
                    setupWeeklyChart(chart, dailyData, isDarkTheme)
                }
                is ChartViewModel.ChartType.Monthly -> {
                    setupMonthlyChart(chart, weeklyData, isDarkTheme)
                }
                // 必须处理所有可能的分支，或者加一个 else
                else -> {
                    // 如果是 Heatmap 或其他类型，暂时清空图表或保持原样
                    chart.data = null
                    chart.invalidate()
                }
            }
        }
    )
}

private fun createBarChart(context: Context, isDarkTheme: Boolean): BarChart {
    return BarChart(context).apply {
        // 基本配置
        description.isEnabled = false
        setDrawGridBackground(false)
        setDrawBorders(false)
        setDrawBarShadow(false)
        isDoubleTapToZoomEnabled = false
        setPinchZoom(false)
        setScaleEnabled(false)
        legend.isEnabled = false
        setTouchEnabled(true)
        setNoDataText("暂无数据")
        setNoDataTextColor(if (isDarkTheme) Color.White.toArgb() else Color.Black.toArgb())

        // X轴配置
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = 7
            textColor = if (isDarkTheme) Color.White.toArgb() else Color.Black.toArgb()
            axisLineColor = if (isDarkTheme) Color.White.toArgb() else Color.Black.toArgb()
        }

        // 左侧Y轴配置
        axisLeft.apply {
            setDrawGridLines(true)
            gridColor = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f).toArgb() else Color.LightGray.toArgb()
            textColor = if (isDarkTheme) Color.White.toArgb() else Color.Black.toArgb()
            axisLineColor = if (isDarkTheme) Color.White.toArgb() else Color.Black.toArgb()
            axisMinimum = 0f
            granularity = 30f // 30分钟间隔
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value >= 60) {
                        "${(value / 60).toInt()}h"
                    } else {
                        "${value.toInt()}m"
                    }
                }
            }
        }

        // 右侧Y轴隐藏
        axisRight.isEnabled = false

        // 动画
        animateY(1000)
    }
}

private fun setupWeeklyChart(chart: BarChart, dailyData: List<DailyData>, isDarkTheme: Boolean) {
    if (dailyData.isEmpty()) {
        chart.data = null
        chart.invalidate()
        return
    }

    val entries = mutableListOf<BarEntry>()
    val daysOfWeek = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    // 确保有7天的数据（按顺序）
    for (i in 0 until 7) {
        val dataForDay = dailyData.getOrNull(i)
        val minutes = dataForDay?.totalMinutes?.toFloat() ?: 0f
        entries.add(BarEntry(i.toFloat(), minutes))
    }

    val dataSet = BarDataSet(entries, "专注时长").apply {
        color = if (isDarkTheme) FluorescentYellow.toArgb() else Color.Black.toArgb()
        valueTextColor = if (isDarkTheme) Color.White.toArgb() else Color.Black.toArgb()
        valueTextSize = 12f
        setDrawValues(true)
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value >= 60) {
                    "${(value / 60).toInt()}h"
                } else if (value > 0) {
                    "${value.toInt()}m"
                } else {
                    ""
                }
            }
        }
    }

    val barData = BarData(dataSet).apply {
        barWidth = 0.5f
        setValueFormatter(dataSet.valueFormatter)
    }

    // 设置X轴标签
    chart.xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index in daysOfWeek.indices) {
                daysOfWeek[index]
            } else {
                ""
            }
        }
    }

    chart.data = barData
    chart.invalidate()
}

private fun setupMonthlyChart(chart: BarChart, weeklyData: List<WeeklyData>, isDarkTheme: Boolean) {
    if (weeklyData.isEmpty()) {
        chart.data = null
        chart.invalidate()
        return
    }

    val entries = mutableListOf<BarEntry>()
    val weekLabels = mutableListOf<String>()
    val dateFormat = SimpleDateFormat("MM/dd", Locale.CHINA)

    // 确保有4周的数据
    for (i in 0 until 4) {
        val dataForWeek = weeklyData.getOrNull(i)
        val minutes = dataForWeek?.totalMinutes?.toFloat() ?: 0f
        entries.add(BarEntry(i.toFloat(), minutes))

        val label = if (dataForWeek != null) {
            val calendar = java.util.Calendar.getInstance().apply {
                time = dataForWeek.weekStart
                add(java.util.Calendar.DAY_OF_YEAR, 6)
            }
            "第${i + 1}周"
        } else {
            "第${i + 1}周"
        }
        weekLabels.add(label)
    }

    val dataSet = BarDataSet(entries, "专注时长").apply {
        color = if (isDarkTheme) FluorescentYellow.toArgb() else Color.Black.toArgb()
        valueTextColor = if (isDarkTheme) Color.White.toArgb() else Color.Black.toArgb()
        valueTextSize = 12f
        setDrawValues(true)
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value >= 60) {
                    "${(value / 60).toInt()}h"
                } else if (value > 0) {
                    "${value.toInt()}m"
                } else {
                    ""
                }
            }
        }
    }

    val barData = BarData(dataSet).apply {
        barWidth = 0.5f
        setValueFormatter(dataSet.valueFormatter)
    }

    // 设置X轴标签
    chart.xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index in weekLabels.indices) {
                weekLabels[index]
            } else {
                ""
            }
        }
    }

    chart.data = barData
    chart.invalidate()
}