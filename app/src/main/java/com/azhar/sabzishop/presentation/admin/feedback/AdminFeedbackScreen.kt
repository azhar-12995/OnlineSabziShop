package com.azhar.sabzishop.presentation.admin.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import com.azhar.sabzishop.presentation.user.feedback.FeedbackCard

@Composable
fun AdminFeedbackScreen(
    onBackClick: () -> Unit,
    viewModel: AdminFeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SabziTopBar(title = "Customer Feedbacks", canNavigateBack = true, onNavigateBack = onBackClick)
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingView()
            uiState.errorMessage != null -> ErrorView(uiState.errorMessage!!)
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", fontSize = 12.sp, color = Color.Gray)
                                Text("${uiState.totalFeedbacks}", fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg Rating", fontSize = 12.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(String.format("%.1f", uiState.averageRating),
                                        fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.Star, null,
                                        tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                }
                            }
                            // Rating distribution
                            Column {
                                (5 downTo 1).forEach { star ->
                                    val count = uiState.feedbacks.count { it.rating == star }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("$star", fontSize = 10.sp)
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(10.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("$count", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.feedbacks.isEmpty()) {
                    item { EmptyStateView("No feedbacks yet") }
                } else {
                    items(uiState.feedbacks, key = { it.feedbackId }) { fb ->
                        FeedbackCard(fb)
                    }
                }
            }
        }
    }
}

