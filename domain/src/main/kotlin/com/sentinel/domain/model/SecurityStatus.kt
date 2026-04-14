package com.sentinel.domain.model

data class SecurityStatus(
    val isRooted: Boolean,
    val isUsbDebuggingEnabled: Boolean,
    val isUnknownSourcesEnabled: Boolean,
    val securityPatchLevel: String,
    val riskScore: Int
)

data class ThreatIndicator(
    val title: String,
    val description: String,
    val severity: Severity
)

enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class ScannedApp(
    val packageName: String,
    val appName: String,
    val riskScore: Int,
    val permissions: List<String>,
    val isSystemApp: Boolean
)
