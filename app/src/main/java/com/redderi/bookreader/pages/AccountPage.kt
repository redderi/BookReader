package com.redderi.bookreader.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.redderi.bookreader.model.ApiResponse
import com.redderi.bookreader.model.PasswordChangeRequest
import com.redderi.bookreader.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.navigation.NavHostController

class NativeColorGenerator {
    init {
        try {
            System.loadLibrary("native-lib")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("NativeColorGenerator", "Ошибка загрузки библиотеки: ${e.message}")
        }
    }
    external fun generateRandomColor(username: String): FloatArray
}

fun generateRandomColor(username: String): Color {
    return try {
        val nativeColorGenerator = NativeColorGenerator()
        val colorArray = nativeColorGenerator.generateRandomColor(username)
        Color(colorArray[0], colorArray[1], colorArray[2])
    } catch (e: Exception) {
        Log.e("NativeColorGenerator", "Ошибка при генерации цвета: ${e.message}")
        Color.Gray
    }
}

@Composable
fun AccountPage(
    username: String,
    navController: NavHostController,
) {
    var showPasswordField by remember { mutableStateOf(false) }
    var showConfirmationMessage by remember { mutableStateOf("") }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var actionToConfirm by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val color = generateRandomColor(username)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.first().toString().uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = username,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Button(
            onClick = { showPasswordField = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Изменить пароль")
        }

        Button(
            onClick = {
                actionToConfirm = "logout"
                showConfirmationDialog = true
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Сменить аккаунт")
        }

        Button(
            onClick = {
                actionToConfirm = "delete"
                showConfirmationDialog = true
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Удалить аккаунт")
        }

        if (showConfirmationMessage.isNotEmpty()) {
            Text(text = showConfirmationMessage, color = Color.Green, style = MaterialTheme.typography.bodyMedium)
        }

        if (showPasswordField) {
            PasswordChangeDialog(
                onConfirm = { oldPassword, newPassword ->
                    changePassword(username, oldPassword, newPassword) { resultMessage ->
                        showConfirmationMessage = resultMessage
                        showPasswordField = false
                    }
                },
                onDismiss = { showPasswordField = false }
            )
        }

        if (showConfirmationDialog) {
            ConfirmationDialog(
                action = actionToConfirm,
                onConfirm = {
                    try {
                        when (actionToConfirm) {
                            "logout" -> {
                                Log.d("AccountPage", "Выход из аккаунта")
                                navController.navigate("auth") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                            "delete" -> deleteUser(username) { resultMessage ->
                                showConfirmationMessage = resultMessage
                                navController.navigate("auth") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AccountPage", "Ошибка при выходе: ${e.message}")
                        showConfirmationMessage = "Ошибка при выходе из аккаунта"
                    }
                    showConfirmationDialog = false
                },
                onDismiss = { showConfirmationDialog = false }
            )
        }
    }
}

@Composable
fun ConfirmationDialog(action: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Подтверждение") },
        text = {
            Text(
                text = when (action) {
                    "logout" -> "Вы уверены, что хотите сменить аккаунт?"
                    "delete" -> "Вы уверены, что хотите удалить аккаунт?"
                    else -> ""
                }
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Подтвердить")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun PasswordChangeDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Изменить пароль",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                TextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Старый пароль") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Новый пароль") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f) // Кнопка будет растягиваться на равную ширину
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = { onConfirm(oldPassword, newPassword) },
                        modifier = Modifier.weight(1f) // Кнопка будет растягиваться на равную ширину
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

fun changePassword(username: String, oldPassword: String, newPassword: String, onResult: (String) -> Unit) {
    val request = PasswordChangeRequest(oldPassword, newPassword)
    RetrofitClient.apiService.changePassword(username, request).enqueue(object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            if (response.isSuccessful) {
                val resultMessage = response.body()?.message ?: "Неизвестная ошибка"
                onResult(resultMessage)
            } else {
                onResult("Ошибка при изменении пароля: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Log.e("ChangePassword", "Ошибка сети", t)
            onResult("Ошибка сети: ${t.message}")
        }
    })
}

fun deleteUser(username: String, onResult: (String) -> Unit) {
    RetrofitClient.apiService.deleteUser(username).enqueue(object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            if (response.isSuccessful) {
                val resultMessage = response.body()?.message ?: "Неизвестная ошибка"
                onResult(resultMessage)
            } else {
                onResult("Ошибка при удалении аккаунта: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Log.e("DeleteUser", "Ошибка сети", t)
            onResult("Ошибка сети: ${t.message}")
        }
    })
}
