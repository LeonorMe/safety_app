package com.sentinel.core.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreSecurityProvider @Inject constructor() {

    fun isRooted(): Boolean {
        val paths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/system/sbin/su",
            "/usr/bin/su"
        )
        return paths.any { File(it).exists() } || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod2(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = process.inputStream.bufferedReader()
            reader.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkRootMethod3(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED, 0
        ) != 0
    }

    fun isUnknownSourcesEnabled(context: Context): Boolean {
        return try {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0
            ) != 0
        } catch (e: Exception) {
            // In newer Android versions, this is handled per-app
            false
        }
    }

    fun getSecurityPatchLevel(): String {
        return Build.VERSION.SECURITY_PATCH
    }
}
