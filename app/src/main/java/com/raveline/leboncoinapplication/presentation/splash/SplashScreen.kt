package com.raveline.leboncoinapplication.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.raveline.leboncoinapplication.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.0f,
        isPlaying = true,
        restartOnPlay = true,
    )

    val animationState = animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.0f
    )

    LaunchedEffect(animationState.isAtEnd && !animationState.isPlaying) {
        if (animationState.isAtEnd && !animationState.isPlaying) {
            delay(2000)
            onAnimationFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen(
        onAnimationFinished = {}
    )
}
