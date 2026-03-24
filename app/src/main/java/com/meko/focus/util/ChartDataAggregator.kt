package com.meko.focus.util

import com.meko.focus.domain.model.FocusSession
import java.util.Calendar
import java.util.Date

/**
 * 图表数据聚合工具类
 *
 * 提供多种维度的时间序列数据聚合功能，用于生成统计数据和可视化图表。
 */
object ChartDataAggregator {

    /**
     * 单日数据聚合结果
     *
     * @property date 日期
     * @property totalMinutes 总专注分钟数
     * @property sessionCount 会话数量
     */
    data class DailyData(
        val date: Date,
        val totalMinutes: Int,
        val sessionCount: Int
    )

    /**
     * 单周数据聚合结果
     *
     * @property weekStart 周开始日期（周一）
     * @property totalMinutes 总专注分钟数
     * @property sessionCount 会话数量
     */
    data class WeeklyData(
        val weekStart: Date,
        val totalMinutes: Int,
        val sessionCount: Int
    )

    /**
     * 年度数据聚合结果（用于热力图）
     *
     * @property date 周开始日期
     * @property totalMinutes 总专注分钟数
     * @property sessionCount 会话数量
     */
    data class WeeklyHeatmapData(
        val date: Date,
        val totalMinutes: Int,
        val sessionCount: Int
    )

    /**
     * 按日聚合会话数据
     *
     * @param sessions 专注会话列表
     * @return 按日期分组的数据列表（按日期升序排列）
     */
    fun aggregateByDay(sessions: List<FocusSession>): List<DailyData> {
        val result = mutableMapOf<String, DailyData>()
        val calendar = Calendar.getInstance()

        for (session in sessions) {
            calendar.time = session.startTime
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

        return result.values.sortedBy { it.date }
    }

    /**
     * 按周聚合会话数据
     *
     * @param sessions 专注会话列表
     * @return 按周分组的数据列表（按周开始日期升序排列）
     */
    fun aggregateByWeek(sessions: List<FocusSession>): List<WeeklyData> {
        val result = mutableMapOf<String, WeeklyData>()
        val calendar = Calendar.getInstance()

        for (session in sessions) {
            calendar.time = session.startTime
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

        return result.values.sortedBy { it.weekStart }
    }

    /**
     * 获取最近7天的数据
     *
     * @param sessions 专注会话列表
     * @return 最近7天的每日数据（包含空数据）
     */
    fun getLast7DaysData(sessions: List<FocusSession>): List<DailyData> {
        val aggregated = aggregateByDay(sessions)
        val calendar = Calendar.getInstance()

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

    /**
     * 获取最近4周的数据
     *
     * @param sessions 专注会话列表
     * @return 最近4周的每周数据（包含空数据）
     */
    fun getLast4WeeksData(sessions: List<FocusSession>): List<WeeklyData> {
        val aggregated = aggregateByWeek(sessions)
        val calendar = Calendar.getInstance()

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

    /**
     * 获取最近52周的数据（用于 GitHub 风格热力图）
     *
     * @param sessions 专注会话列表
     * @return 最近52周的每周数据（包含空数据）
     */
    fun getLast52WeeksData(sessions: List<FocusSession>): List<WeeklyHeatmapData> {
        val aggregated = aggregateByWeek(sessions)
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        val last52Weeks = mutableListOf<WeeklyHeatmapData>()

        for (i in 51 downTo 0) {
            calendar.time = Date()
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

            last52Weeks.add(
                WeeklyHeatmapData(
                    date = targetWeek,
                    totalMinutes = dataForWeek.totalMinutes,
                    sessionCount = dataForWeek.sessionCount
                )
            )
        }

        return last52Weeks
    }

    /**
     * 按年聚合会话数据
     *
     * @param sessions 专注会话列表
     * @return Map<年份, 总分钟数>
     */
    fun getYearlyData(sessions: List<FocusSession>): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val calendar = Calendar.getInstance()

        for (session in sessions) {
            calendar.time = session.startTime
            val year = calendar.get(Calendar.YEAR)
            val current = result[year] ?: 0
            result[year] = current + session.durationMinutes
        }

        return result
    }

    /**
     * 按月聚合指定年份的会话数据
     *
     * @param sessions 专注会话列表
     * @param year 年份
     * @return Map<月份(0-11), 总分钟数>
     */
    fun getMonthlyData(sessions: List<FocusSession>, year: Int): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val calendar = Calendar.getInstance()

        for (session in sessions) {
            calendar.time = session.startTime
            val sessionYear = calendar.get(Calendar.YEAR)
            if (sessionYear == year) {
                val month = calendar.get(Calendar.MONTH)
                val current = result[month] ?: 0
                result[month] = current + session.durationMinutes
            }
        }

        return result
    }
}