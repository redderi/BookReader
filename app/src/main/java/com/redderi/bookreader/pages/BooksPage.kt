package com.redderi.bookreader.pages

import com.redderi.bookreader.utils.TagsRow
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.redderi.bookreader.R
import com.redderi.bookreader.model.ApiResponse
import com.redderi.bookreader.model.Book
import com.redderi.bookreader.model.BookIdRequest
import com.redderi.bookreader.network.RetrofitClient
import com.redderi.bookreader.utils.RatingStars
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksPage(username: String) {
    val context = LocalContext.current
    var userBooks by remember { mutableStateOf<List<Book>>(emptyList()) }
    var allBooks by remember { mutableStateOf<List<Book>>(emptyList()) }
    var filteredBooks by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showAllBooks by remember { mutableStateOf(false) }
    var showAddButton by remember { mutableStateOf(true) }
    var readingFilePath by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(username) {
        isLoading = true
        errorMessage = ""
        fetchUserBooks(username) { fetchedBooks ->
            userBooks = fetchedBooks
            filteredBooks = fetchedBooks
            isLoading = false
        }
    }

    fun filterBooks(query: String) {
        filteredBooks = if (query.isEmpty()) {
            allBooks
        } else {
            allBooks.filter { book ->
                book.title.contains(query, ignoreCase = true) ||
                        book.authorName.contains(query, ignoreCase = true) ||
                        book.tagNames.any { it.contains(query, ignoreCase = true) }
            }
        }
    }

    if (readingFilePath != null) {
        ReadingPage(filePath = readingFilePath!!)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (showAllBooks) {
                        IconButton(
                            onClick = {
                                showAllBooks = false
                                showAddButton = true
                                isLoading = true
                                fetchUserBooks(username) { fetchedBooks ->
                                    userBooks = fetchedBooks
                                    isLoading = false
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Вернуться")
                        }
                    }

                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            filterBooks(it)
                        },
                        label = { Text("Поиск книги") },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(50)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                if (showAllBooks) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(filteredBooks) { book ->
                            BookItem(
                                book,
                                onAddBook = { bookId -> addBookToUser(username, bookId, context) },
                                onStartReading = { bookId ->
                                    startReadingBook(context, bookId) { filePath ->
                                        readingFilePath = filePath
                                    }
                                },
                                onDeleteBook = {},
                                isUserBook = false
                            )
                        }
                    }
                } else {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                    } else {
                        if (errorMessage.isNotEmpty()) {
                            Text(text = errorMessage, color = Color.Red)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                items(userBooks) { book ->
                                    BookItem(
                                        book,
                                        onAddBook = { bookId -> addBookToUser(username, bookId, context) },
                                        onStartReading = { bookId ->
                                            startReadingBook(context, bookId) { filePath ->
                                                readingFilePath = filePath
                                            }
                                        },
                                        onDeleteBook = { bookId ->
                                            deleteBookFromUser(username, bookId, context) {
                                                userBooks = userBooks.filter { it.id != bookId }
                                            }
                                        },
                                        isUserBook = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showAddButton) {
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        fetchAllBooks { fetchedBooks ->
                            allBooks = fetchedBooks
                            filteredBooks = fetchedBooks
                            showAllBooks = true
                            showAddButton = false
                            isLoading = false
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(36.dp)
                        .size(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("+", style = MaterialTheme.typography.bodyLarge.
                    copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookItem(
    book: Book,
    onAddBook: (Long) -> Unit,
    onStartReading: (Long) -> Unit,
    onDeleteBook: (Long) -> Unit,
    isUserBook: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (isUserBook) {
                        showDeleteDialog = true
                    }
                }
            ),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val coverPainter: Painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = "${RetrofitClient.BASE_URL}/api/books/cover/${book.id}")
                        .apply(block = fun ImageRequest.Builder.() {
                            crossfade(true)
                            placeholder(R.drawable.placeholder)
                            error(R.drawable.error)
                        }).build()
                )
                Image(
                    painter = coverPainter,
                    contentDescription = book.title,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.authorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "(${book.year})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RatingStars(book.rating)

                        Row(
                            modifier = Modifier.wrapContentWidth(Alignment.End).padding(end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isUserBook) {
                                IconButton(onClick = { onStartReading(book.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Начать чтение",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onAddBook(book.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            TagsRow(tagNames = book.tagNames)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить книгу?") },
            text = { Text("Вы уверены, что хотите удалить книгу \"${book.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteBook(book.id)
                    }
                ) {
                    Text("Удалить", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}



fun fetchUserBooks(username: String, onResult: (List<Book>) -> Unit) {
    RetrofitClient.apiService.getUserBooks(username).enqueue(object : Callback<List<Book>> {
        override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
            if (response.isSuccessful) {
                val books = response.body() ?: emptyList()
                onResult(books)
            } else {
                onResult(emptyList())
            }
        }
        override fun onFailure(call: Call<List<Book>>, t: Throwable) {
            onResult(emptyList())
        }
    })
}

fun fetchAllBooks(onResult: (List<Book>) -> Unit) {
    RetrofitClient.apiService.getAllBooks().enqueue(object : Callback<List<Book>> {
        override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
            if (response.isSuccessful) {
                val books = response.body() ?: emptyList()
                onResult(books)
            } else {
                onResult(emptyList())
            }
        }
        override fun onFailure(call: Call<List<Book>>, t: Throwable) {
            onResult(emptyList())
        }
    })
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun addBookToUser(username: String, bookId: Long, context: Context) {
    val request = BookIdRequest(bookId)
    RetrofitClient.apiService.addBookToUser(username, request).enqueue(object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            if (response.isSuccessful) {
                Log.d("com.redderi.bookreader.pages.BooksPage", "Книга добавлена!")
                showToast(context, "Книга добавлена в ваш список!")
            } else {
                Log.d("com.redderi.bookreader.pages.BooksPage", "Ошибка добавления книги!")
                showToast(context, "Ошибка добавления книги!")
            }
        }
        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Log.d("com.redderi.bookreader.pages.BooksPage", "Ошибка подключения к серверу!")
            showToast(context, "Ошибка подключения к серверу!")
        }
    })
}

fun deleteBookFromUser(username: String, bookId: Long, context: Context, onSuccess: () -> Unit) {
    RetrofitClient.apiService.removeBookFromUser(username, bookId).enqueue(object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.status == "Success") {
                    Log.d("com.redderi.bookreader.pages.BooksPage", "Книга удалена!")
                    showToast(context, "Книга успешно удалена!")
                    onSuccess() // Обновляем UI
                } else {
                    Log.d("com.redderi.bookreader.pages.BooksPage", "Ошибка удаления книги!")
                    showToast(context, apiResponse?.message ?: "Ошибка удаления книги!")
                }
            } else {
                Log.d("com.redderi.bookreader.pages.BooksPage", "Не удалось удалить книгу. ${response.errorBody()?.string()}")
                showToast(context, "Ошибка удаления книги!")
            }
        }

        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Log.d("com.redderi.bookreader.pages.BooksPage", "Ошибка подключения к серверу!")
            showToast(context, "Ошибка подключения к серверу!")
        }
    })
}

fun startReadingBook(context: Context, bookId: Long, onResult: (String?) -> Unit) {
    val file = File(context.filesDir, "book_$bookId.pdf")

    if (file.exists()) {
        onResult(file.absolutePath)
        return
    }

    RetrofitClient.apiService.downloadBook(bookId).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    try {
                        val outputStream = FileOutputStream(file)
                        outputStream.write(responseBody.bytes())
                        outputStream.close()
                        onResult(file.absolutePath)
                    } catch (e: IOException) {
                        Log.e("com.redderi.bookreader.pages.BooksPage", "Ошибка записи файла", e)
                        onResult(null)
                    }
                }
            } else {
                Log.e("com.redderi.bookreader.pages.BooksPage", "Не удалось скачать книгу. Код ошибки: ${response.code()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("com.redderi.bookreader.pages.BooksPage", "Ошибка при подключении: ${t.message}")
            onResult(null)
        }
    })
}

