package com.meko.focus.util

import com.meko.focus.domain.model.FocusSession
import java.util.Calendar
import java.util.Date

object ChartDataAggregator {

    data class DailyData(
        val date: Date,
        val totalMinutes: Int,
        val sessionCount: Int
    )

    data class WeeklyData(
        val weekStart: Date,
        val totalMinutes: Int,
        val sessionCount: Int
    )

    fun aggregateByDay(sessions: List<FocusSession>): List<DailyData> {
        val result = mutableMapOf<String, DailyData>()
        val calendar = Calendar.getInstance()

        // 按日期分组
        for (session in sessions) {
            calendar.time = session.startTime
            // 重置时间为当天的开始（00:00:00）
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val dateKey = calendar.time
            val current = result[dateKey.time.toString()] ?: DailyData(dateKey, 0, 0)

            result[dateKey.time.toString()] = DailyData(
                date = dateKey,
                totalMinutes = current.totalMinutes + session.durationMinutes,
                sessionCount = current.sessionCount + 1
            )
        }

        // 按日期排序
        return result.values.sortedBy { it.date }
    }

    fun aggregateByWeek(sessions: List<FocusSession>): List<WeeklyData> {
        val result = mutableMapOf<String, WeeklyData>()
        val calendar = Calendar.getInstance()

        // 按周分组
        for (session in sessions) {
            calendar.time = session.startTime
            // 设置到当周的开始（周一）
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val weekStart = calendar.time
            val weekKey = weekStart.time.toString()
            val current = result[weekKey] ?: WeeklyData(weekStart, 0, 0)

            result[weekKey] = WeeklyData(
                weekStart = weekStart,
                totalMinutes = current.totalMinutes + session.durationMinutes,
                sessionCount = current.sessionCount + 1
            )
        }

        // 按周开始日期排序
        return result.values.sortedBy { it.weekStart }
    }

    fun getLast7DaysData(sessions: List<FocusSession>): List<DailyData> {
        val aggregated = aggregateByDay(sessions)
        val calendar = Calendar.getInstance()

        // 获取最近7天的数据（包括今天）
        val last7Days = mutableListOf<DailyData>()

        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val targetDate = calendar.time
            val dataForDay = aggregated.find {
                it.date.time == targetDate.time
            } ?: DailyData(targetDate, 0, 0)

            last7Days.add(dataForDay)
        }

        return last7Days
    }

    fun getLast4WeeksData(sessions: List<FocusSession>): List<WeeklyData> {
        val aggregated = aggregateByWeek(sessions)
        val calendar = Calendar.getInstance()

        // 获取最近4周的数据
        val last4Weeks = mutableListOf<WeeklyData>()

        for (i in 3 downTo 0) {
            calendar.time = Date()
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.add(Calendar.WEEK_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val targetWeek = calendar.time
            val dataForWeek = aggregated.find {
                it.weekStart.time == targetWeek.time
            } ?: WeeklyData(targetWeek, 0, 0)

            last4Weeks.add(dataForWeek)
        }

        return last4Weeks
    }
}