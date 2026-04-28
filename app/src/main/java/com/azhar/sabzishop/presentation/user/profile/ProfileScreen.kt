package com.azhar.sabzishop.presentation.user.profile

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.ImageUtils

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onOrdersClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val base64 = ImageUtils.uriToBase64(context, it)
            if (base64 != null) {
                viewModel.onProfileImageSelected(base64)
            }
        }
    }

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLogout()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.logout() }) {
                    Text("Yes, Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            SabziTopBar(
                title = "Profile",
                canNavigateBack = true,
                onNavigateBack = onBackClick,
                actions = {
                    if (!uiState.isEditing && uiState.user != null) {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(Icons.Default.Edit, "Edit Profile")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingView()
            uiState.isEditing -> {
                // Edit Mode
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize()
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))

                    // Profile image (editable)
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap: Bitmap? = remember(uiState.editProfileImageBase64) {
                            if (uiState.editProfileImageBase64.isNotBlank())
                                ImageUtils.base64ToBitmap(uiState.editProfileImageBase64) else null
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap.asImageBitmap(), "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        // Camera overlay
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).size(32.dp),
                            shape = CircleShape, color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, null,
                                    tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Text("Tap to change photo", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(24.dp))

                    SabziTextField(uiState.editFullName, viewModel::onFullNameChange, "Full Name",
                        leadingIcon = { Icon(Icons.Default.Person, null) })
                    Spacer(Modifier.height(12.dp))
                    SabziTextField(uiState.editPhone, viewModel::onPhoneChange, "Phone Number",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, null) })
                    Spacer(Modifier.height(12.dp))
                    SabziTextField(uiState.editAddress, viewModel::onAddressChange, "Area / City",
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) })
                    Spacer(Modifier.height(12.dp))
                    SabziTextField(uiState.editStreet, viewModel::onStreetChange, "Street",
                        leadingIcon = { Icon(Icons.Default.Signpost, null) })
                    Spacer(Modifier.height(12.dp))
                    SabziTextField(uiState.editHouseNumber, viewModel::onHouseNumberChange, "House Number",
                        leadingIcon = { Icon(Icons.Default.Home, null) })

                    uiState.errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(24.dp))
                    SabziButton("Save Changes", onClick = viewModel::saveProfile, isLoading = uiState.isSaving)
                    Spacer(Modifier.height(8.dp))
                    SabziOutlinedButton("Cancel", onClick = viewModel::cancelEditing)
                }
            }
            else -> {
                // View Mode
                Column(modifier = Modifier.padding(padding).fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(24.dp))

                    // Avatar with profile image
                    val user = uiState.user
                    val profileBitmap: Bitmap? = remember(user?.profileImageBase64) {
                        if (!user?.profileImageBase64.isNullOrBlank())
                            ImageUtils.base64ToBitmap(user!!.profileImageBase64) else null
                    }
                    if (profileBitmap != null) {
                        Image(
                            profileBitmap.asImageBitmap(), "Profile",
                            modifier = Modifier.size(80.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(modifier = Modifier.size(80.dp), shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(user?.fullName ?: "Guest", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(32.dp))

                    // Info card
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ProfileRow(Icons.Default.Phone, "Phone", user?.phone ?: "—")
                            ProfileRow(Icons.Default.Email, "Email", user?.email ?: "—")
                            ProfileRow(Icons.Default.LocationOn, "Address",
                                buildString {
                                    if (!user?.houseNumber.isNullOrBlank()) append("House ${user!!.houseNumber}, ")
                                    if (!user?.street.isNullOrBlank()) append("${user!!.street}, ")
                                    append(user?.address ?: "—")
                                }.ifBlank { "—" }
                            )
                            ProfileRow(Icons.Default.Person, "Role", user?.role?.uppercase() ?: "CUSTOMER")
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(onClick = viewModel::startEditing, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onOrdersClick, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.List, null)
                        Spacer(Modifier.width(8.dp))
                        Text("My Orders")
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Logout, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
