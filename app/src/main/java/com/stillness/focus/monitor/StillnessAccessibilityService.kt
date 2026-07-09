package com.stillness.focus.monitor

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.stillness.focus.AfterCloseActivity
import com.stillness.focus.BeforeOpenActivity
import com.stillness.focus.StillnessApp
import com.stillness.focus.util.isAccessibilityServiceEnabled

class StillnessAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        val preferences = (application as StillnessApp).preferences
        if (!preferences.isSetupCompleteBlocking()) return

        val blockedApps = preferences.getBlockedPackagesBlocking()
        if (blockedApps.isEmpty()) return

        if (packageName in blockedApps) {
            handleBlockedAppOpened(packageName)
        } else {
            handleLeftBlockedApp(blockedApps)
        }
    }

    private fun handleBlockedAppOpened(packageName: String) {
        if (SessionManager.allowedPackage == packageName) {
            SessionManager.markActiveInBlockedApp(packageName)
            return
        }

        if (SessionManager.isBeforeScreenShowing.compareAndSet(false, true)) {
            val intent = BeforeOpenActivity.createIntent(this, packageName).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }

    private fun handleLeftBlockedApp(blockedApps: Set<String>) {
        val activePackage = SessionManager.activeBlockedPackage ?: return
        if (activePackage !in blockedApps) return

        SessionManager.activeBlockedPackage = null

        if (SessionManager.isAfterScreenShowing.compareAndSet(false, true)) {
            val intent = AfterCloseActivity.createIntent(this, activePackage).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() = Unit

    companion object {
        fun isEnabled(context: android.content.Context): Boolean {
            return isAccessibilityServiceEnabled(context, StillnessAccessibilityService::class.java)
        }
    }
}
