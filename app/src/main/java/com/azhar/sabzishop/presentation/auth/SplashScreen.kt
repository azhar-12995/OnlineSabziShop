package com.azhar.sabzishop.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToUser: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Animate entrance
    var startAnimation by remember { mutableStateOf(false) }
    val iconScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "iconScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900, delayMillis = 200),
        label = "contentAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 500),
        label = "textAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Navigate after splash animation + data loaded
    LaunchedEffect(uiState.destination) {
        if (uiState.destination != null) {
            delay(1500) // Let the user see the splash for a moment
            when (uiState.destination) {
                "auth" -> onNavigateToAuth()
                "user" -> onNavigateToUser()
                "admin" -> onNavigateToAdmin()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Outer glowing ring
            Surface(
                modifier = Modifier
                    .size(150.dp)
                    .scale(iconScale)
                    .alpha(contentAlpha),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Inner ring
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Icon circle
                            Surface(
                                modifier = Modifier.size(90.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.25f),
                                shadowElevation = 8.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = "Sabzi Shop",
                                        tint = Color.White,
                                        modifier = Modifier.size(52.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // App name
            Text(
                "Sabzi Shop",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.alpha(textAlpha)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "🌿 Fresh & Healthy Vegetables",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(56.dp))

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.7f),
                    strokeWidth = 2.5.dp,
                    modifier = Modifier
                        .size(32.dp)
                        .alpha(contentAlpha)
                )
            }
        }
    }
}
