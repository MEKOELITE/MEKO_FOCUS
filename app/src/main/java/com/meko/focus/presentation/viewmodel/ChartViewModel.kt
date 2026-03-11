package com.meko.focus.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meko.focus.domain.model.FocusSession
import com.meko.focus.domain.repository.FocusSessionRepository
import com.meko.focus.util.ChartDataAggregator
import com.meko.focus.util.ChartDataAggregator.DailyData
import com.meko.focus.util.ChartDataAggregator.WeeklyData
import com.meko.focus.util.ChartDataAggregator.WeeklyHeatmapData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val focusSessionRepository: FocusSessionRepository
) : ViewModel() {

    sealed class ChartType {
        object Weekly : ChartType()
        object Monthly : ChartType()
        object Heatmap : ChartType()
    }

    data class ChartUiState(
        val isLoading: Boolean = true,
        val chartType: ChartType = ChartType.Weekly,
        val dailyData: List<DailyData> = emptyList(),
        val weeklyData: List<WeeklyData> = emptyList(),
        val heatmapData: List<WeeklyHeatmapData> = emptyList(),
        val totalSessions: Int = 0,
        val totalMinutes: Int = 0,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    init {
        loadChartData()
    }

    fun loadChartData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // 获取所有完成的会话
                val sessions = focusSessionRepository.getCompletedSessions()

                // 计算统计数据
                val totalSessions = sessions.size
                val totalMinutes = sessions.sumOf { it.durationMinutes }

                // 获取图表数据
                val dailyData = ChartDataAggregator.getLast7DaysData(sessions)
                val weeklyData = ChartDataAggregator.getLast4WeeksData(sessions)
                val heatmapData = ChartDataAggregator.getLast52WeeksData(sessions)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dailyData = dailyData,
                        weeklyData = weeklyData,
                        heatmapData = heatmapData,
                        totalSessions = totalSessions,
                        totalMinutes = totalMinutes
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "加载图表数据失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun switchChartType(chartType: ChartType) {
        _uiState.update { it.copy(chartType = chartType) }
    }

    fun refreshData() {
        loadChartData()
    }

    // 获取本周的开始和结束日期（用于查询数据库）
    fun getCurrentWeekRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
        }

        // 本周开始（周一）
        val start = calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // 本周结束（周日）
        val end = calendar.apply {
            add(Calendar.DAY_OF_WEEK, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return Pair(start, end)
    }

    // 获取本月的开始和结束日期
    fun getCurrentMonthRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()

        // 本月开始
        val start = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // 本月结束
        val end = calendar.apply {
            set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return Pair(start, end)
    }
}