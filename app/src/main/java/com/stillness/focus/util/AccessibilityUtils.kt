package com.stillness.focus.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false

    val expectedComponent = ComponentName(context, serviceClass).flattenToString()
    return TextUtils.SimpleStringSplitter(':').let { splitter ->
        splitter.setString(enabledServices)
        generateSequence { if (splitter.hasNext()) splitter.next() else null }
            .any { it.equals(expectedComponent, ignoreCase = true) }
    }
}

fun openAccessibilitySettings(context: Context) {
    context.startActivity(
        android.content.Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}
