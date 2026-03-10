package com.meko.focus.data.repository

import com.meko.focus.domain.model.Quote
import com.meko.focus.domain.repository.QuoteRepository
import kotlin.random.Random
import javax.inject.Inject

class QuoteRepositoryImpl @Inject constructor() : QuoteRepository {

    private val quotes = listOf(
        Quote(1, "Gaudium Perenne, Intentio Constans"),
        Quote(2, "专注当下，恒久喜悦"),
        Quote(3, "持之以恒，志向恒定"),
        Quote(4, "深度专注，心流体验"),
        Quote(5, "时间是最宝贵的资源"),
        Quote(6, "一次只做一件事，做到极致"),
        Quote(7, "休息是为了更好地专注"),
        Quote(8, "极简主义，极致专注"),
        Quote(9, "MEKO FOCUS - 恒久喜悦，恒定志向"),
        Quote(10, "每一刻的专注，都在塑造未来")
    )

    override suspend fun getRandomQuote(): Quote {
        return quotes[Random.nextInt(quotes.size)]
    }

    override suspend fun getAllQuotes(): List<Quote> {
        return quotes
    }
}