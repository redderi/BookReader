package com.redderi.bookreader.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.redderi.bookreader.model.ApiResponse
import com.redderi.bookreader.model.Quote
import com.redderi.bookreader.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesPage(username: String) {
    var quotes by remember { mutableStateOf(listOf<Quote>()) }
    var filteredQuotes by remember { mutableStateOf(listOf<Quote>()) }
    var newQuoteText by remember { mutableStateOf(TextFieldValue("")) }
    var newQuoteAuthor by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf("") }
    var showAddQuote by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    fun getUserQuotes(username: String) {
        RetrofitClient.apiService.getUserQuotes(username).enqueue(object : Callback<List<Quote>> {
            override fun onResponse(call: Call<List<Quote>>, response: Response<List<Quote>>) {
                if (response.isSuccessful) {
                    quotes = response.body() ?: emptyList()
                    filteredQuotes = quotes
                    errorMessage = ""
                } else {
                    errorMessage = "Ошибка получения цитат: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<Quote>>, t: Throwable) {
                errorMessage = "Ошибка сети: ${t.message}"
            }
        })
    }

    fun addQuote() {
        if (newQuoteText.text.isNotEmpty() && newQuoteAuthor.text.isNotEmpty()) {
            val newQuote = Quote(text = newQuoteText.text, author = newQuoteAuthor.text)
            RetrofitClient.apiService.addQuoteToUser(username, newQuote)
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            quotes = quotes + newQuote
                            filteredQuotes = quotes
                            newQuoteText = TextFieldValue("")
                            newQuoteAuthor = TextFieldValue("")
                            errorMessage = ""
                            showAddQuote = false
                        } else {
                            Log.e("QuotesPage", "Ошибка при добавлении цитаты: ${response.code()} ${response.message()}")
                            errorMessage = "Ошибка добавления цитаты: ${response.message()}"
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("QuotesPage", "Ошибка сети: ${t.message}")
                        errorMessage = "Ошибка сети: ${t.message}"
                    }
                })
        } else {
            errorMessage = "Пожалуйста, заполните оба поля."
        }
    }

    fun deleteQuote(index: Int) {
        RetrofitClient.apiService.removeQuoteFromUser(username, index)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        quotes = quotes.filterIndexed { i, _ -> i != index }
                        filteredQuotes = quotes
                        errorMessage = ""
                    } else {
                        errorMessage = "Ошибка удаления цитаты: ${response.message()}"
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    errorMessage = "Ошибка сети: ${t.message}"
                }
            })
    }

    fun editQuote(index: Int, newText: String, newAuthor: String) {
        val updatedQuote = Quote(text = newText, author = newAuthor)
        RetrofitClient.apiService.updateQuoteOfUser(username, index, updatedQuote)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        quotes = quotes.toMutableList().apply {
                            set(index, updatedQuote)
                        }
                        filteredQuotes = quotes
                        errorMessage = ""
                    } else {
                        errorMessage = "Ошибка обновления цитаты: ${response.message()}"
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    errorMessage = "Ошибка сети: ${t.message}"
                }
            })
    }

    fun filterQuotes() {
        filteredQuotes = if (searchText.text.isEmpty()) {
            quotes
        } else {
            quotes.filter { quote ->
                quote.text.contains(searchText.text, ignoreCase = true) ||
                        (quote.author?.contains(searchText.text, ignoreCase = true) ?: false)
            }
        }
    }

    LaunchedEffect(searchText) {
        filterQuotes()
    }

    LaunchedEffect(Unit) {
        getUserQuotes(username)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxHeight()) {
            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    filterQuotes()
                },
                label = { Text("Поиск цитаты") },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp)
                    .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(50))
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                )
            )

            Column(modifier = Modifier.padding(4.dp)) {
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (showAddQuote) {
                    TextField(
                        value = newQuoteText,
                        onValueChange = { newQuoteText = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        label = { Text("Цитата") },
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.White)
                    )
                    TextField(
                        value = newQuoteAuthor,
                        onValueChange = { newQuoteAuthor = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        label = { Text("Автор") },
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.White)
                    )
                    Button(onClick = { addQuote() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Добавить цитату")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredQuotes) { quote ->
                        QuoteItem(
                            quote = quote,
                            onDelete = { deleteQuote(quotes.indexOf(quote)) },
                            onEdit = { newText, newAuthor ->
                                editQuote(quotes.indexOf(quote), newText, newAuthor)
                            }
                        )
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 36.dp)) {
            Button(
                onClick = { showAddQuote = !showAddQuote },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.BottomCenter),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "+",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteItem(quote: Quote, onDelete: () -> Unit, onEdit: (String, String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(quote.text) }
    var editedAuthor by remember { mutableStateOf(quote.author ?: "") }
    var offsetX by remember { mutableStateOf(0f) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val swipeThreshold = 120f
    val backgroundColor = when {
        offsetX < -swipeThreshold -> Color.Blue
        offsetX > swipeThreshold -> Color.Red
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        offsetX += dragAmount.x * 0.8f
                        change.consume()
                    },
                    onDragEnd = {
                        if (offsetX < -swipeThreshold) {
                            isEditing = true
                        } else if (offsetX > swipeThreshold) {
                            showDeleteConfirmationDialog = true
                        }
                        offsetX = 0f
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .offset(x = offsetX.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isEditing) {
                        TextField(
                            value = editedText,
                            onValueChange = { editedText = it },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            label = { Text("Цитата") },
                            colors = TextFieldDefaults.textFieldColors(containerColor = Color.White)
                        )

                        TextField(
                            value = editedAuthor,
                            onValueChange = { editedAuthor = it },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            label = { Text("Автор") },
                            colors = TextFieldDefaults.textFieldColors(containerColor = Color.White)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    onEdit(editedText, editedAuthor)
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Изменить")
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Button(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Отмена")
                            }
                        }
                    } else {
                        Text(
                            text = "\"${quote.text}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Автор: ${quote.author ?: "Не указан"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Подтвердите удаление") },
            text = { Text("Вы уверены, что хотите удалить эту цитату?") },
            confirmButton = {
                Button(onClick = {
                    onDelete()
                    showDeleteConfirmationDialog = false
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Нет")
                }
            }
        )
    }
}