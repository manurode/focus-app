package com.stillness.focus.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

private val TRANSIENT_OVERLAY_PACKAGES = setOf(
    "android",
    "com.android.systemui",
    "com.samsung.android.app.cocktailbarservice",
    "com.samsung.android.app.taskedge",
    "com.samsung.android.service.notification",
)

private val SYSTEM_OVERLAY_CLASS_KEYWORDS = listOf(
    "NotificationShade",
    "StatusBar",
    "SystemUI",
    "QuickSettings",
    "QsPanel",
    "RecentsActivity",
    "OverviewPanel",
    "NavigationBar",
)

fun AccessibilityEvent.isTransientForegroundChange(context: Context): Boolean {
    val packageName = packageName?.toString() ?: return false
    return isTransientForegroundPackage(context, packageName) || isSystemOverlayEvent()
}

fun AccessibilityEvent.isSystemOverlayEvent(): Boolean {
    val className = className?.toString() ?: return false
    return SYSTEM_OVERLAY_CLASS_KEYWORDS.any { keyword ->
        className.contains(keyword, ignoreCase = true)
    }
}

fun isTransientForegroundPackage(context: Context, packageName: String): Boolean {
    if (packageName in TRANSIENT_OVERLAY_PACKAGES) return true
    return context.isInputMethodPackage(packageName)
}

private fun Context.isInputMethodPackage(packageName: String): Boolean {
    val imm = getSystemService(InputMethodManager::class.java) ?: return false
    return imm.enabledInputMethodList.any { it.packageName == packageName }
}

fun Context.isLauncherPackage(packageName: String): Boolean {
    val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    return packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        .any { resolveInfo -> resolveInfo.activityInfo.packageName == packageName }
}
