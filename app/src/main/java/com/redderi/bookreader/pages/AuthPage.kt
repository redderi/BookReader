package com.redderi.bookreader.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.redderi.bookreader.utils.*

@Composable
fun AuthPage(navController: NavController, onLoginSuccess: (String) -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue()) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isLogin) "Вход" else "Регистрация", style = MaterialTheme.typography.titleLarge)

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя пользователя") }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = VisualTransformation.None
        )
        if (!isLogin) {
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Повторите пароль") },
                visualTransformation = VisualTransformation.None
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (isLogin) {
                // Вход пользователя
                loginUser(username.text, password.text, navController) { success, message ->
                    if (success) {
                        // Сохраняем имя пользователя и переходим на страницу книг
                        onLoginSuccess(username.text)
                        navController.navigate("books") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        errorMessage = message
                    }
                }
            } else {
                // Регистрация пользователя
                if (password.text == confirmPassword.text) {
                    registerUser(username.text, password.text) { success, message ->
                        if (success) {
                            // Переключаемся на страницу книг после успешной регистрации
                            onLoginSuccess(username.text)
                            navController.navigate("books") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            errorMessage = message
                        }
                    }
                } else {
                    errorMessage = "Пароли не совпадают"
                }
            }
        }) {
            Text(text = if (isLogin) "Войти" else "Зарегистрироваться")
        }

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { isLogin = !isLogin }) {
            Text(text = if (isLogin) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти")
        }
    }
}