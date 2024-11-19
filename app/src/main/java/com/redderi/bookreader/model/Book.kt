package com.redderi.bookreader.model

data class Book(
    val id: Long,
    val title: String,
    val authorName: String,
    val year: Int,
    val rating: Float,
    val filePath: String,
    val coverImagePath: String,
    val tagNames: List<String>
)