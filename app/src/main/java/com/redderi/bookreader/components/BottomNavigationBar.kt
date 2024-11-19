package com.redderi.bookreader.components

import androidx.compose.foundation.Image
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.redderi.bookreader.R

@Composable
fun BottomNavigationBar(navController: NavController, username: String) {
    NavigationBar(containerColor = Color(0xFFD0BCFF)) {
        val pages = listOf(
            Pair("Quotes", R.drawable.bookmarks_24dp),
            Pair("Books", R.drawable.menu_book_24dp),
            Pair("Settings", R.drawable.settings_24dp),
            Pair("Account", R.drawable.person_24dp)
        )

        pages.forEachIndexed { index, (label, iconId) ->
            val route = when (index) {
                0 -> "quotes/$username"
                1 -> "books/$username"
                2 -> "settings"
                3 -> "account"
                else -> "books"
            }
            NavigationBarItem(
                label = { Text(label) },
                icon = {
                    Image(
                        painter = painterResource(id = iconId),
                        contentDescription = label
                    )
                },
                selected = navController.currentDestination?.route == route,
                onClick = {
                    navController.navigate(route)
                }
            )
        }
    }
}