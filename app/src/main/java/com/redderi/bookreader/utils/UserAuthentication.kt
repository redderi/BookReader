package com.redderi.bookreader.utils

import androidx.navigation.NavController
import com.redderi.bookreader.model.LoginRequest
import com.redderi.bookreader.model.RegistrationRequest
import com.redderi.bookreader.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun loginUser(username: String, password: String, navController: NavController, callback: (Boolean, String) -> Unit) {
    val request = LoginRequest(username, password)

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.login(request).execute()

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    withContext(Dispatchers.Main) {
                        if (apiResponse.status == "success") {
                            callback(true, apiResponse.message)
                        } else {
                            callback(false, "Ошибка: ${apiResponse.message}")
                        }
                    }
                } ?: withContext(Dispatchers.Main) {
                    callback(false, "Неизвестный ответ")
                }
            } else {
                withContext(Dispatchers.Main) {
                    callback(false, "Ошибка: ${response.message()}")
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

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.register(request).execute()

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    when (apiResponse.status) {
                        "success" -> {
                            withContext(Dispatchers.Main) {
                                callback(true, apiResponse.message)
                            }
                        }
                        "error" -> {
                            withContext(Dispatchers.Main) {
                                callback(false, "Ошибка: ${apiResponse.message}")
                            }
                        }
                        else -> {
                            withContext(Dispatchers.Main) {
                                callback(false, "Неизвестный статус: ${apiResponse.status}")
                            }
                        }
                    }
                } ?: withContext(Dispatchers.Main) {
                    callback(false, "Неизвестный ответ")
                }
            } else {
                withContext(Dispatchers.Main) {
                    callback(false, "Ошибка: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(false, "Ошибка сети: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }
}
