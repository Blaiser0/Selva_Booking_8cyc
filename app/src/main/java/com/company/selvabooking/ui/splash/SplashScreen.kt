package com.company.selvabooking.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.company.selvabooking.ui.components.LogoDisplaySize
import com.company.selvabooking.ui.components.SelvaLogo
import com.company.selvabooking.ui.theme.LogoBackground
import com.company.selvabooking.utils.Constants
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(Constants.SPLASH_DELAY_MS)
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LogoBackground),
        contentAlignment = Alignment.Center
    ) {
        SelvaLogo(
            size = LogoDisplaySize.Splash,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = logoAlpha
                }
        )
    }
}
