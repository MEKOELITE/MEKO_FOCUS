package com.meko.focus.domain.repository

import com.meko.focus.domain.model.Quote

interface QuoteRepository {
    suspend fun getRandomQuote(): Quote
    suspend fun getAllQuotes(): List<Quote>
}