package com.sentinel.feature.scan

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.sentinel.domain.model.ScannedApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scanInstalledApps(): List<ScannedApp> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return apps.mapNotNull { appInfo ->
            try {
                val packageInfo = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS)
                val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
                
                val riskScore = calculateAppRisk(appInfo, permissions)
                
                ScannedApp(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    riskScore = riskScore,
                    permissions = permissions,
                    isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.riskScore }
    }

    private fun calculateAppRisk(appInfo: ApplicationInfo, permissions: List<String>): Int {
        var score = 0
        
        // Dangerous permissions
        val dangerous = listOf(
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.PROCESS_OUTGOING_CALLS",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.BIND_ACCESSIBILITY_SERVICE"
        )
        
        score += permissions.count { dangerous.contains(it) } * 10
        
        // System apps are generally trusted but we still flag high permissions
        if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            score += 15 // Base boost for non-system apps
        }
        
        return score.coerceIn(0, 100)
    }
}
