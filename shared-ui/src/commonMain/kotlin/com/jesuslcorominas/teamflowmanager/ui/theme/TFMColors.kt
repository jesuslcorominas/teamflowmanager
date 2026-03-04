package com.jesuslcorominas.teamflowmanager.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF003366)
val PrimaryLight = Color(0xFF4F7BBF)

val AccentEmphasis = Color(0xFF960615)
val AccentAffirmative = Color(0xFF3BC15B)
val AccentDanger = Color(0xFF960615)

val ShirtOrange = Color(0xFFDD4E3E)

val ContentMain = Color(0xFF001933)
val ContentContrast = Color(0xFFFFFFFF)

val BackgroundMain = Color(0xFFFFFFFF)
val BackgroundContrast = Color(0xFF001933)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

val SubstitutionGreen = Color(0xFF4CAF50)
val SubstitutionRed = Color(0xFF960615)

val GoalKeeperBadge = Color(0xFF960615)

val ChartTeamColor = Color(0xFF003366)
val ChartOpponentColor = Color(0xFFFF6B35)

val LightColorScheme =
    lightColorScheme(
        primary = Primary,
        onPrimary = White,
        primaryContainer = PrimaryLight,
        onPrimaryContainer = ContentMain,
        secondary = AccentEmphasis,
        onSecondary = White,
        secondaryContainer = Color(0xFFFBECEA),
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
