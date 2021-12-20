package ru.dkx86.markus

import java.time.LocalDate

data class Article(
    val title: String,
    val date: LocalDate,
    val type: String,
    val content: String,
    val summary: String,
    val fileName: String
)