package com.azhar.sabzishop.presentation.admin.editproduct

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.presentation.components.*
import com.azhar.sabzishop.utils.Constants
import com.azhar.sabzishop.utils.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    onBackClick: () -> Unit,
    onProductUpdated: () -> Unit,
    viewModel: EditProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onProductUpdated() }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(context, it) }
    }

    Scaffold(
        topBar = { SabziTopBar(title = "Edit Product", canNavigateBack = true, onNavigateBack = onBackClick) }
    ) { padding ->
        if (uiState.isLoading && uiState.name.isBlank()) { LoadingView(); return@Scaffold }

        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center) {
                val bitmap = remember(uiState.imageBase64) {
                    if (uiState.imageBase64.isNotBlank()) ImageUtils.base64ToBitmap(uiState.imageBase64) else null
                }
                if (bitmap != null) {
                    Image(bitmap.asImageBitmap(), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Upload, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Tap to change image", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            SabziTextField(uiState.name, viewModel::onNameChange, "Vegetable Name")
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(value = uiState.category, onValueChange = {}, readOnly = true,
                    label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    Constants.CATEGORIES.filter { it != "All" }.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { viewModel.onCategoryChange(cat); categoryExpanded = false })
                    }
                }
            }
            SabziTextField(uiState.price, viewModel::onPriceChange, "Price per kg (PKR)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            SabziTextField(uiState.stock, viewModel::onStockChange, "Stock Quantity",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            SabziTextField(uiState.description, viewModel::onDescriptionChange, "Description", singleLine = false, maxLines = 3)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.isAvailable, onCheckedChange = viewModel::onAvailabilityChange)
                Text("Available for sale")
            }
            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            SabziButton("Save Changes", onClick = viewModel::updateProduct, isLoading = uiState.isLoading)
        }
        OverlayLoader(isLoading = uiState.isLoading && uiState.name.isNotBlank(), message = "Updating product...")
        }
    }
}

