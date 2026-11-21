package cl.planb.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bosque = Color(0xFF112820)
val Lima = Color(0xFFB6F36A)
val Crema = Color(0xFFF5F5ED)

@Composable
fun PlanBTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            lightColorScheme(
                primary = Bosque,
                onPrimary = Color.White,
                primaryContainer = Lima,
                onPrimaryContainer = Bosque,
                background = Crema,
                surface = Color.White,
                secondary = Color(0xFF516448),
            ),
        content = content,
    )
}
