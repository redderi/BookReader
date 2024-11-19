package com.redderi.bookreader.model

data class PasswordChangeRequest(
    val oldPassword: String,
    val newPassword: String
)