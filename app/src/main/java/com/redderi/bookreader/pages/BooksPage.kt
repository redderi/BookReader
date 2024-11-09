package com.redderi.bookreader.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redderi.bookreader.model.Book

@Composable
fun BooksPage() {
    val books = listOf(
        Book("1984", "Джордж Оруэлл"),
        Book("Убить пересмешника", "Харпер Ли"),
        Book("Великий Гэтсби", "Ф. Скотт Фицджеральд"),
        Book("Моби Дик", "Герман Мелвилл"),
        Book("Война и мир", "Лев Толстой")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(books) { book ->
            BookItem(book)
        }
    }
}

@Composable
fun BookItem(book: Book) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = book.title, style = MaterialTheme.typography.titleLarge)
            Text(text = book.author, style = MaterialTheme.typography.bodyMedium)
        }
    }
}