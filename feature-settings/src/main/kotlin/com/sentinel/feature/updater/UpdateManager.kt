package com.sentinel.feature.updater

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    private val api: GitHubUpdateService,
    @ApplicationContext private val context: Context
) {
    suspend fun checkForUpdates(): UpdateResult {
        return try {
            val latest = api.getLatestRelease("sentinel-oneguard", "sentinel-android")
            val currentVersion = getCurrentVersionName()
            
            if (isNewer(latest.tag_name, currentVersion)) {
                UpdateResult.UpdateAvailable(
                    version = latest.tag_name,
                    changelog = latest.body,
                    downloadUrl = latest.assets.firstOrNull { it.name.endsWith(".apk") }?.browser_download_url
                )
            } else {
                UpdateResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun getCurrentVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "0.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.0.0"
        }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        // Simple version comparison logic (e.g., "v1.1.0" vs "1.0.0")
        val l = latest.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val c = current.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until minOf(l.size, c.size)) {
            if (l[i] > c[i]) return true
            if (l[i] < c[i]) return false
        }
        return l.size > c.size
    }
}

sealed class UpdateResult {
    object UpToDate : UpdateResult()
    data class UpdateAvailable(val version: String, val changelog: String, val downloadUrl: String?) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}
