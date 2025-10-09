package com.jesuslcorominas.teamflowmanager.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF003366)
val PrimaryLight = Color(0xFF4F7BBF)
val PrimaryLight20 = Color(0xFFE6EEF8)

val AccentEmphasis = Color(0xFFDB3B2E)
val AccentEmphasis10 = Color(0xFFFBECEA)

val AccentAffirmative = Color(0xFF3BC15B)
val AccentAffirmative10 = Color(0xFFEAF9EE)

val AccentWarning = Color(0xFFE6A93B)
val AccentWarning10 = Color(0xFFFDF6E8)

val AccentDanger = Color(0xFFDA3B28)
val AccentDanger10 = Color(0xFFFCE9E7)

val ContentMain = Color(0xFF1A1A1A)
val ContentHigh = Color(0xFF7A7A7A)
val ContentLow = Color(0xFFD9D9D9)
val ContentContrast = Color(0xFFFFFFFF)
val ContentDisabled = Color(0xFFB3B3B3)

val BackgroundMain = Color(0xFFFFFFFF)
val BackgroundLow = Color(0xFFF5F5F5)
val BackgroundDisabled = Color(0xFFBFBFBF)
val BackgroundContrast = Color(0xFF001933) // Azul muy oscuro (pantallas o secciones invertidas)
val BackgroundOverlay = Color(0x66000000)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = ContentMain,
    secondary = AccentEmphasis,
    onSecondary = White,
    secondaryContainer = AccentEmphasis10,
    onSecondaryContainer = ContentMain,
    tertiary = AccentAffirmative,
    onTertiary = White,
    error = AccentDanger,
    onError = White,
    background = BackgroundMain,
    onBackground = ContentMain,
    surface = White,
    onSurface = ContentMain,
)
