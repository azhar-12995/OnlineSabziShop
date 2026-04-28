package com.azhar.sabzishop.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.SabziButton
import com.azhar.sabzishop.presentation.components.SabziTextField

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSignUpSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Text("Join Sabzi Shop today", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        SabziTextField(uiState.fullName, viewModel::onFullNameChange, "Full Name",
            leadingIcon = { Icon(Icons.Default.Person, null) })
        Spacer(Modifier.height(12.dp))
        SabziTextField(uiState.phone, viewModel::onPhoneChange, "Phone Number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = { Icon(Icons.Default.Phone, null) })
        Spacer(Modifier.height(12.dp))
        SabziTextField(uiState.email, viewModel::onEmailChange, "Email Address",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Default.Email, null) })
        Spacer(Modifier.height(12.dp))

        // Address fields
        Text("Delivery Address", style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
        SabziTextField(uiState.address, viewModel::onAddressChange, "Area / City",
            leadingIcon = { Icon(Icons.Default.LocationOn, null) })
        Spacer(Modifier.height(12.dp))
        SabziTextField(uiState.street, viewModel::onStreetChange, "Street",
            leadingIcon = { Icon(Icons.Default.Signpost, null) })
        Spacer(Modifier.height(12.dp))
        SabziTextField(uiState.houseNumber, viewModel::onHouseNumberChange, "House Number",
            leadingIcon = { Icon(Icons.Default.Home, null) })
        Spacer(Modifier.height(12.dp))

        SabziTextField(uiState.password, viewModel::onPasswordChange, "Password",
            isPassword = true, leadingIcon = { Icon(Icons.Default.Lock, null) })
        Spacer(Modifier.height(12.dp))
        SabziTextField(uiState.confirmPassword, viewModel::onConfirmPasswordChange, "Confirm Password",
            isPassword = true, leadingIcon = { Icon(Icons.Default.Lock, null) })

        uiState.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(24.dp))
        SabziButton("Create Account", onClick = viewModel::signUp, isLoading = uiState.isLoading)
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?")
            TextButton(onClick = onLoginClick) { Text("Login") }
        }
        TextButton(onClick = onBackClick) { Text("← Back") }
    }
}
