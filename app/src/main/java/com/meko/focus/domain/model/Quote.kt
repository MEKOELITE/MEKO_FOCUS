package com.meko.focus.domain.model

data class Quote(
    val id: Int,
    val text: String,
    val author: String? = null
)