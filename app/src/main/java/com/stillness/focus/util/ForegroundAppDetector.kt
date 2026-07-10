package com.stillness.focus.util

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityWindowInfo

sealed class ForegroundState {
    data object InBlockedApp : ForegroundState()

    data class InOtherApp(val packageName: String) : ForegroundState()

    data object Unknown : ForegroundState()
}

fun AccessibilityService.detectForegroundState(
    activeBlockedPackage: String,
    ownPackage: String,
): ForegroundState {
    val serviceWindows = windows
    if (serviceWindows.isNullOrEmpty()) {
        return ForegroundState.Unknown
    }

    try {
        var blockedAppVisible = false
        var topOtherApp: String? = null
        var topOtherLayer = -1

        for (window in serviceWindows) {
            if (window.type != AccessibilityWindowInfo.TYPE_APPLICATION) continue
            if (!window.isActive) continue

            val root = window.root ?: continue
            try {
                val packageName = root.packageName?.toString() ?: continue
                when {
                    packageName == activeBlockedPackage -> blockedAppVisible = true
                    packageName == ownPackage -> Unit
                    isTransientForegroundPackage(this, packageName) -> Unit
                    window.layer >= topOtherLayer -> {
                        topOtherLayer = window.layer
                        topOtherApp = packageName
                    }
                }
            } finally {
                @Suppress("DEPRECATION")
                root.recycle()
            }
        }

        return when {
            blockedAppVisible -> ForegroundState.InBlockedApp
            topOtherApp != null -> ForegroundState.InOtherApp(topOtherApp)
            else -> ForegroundState.Unknown
        }
    } finally {
        for (window in serviceWindows) {
            window.recycle()
        }
    }
}
