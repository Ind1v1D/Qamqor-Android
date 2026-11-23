package com.example.qamqorapp
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.data.SettingsRepository
import com.example.qamqorapp.data.ThemeMode
import com.example.qamqorapp.ui.localizedFor
import com.example.qamqorapp.ui.navigation.AppShell
import com.example.qamqorapp.ui.theme.QamqorTheme
import org.koin.android.ext.android.inject
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)
        val settings: SettingsRepository by inject()
        setContent {
            AppTheme(settings) {
                AppShell()
            }
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private companion object {
        const val TAG = "QamqorLifecycle"
    }
}
@Composable
private fun AppTheme(
    settings: com.example.qamqorapp.data.SettingsRepository,
    content: @Composable () -> Unit,
) {
    val themeMode by settings.themeMode.collectAsStateWithLifecycle()
    val language by settings.language.collectAsStateWithLifecycle()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val baseContext = LocalContext.current
    val localizedContext = remember(language, baseContext) {
        val localized = baseContext.localizedFor(language)
        object : ContextWrapper(baseContext) {
            override fun getResources(): Resources = localized.resources
            override fun getAssets(): AssetManager = localized.assets
        }
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        QamqorTheme(darkTheme = darkTheme, content = content)
    }
}
