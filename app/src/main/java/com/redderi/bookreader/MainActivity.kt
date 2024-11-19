package com.redderi.bookreader

import com.redderi.bookreader.pages.BooksPage
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.redderi.bookreader.components.BottomNavigationBar
import com.redderi.bookreader.pages.*

class MainActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()

        if (!Settings.System.canWrite(this)) {
            Toast.makeText(this, "Пожалуйста, разрешите приложению изменять настройки яркости", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:${packageName}")
            startActivity(intent)
        }

        setContent {
            MyApp(this)
        }
    }
}

@Composable
fun MyApp(context: Context) {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var brightness by remember { mutableFloatStateOf(getCurrentBrightness(context)) }
    var showBrightnessIndicator by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (isLoggedIn) {
                BottomNavigationBar(navController, username)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pointers = event.changes

                                if (pointers.size == 2) {
                                    val firstPointer = pointers[0]
                                    val secondPointer = pointers[1]

                                    if (firstPointer.pressed) {
                                        val dragAmount = secondPointer.positionChange()
                                        val newBrightness = (brightness - dragAmount.y / 500).coerceIn(0f, 1f)
                                        if (newBrightness != brightness) {
                                            brightness = newBrightness
                                            setBrightness(context, brightness)
                                            showBrightnessIndicator = true
                                        }
                                    }
                                } else {
                                    showBrightnessIndicator = false
                                }
                            }
                        }
                    }
            ) {
                if (showBrightnessIndicator) {
                    LinearProgressIndicator(
                        progress = { brightness },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .align(Alignment.TopCenter),
                    )
                }

                NavHost(navController, startDestination = "auth", Modifier.fillMaxSize()) {
                    composable("auth") {
                        AuthPage(navController) { name ->
                            isLoggedIn = true
                            username = name
                            navController.navigate("books/$username") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    }
                    composable("books/{username}") { backStackEntry ->
                        val usernameArg = backStackEntry.arguments?.getString("username") ?: ""
                        BooksPage(usernameArg)
                    }
                    composable("account") {
                        AccountPage(username = username, navController = navController)
                    }
                    composable("quotes/{username}") { backStackEntry ->
                        val usernameArg = backStackEntry.arguments?.getString("username") ?: ""
                        QuotesPage(username = usernameArg)
                    }
                    composable("settings") { SettingsPage() }
                }
            }
        }
    }
}

fun getCurrentBrightness(context: Context): Float {
    return Settings.System.getInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS,
        255
    ) / 255f
}

fun setBrightness(context: Context, brightness: Float) {
    val contentResolver: ContentResolver = context.contentResolver
    val newBrightness = (brightness * 255).toInt()

    try {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, newBrightness)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp(context = LocalContext.current)
}