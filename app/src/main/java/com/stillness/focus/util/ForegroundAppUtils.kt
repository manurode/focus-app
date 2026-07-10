package com.stillness.focus.util

import android.content.Context
import android.view.inputmethod.InputMethodManager

private val TRANSIENT_OVERLAY_PACKAGES = setOf(
    "android",
    "com.android.systemui",
    "com.samsung.android.app.cocktailbarservice",
    "com.samsung.android.app.taskedge",
    "com.samsung.android.service.notification",
    "com.samsung.android.app.navigatebar",
)

fun isTransientForegroundPackage(context: Context, packageName: String): Boolean {
    if (packageName in TRANSIENT_OVERLAY_PACKAGES) return true
    return context.isInputMethodPackage(packageName)
}

private fun Context.isInputMethodPackage(packageName: String): Boolean {
    val imm = getSystemService(InputMethodManager::class.java) ?: return false
    return imm.enabledInputMethodList.any { it.packageName == packageName }
}
