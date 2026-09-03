package com.jesuslcorominas.teamflowmanager.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.jesuslcorominas.teamflowmanager.sharedui.R

private val provider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

private val bebasNeue = GoogleFont("Bebas Neue")

private val publicSans = GoogleFont("Public Sans")

actual val BebasNeueFontFamily: FontFamily =
    FontFamily(
        Font(bebasNeue, provider, FontWeight.Normal),
        Font(bebasNeue, provider, FontWeight.Bold),
    )

actual val PublicSansFontFamily: FontFamily =
    FontFamily(
        Font(publicSans, provider, FontWeight.Light),
        Font(publicSans, provider, FontWeight.Normal),
        Font(publicSans, provider, FontWeight.Medium),
        Font(publicSans, provider, FontWeight.SemiBold),
        Font(publicSans, provider, FontWeight.Bold),
    )
