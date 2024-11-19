package com.redderi.bookreader.utils

import com.redderi.bookreader.model.LoginRequest
import com.redderi.bookreader.model.RegistrationRequest
import com.redderi.bookreader.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun loginUser(username: String, password: String, callback: (Boolean, String) -> Unit) {
    val request = LoginRequest(username, password)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.login(request).execute()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.status == "Success") {
                            callback(true, apiResponse.message)
                        } else {
                            callback(false, "Ошибка авторизации: ${apiResponse.message}")
                        }
                    } ?: callback(false, "Неизвестный ответ")
                } else {
                    callback(false, "Ошибка: неверные данные ${response.message()}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(false, "Ошибка сети: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }
}

fun registerUser(username: String, password: String, callback: (Boolean, String) -> Unit) {
    val request = RegistrationRequest(username, password)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.register(request).execute()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.status == "Success") {
                            callback(true, apiResponse.message)
                        } else {
                            callback(false, "Ошибка регистрации ${apiResponse.message}")
                        }
                    } ?: callback(false, "Неизвестный ответ")
                } else {
                    callback(false, "Ошибка: имя занято ${response.message()}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(false, "Ошибка сети: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }
}