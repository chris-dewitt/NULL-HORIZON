package com.nullhorizon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullhorizon.app.ui.theme.NullHorizonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NullHorizonTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BootScreen()
                }
            }
        }
    }
}

@Composable
fun BootScreen(
    title: String = stringResource(R.string.app_name),
    status: String = stringResource(R.string.boot_status),
) {
    val graphite = Color(0xFF1B1E24)
    val panel = Color(0xFF2A3038)
    val warmText = Color(0xFFE8E2D6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(graphite, panel, graphite),
                ),
            )
            .padding(24.dp)
            .semantics { contentDescription = "NULL HORIZON boot screen" },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            color = warmText,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        )
        Text(
            text = status,
            modifier = Modifier.padding(top = 12.dp),
            color = warmText.copy(alpha = 0.85f),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BootScreenPreview() {
    NullHorizonTheme {
        BootScreen()
    }
}
