package com.sentinel.feature.dashboard

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sentinel.domain.model.SecurityStatus

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> LoadingView()
            is DashboardUiState.Success -> DashboardContent(state.status, viewModel::refreshSecurityStatus)
            is DashboardUiState.Error -> ErrorView(state.message)
        }
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(status: SecurityStatus, onRefresh: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sentinel OneGuard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SecurityScoreWidget(status.riskScore)
            }

            item {
                Text(
                    "Device Integrity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                SecurityItem(
                    title = "Root Access",
                    status = if (status.isRooted) "Detected" else "Secured",
                    isSecure = !status.isRooted,
                    icon = Icons.Rounded.Warning
                )
            }

            item {
                SecurityItem(
                    title = "USB Debugging",
                    status = if (status.isUsbDebuggingEnabled) "Enabled" else "Disabled",
                    isSecure = !status.isUsbDebuggingEnabled,
                    icon = Icons.Rounded.Info
                )
            }

            item {
                SecurityItem(
                    title = "Security Patch",
                    status = status.securityPatchLevel,
                    isSecure = true, // Simplified
                    icon = Icons.Rounded.CheckCircle
                )
            }

            if (status.riskyApps.isNotEmpty()) {
                item {
                    Text(
                        "Risky Apps Found",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                items(status.riskyApps) { app ->
                    SecurityItem(
                        title = app.appName,
                        status = "Risk Score: ${app.riskScore}",
                        isSecure = false,
                        icon = Icons.Rounded.Warning
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Refresh Security Status")
                }
            }
        }
    }
}

@Composable
fun SecurityScoreWidget(score: Int) {
    val animatedScore by animateIntAsState(targetValue = score, label = "score")
    
    val scoreColor = when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 50 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .drawBehind {
                    drawCircle(
                        color = scoreColor.copy(alpha = 0.1f),
                        radius = size.minDimension / 1.5f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$animatedScore",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor
                )
                Text(
                    text = "Security Score",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SecurityItem(
    title: String,
    status: String,
    isSecure: Boolean,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSecure) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(
                    status,
                    color = if (isSecure) MaterialTheme.colorScheme.onSurfaceVariant 
                            else Color(0xFFF44336),
                    fontSize = 14.sp
                )
            }
        }
    }
}
