package com.jesuslcorominas.teamflowmanager.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont

val provider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = emptyList(),
    )

val publicSans = GoogleFont("Public Sans")

val PublicSansFontFamily =
    FontFamily(
        androidx.compose.ui.text.googlefonts
            .Font(publicSans, provider, FontWeight.Light),
        androidx.compose.ui.text.googlefonts
            .Font(publicSans, provider, FontWeight.Normal),
        androidx.compose.ui.text.googlefonts
            .Font(publicSans, provider, FontWeight.Medium),
        androidx.compose.ui.text.googlefonts
            .Font(publicSans, provider, FontWeight.SemiBold),
        androidx.compose.ui.text.googlefonts
            .Font(publicSans, provider, FontWeight.Bold),
    )
