package com.jesuslcorominas.teamflowmanager.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.jesuslcorominas.teamflowmanager.R

val provider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )
val publicSans = GoogleFont("Public Sans")
val bebasNeue = GoogleFont("Bebas Neue")

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

val BebasNeueFontFamily =
    FontFamily(
        androidx.compose.ui.text.googlefonts
            .Font(bebasNeue, provider, FontWeight.Light),
        androidx.compose.ui.text.googlefonts
            .Font(bebasNeue, provider, FontWeight.Normal),
        androidx.compose.ui.text.googlefonts
            .Font(bebasNeue, provider, FontWeight.Medium),
        androidx.compose.ui.text.googlefonts
            .Font(bebasNeue, provider, FontWeight.SemiBold),
        androidx.compose.ui.text.googlefonts
            .Font(bebasNeue, provider, FontWeight.Bold),
    )
