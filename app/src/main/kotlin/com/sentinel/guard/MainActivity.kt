package com.sentinel.guard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.sentinel.feature.dashboard.DashboardScreen
import com.sentinel.feature.dashboard.DashboardViewModel
import com.sentinel.guard.ui.theme.SentinelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SentinelTheme {
                DashboardScreen(viewModel = dashboardViewModel)
            }
        }
    }
}
