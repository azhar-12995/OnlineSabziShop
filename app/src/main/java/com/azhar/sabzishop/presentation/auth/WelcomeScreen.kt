package com.azhar.sabzishop.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azhar.sabzishop.presentation.components.SabziButton
import com.azhar.sabzishop.presentation.components.SabziOutlinedButton

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(40.dp))

            // Branding
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Sabzi Shop", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Fresh Vegetables\nDelivered to Your Home",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    FeatureBadge("🌿 Farm Fresh")
                    FeatureBadge("✅ Best Quality")
                    FeatureBadge("🚴 On Time")
                }
            }

            // Action buttons
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SabziButton(
                    text = "Login",
                    onClick = onLoginClick,
                    containerColor = MaterialTheme.colorScheme.secondary
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    TextButton(onClick = onSignUpClick, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Sign Up", color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
                TextButton(onClick = onGuestClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Continue as Guest", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
private fun FeatureBadge(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.2f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = Color.White, fontSize = 12.sp)
    }
}

