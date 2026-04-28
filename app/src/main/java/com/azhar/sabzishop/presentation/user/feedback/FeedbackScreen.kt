package com.azhar.sabzishop.presentation.user.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhar.sabzishop.domain.model.Feedback
import com.azhar.sabzishop.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit,
    onLoginRequired: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            SabziTopBar(title = "Feedback", canNavigateBack = true, onNavigateBack = onBackClick)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.user == null) {
            // Guest - must login
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Feedback, null, modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Login to submit feedback", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                SabziButton("Login", onClick = onLoginRequired,
                    modifier = Modifier.width(200.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Submit Feedback Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Share Your Feedback", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))

                            // Star rating
                            Text("Rating", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..5).forEach { star ->
                                    IconButton(
                                        onClick = { viewModel.onRatingChange(star) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            if (star <= uiState.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "$star star",
                                            tint = if (star <= uiState.rating) Color(0xFFFF9800) else Color.Gray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = uiState.message,
                                onValueChange = viewModel::onMessageChange,
                                label = { Text("Your feedback message") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 5,
                                singleLine = false
                            )

                            uiState.errorMessage?.let {
                                Spacer(Modifier.height(8.dp))
                                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }

                            Spacer(Modifier.height(16.dp))
                            SabziButton("Submit Feedback", onClick = viewModel::submitFeedback,
                                isLoading = uiState.isSending)
                        }
                    }
                }

                // My previous feedbacks
                if (uiState.myFeedbacks.isNotEmpty()) {
                    item {
                        Text("My Previous Feedbacks", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    items(uiState.myFeedbacks, key = { it.feedbackId }) { fb ->
                        FeedbackCard(fb)
                    }
                }

                if (uiState.isLoading) {
                    item { LoadingView(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
fun FeedbackCard(feedback: Feedback) {
    val dateStr = feedback.createdAt?.toDate()?.let {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
    } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                // Stars
                Row {
                    repeat(5) { i ->
                        Icon(
                            if (i < feedback.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            null,
                            tint = if (i < feedback.rating) Color(0xFFFF9800) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(feedback.message, style = MaterialTheme.typography.bodyMedium)
            if (feedback.userName.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("— ${feedback.userName}", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

