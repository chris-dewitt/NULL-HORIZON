package com.nullhorizon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.nullhorizon.app.audio.AndroidSoundPlayer
import com.nullhorizon.app.audio.LocalSoundPlayer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.navigation.NullHorizonNavHost
import com.nullhorizon.app.ui.chrome.BootSequenceScreen
import com.nullhorizon.app.ui.chrome.CrtFrame
import com.nullhorizon.app.ui.chrome.CrtProfile
import com.nullhorizon.app.ui.theme.NullHorizonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NullHorizonApplication
        setContent {
            NullHorizonApp(app = app)
        }
    }
}

@Composable
fun NullHorizonApp(
    app: NullHorizonApplication,
) {
    val accessibility by app.container.settingsRepository.accessibilitySettings
        .collectAsStateWithLifecycle(
            initialValue = com.nullhorizon.app.data.settings.AccessibilitySettings(),
        )
    val density = LocalDensity.current
    val contentDensity = if (accessibility.largerText) {
        Density(
            density = density.density,
            fontScale = density.fontScale * 1.15f,
        )
    } else {
        density
    }
    val terminalFont = remember {
        FontFamily(
            Font(
                resId = R.font.nh_terminal_regular,
                weight = FontWeight.Normal,
            ),
        )
    }
    var bootComplete by rememberSaveable { mutableStateOf(false) }

    // App-lifetime sound player; freed when the composition leaves.
    val context = LocalContext.current
    val soundPlayer = remember { AndroidSoundPlayer(context, enabled = accessibility.soundEnabled) }
    soundPlayer.enabled = accessibility.soundEnabled
    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    NullHorizonTheme(
        highContrast = accessibility.highContrast,
        reducedMotion = accessibility.reducedMotion,
        largerText = accessibility.largerText,
        disableCrt = accessibility.disableCrt,
        fontFamily = terminalFont,
    ) {
        CompositionLocalProvider(
            LocalDensity provides contentDensity,
            LocalSoundPlayer provides soundPlayer,
        ) {
            CrtFrame(
                modifier = Modifier.fillMaxSize(),
                profile = CrtProfile.Lean,
            ) {
                if (!bootComplete) {
                    BootSequenceScreen(onFinished = { bootComplete = true })
                } else {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            NullHorizonNavHost(appContainer = app.container)
                        }
                    }
                }
            }
        }
    }
}
