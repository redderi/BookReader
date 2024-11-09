package com.redderi.bookreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import com.redderi.bookreader.components.BottomNavigationBar
import com.redderi.bookreader.pages.AccountPage
import com.redderi.bookreader.pages.AuthPage
import com.redderi.bookreader.pages.BooksPage
import com.redderi.bookreader.pages.QuotesPage
import com.redderi.bookreader.pages.ReadSetupPage
import com.redderi.bookreader.pages.SettingsPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            if (isLoggedIn) {
                BottomNavigationBar(navController)
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "auth", Modifier.padding(padding)) {
            composable("auth") {
                AuthPage(navController) { name ->
                    isLoggedIn = true
                    username = name
                }
            }
            composable("books") { BooksPage() }
            composable("readSetup") { ReadSetupPage() }
            composable("quotes") { QuotesPage() }
            composable("settings") { SettingsPage() }
            composable("account") { AccountPage() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp()
}