package com.redderi.bookreader.pages

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun SettingsPage() {
    val context = LocalContext.current

    var brightness by remember {
        mutableFloatStateOf(getScreenBrightness(context))
    }

    LaunchedEffect(brightness) {
        setScreenBrightness(context, brightness)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.titleLarge
            )

            Column {
                Text("Яркость экрана", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Текущая яркость: ${(brightness * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

fun getScreenBrightness(context: Context): Float {
    return try {
        val brightness = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )
        brightness / 255f
    } catch (e: Settings.SettingNotFoundException) {
        0.5f
    }
}


fun setScreenBrightness(context: Context, brightness: Float) {
    if (Settings.System.canWrite(context)) {
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            (brightness * 255).toInt()
        )
    } else {
        val intent = android.content.Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        context.startActivity(intent)
        Toast.makeText(context, "Необходим доступ для изменения яркости", Toast.LENGTH_SHORT).show()
    }
}
