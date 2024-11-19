package com.redderi.bookreader.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookIdRequest(
        val bookId: Long
)