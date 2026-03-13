package com.meko.focus.util

import com.meko.focus.domain.model.FocusSession
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.Date

class ChartDataAggregatorTest {

    private fun createSession(minutesAgo: Int, durationMinutes: Int = 25): FocusSession {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, -minutesAgo)
        return FocusSession(
            startTime = calendar.time,
            duration = durationMinutes * 60 * 1000L,
            isCompleted = true,
            tag = "测试"
        )
    }

    @Test
    fun `aggregateByDay should group sessions by date`() {
        // 创建两个今天的session
        val today = createSession(0, 25)
        val today2 = createSession(30, 30)

        val sessions = listOf(today, today2)
        val result = ChartDataAggregator.aggregateByDay(sessions)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `aggregateByDay should return empty list for empty input`() {
        val result = ChartDataAggregator.aggregateByDay(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `aggregateByWeek should group sessions by week`() {
        val session1 = createSession(0, 25)  // 今天
        val session2 = createSession(60 * 24, 30)  // 昨天

        val sessions = listOf(session1, session2)
        val result = ChartDataAggregator.aggregateByWeek(sessions)

        // 至少应该有一周的数据
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `getLast7DaysData should return 7 days`() {
        val result = ChartDataAggregator.getLast7DaysData(emptyList())
        assertEquals(7, result.size)
    }

    @Test
    fun `getLast4WeeksData should return 4 weeks`() {
        val result = ChartDataAggregator.getLast4WeeksData(emptyList())
        assertEquals(4, result.size)
    }

    @Test
    fun `getLast52WeeksData should return 52 weeks`() {
        val result = ChartDataAggregator.getLast52WeeksData(emptyList())
        assertEquals(52, result.size)
    }

    @Test
    fun `getYearlyData should group by year`() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        val session = FocusSession(
            startTime = Date(),
            duration = 25 * 60 * 1000L,
            isCompleted = true,
            tag = "测试"
        )

        val result = ChartDataAggregator.getYearlyData(listOf(session))
        assertTrue(result.containsKey(currentYear))
    }

    @Test
    fun `DailyData should calculate duration correctly`() {
        val session = FocusSession(
            startTime = Date(),
            duration = 25 * 60 * 1000L,
            isCompleted = true,
            tag = "测试"
        )

        assertEquals(25, session.durationMinutes)
    }
}
