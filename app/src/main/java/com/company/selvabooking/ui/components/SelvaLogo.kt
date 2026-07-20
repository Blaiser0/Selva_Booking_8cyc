package com.company.selvabooking.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.company.selvabooking.R
import com.company.selvabooking.ui.theme.LogoBackground

enum class LogoDisplaySize {
    Splash,
    Auth
}

@Composable
fun SelvaLogo(
    size: LogoDisplaySize,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val widthFraction = when (size) {
        LogoDisplaySize.Splash -> 0.92f
        LogoDisplaySize.Auth -> 0.82f
    }
    val maxWidth = when (size) {
        LogoDisplaySize.Splash -> 420.dp
        LogoDisplaySize.Auth -> 340.dp
    }
    val logoWidth = minOf(screenWidth * widthFraction, maxWidth)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(LogoBackground),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_selva_booking),
            contentDescription = "Selva Booking",
            modifier = Modifier.width(logoWidth),
            contentScale = ContentScale.Fit
        )
    }
}
