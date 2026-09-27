package com.tvwireguard.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object YoutubeLauncher {
    private val CANDIDATES = listOf(
        "com.google.android.youtube.tv",
        "com.google.android.youtube.googletv",
        "com.google.android.youtube",
        "com.teamsmart.videomanager.tv"
    )

    fun launch(context: Context): Boolean {
        val pm = context.packageManager
        for (pkg in CANDIDATES) {
            val launch = resolveLaunchIntent(pm, pkg) ?: continue
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
            return true
        }
        return false
    }

    fun isInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return CANDIDATES.any { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun resolveLaunchIntent(pm: PackageManager, pkg: String): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pm.getLeanbackLaunchIntentForPackage(pkg)?.let { return it }
        }
        val leanback = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            setPackage(pkg)
        }
        if (pm.resolveActivity(leanback, 0) != null) {
            return leanback
        }
        return pm.getLaunchIntentForPackage(pkg)
    }
}
