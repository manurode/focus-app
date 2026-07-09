package com.stillness.focus.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
)

class InstalledAppsRepository(private val context: Context) {
    fun getLaunchableApps(): List<InstalledApp> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .asSequence()
            .map { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                InstalledApp(
                    packageName = appInfo.packageName,
                    label = appInfo.loadLabel(pm).toString(),
                    icon = appInfo.loadIcon(pm),
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun getAppLabel(packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            info.loadLabel(pm).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
