package com.redderi.bookreader.pages

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.IOException

@Composable
fun ReadingPage(filePath: String) {
    var loading by remember { mutableStateOf(true) }
    var imageScale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    LaunchedEffect(filePath) {
        loading = false
    }
    Scaffold(
        bottomBar = {}
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        imageScale = (imageScale * zoom).coerceIn(0.5f, 3f)
                        if (imageScale > 1f) {
                            offset = Offset(
                                x = offset.x + pan.x,
                                y = offset.y + pan.y
                            )
                        }
                    }
                }
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    PdfRendererPage(filePath = filePath, imageScale = imageScale, offset = offset)
                }
            }
        }
    }
}

@Composable
fun PdfRendererPage(filePath: String, imageScale: Float, offset: Offset) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var pageInput by remember { mutableStateOf("") }

    LaunchedEffect(filePath) {
        try {
            val file = File(filePath)
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            pageCount = renderer.pageCount

            if (pageCount > 0) {
                bitmap = renderPage(renderer, currentPage)
            }
            renderer.close()
        } catch (e: IOException) {
            Log.e("PdfRenderer", "Error opening PDF", e)
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "PDF Page",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = imageScale,
                            scaleY = imageScale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        if (currentPage > 0) {
                            currentPage--
                            bitmap = getRenderer(filePath)?.let { renderPage(it, currentPage) }
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page")
                    }

                    Text(
                        text = "${currentPage + 1} / $pageCount",
                        style = TextStyle(fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            showDialog = true
                        }
                    )
                    IconButton(onClick = {
                        if (currentPage < pageCount - 1) {
                            currentPage++
                            bitmap = getRenderer(filePath)?.let { renderPage(it, currentPage) }
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page")
                    }
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Выберите страницу") },
                    text = {
                        TextField(
                            value = pageInput,
                            onValueChange = { pageInput = it },
                            label = { Text("Номер страницы") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            keyboardActions = KeyboardActions(onDone = {
                                val page = pageInput.toIntOrNull()
                                if (page != null && page in 1..pageCount) {
                                    currentPage = page - 1
                                    bitmap = getRenderer(filePath)?.let { renderPage(it, currentPage) }
                                    showDialog = false
                                }
                            })
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val page = pageInput.toIntOrNull()
                            if (page != null && page in 1..pageCount) {
                                currentPage = page - 1
                                bitmap = getRenderer(filePath)?.let { renderPage(it, currentPage) }
                                showDialog = false
                            }
                        }) {
                            Text("Перейти")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

private fun renderPage(renderer: PdfRenderer, pageNumber: Int): Bitmap? {
    return try {
        val page = renderer.openPage(pageNumber)
        val scale = 1f
        val width = (page.width * scale).toInt()
        val height = (page.height * scale).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap
    } catch (e: Exception) {
        Log.e("PdfRenderer", "Error rendering page", e)
        null
    }
}

private fun getRenderer(filePath: String): PdfRenderer? {
    return try {
        val file = File(filePath)
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(fd)
    } catch (e: IOException) {
        Log.e("PdfRenderer", "Error creating renderer", e)
        null
    }
}

