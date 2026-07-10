package com.stillness.focus.monitor

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.stillness.focus.AfterCloseActivity
import com.stillness.focus.BeforeOpenActivity
import com.stillness.focus.StillnessApp
import com.stillness.focus.util.isAccessibilityServiceEnabled
import com.stillness.focus.util.isLauncherPackage
import com.stillness.focus.util.isTransientForegroundChange
import com.stillness.focus.util.isTransientForegroundPackage

class StillnessAccessibilityService : AccessibilityService() {

    private val leaveConfirmationHandler = Handler(Looper.getMainLooper())
    private var pendingLeaveRunnable: Runnable? = null
    private var pendingLeaveDestination: String? = null
    private var lastBlockedAppForegroundTime = 0L
    private var lastTransientOverlayTime = 0L
    private var lastShadeCloseTime = 0L
    private var notificationShadeOpen = false

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
            notificationShadeOpen = false
            onBlockedAppForeground(packageName)
            cancelPendingLeaveConfirmation()
            handleBlockedAppOpened(packageName)
        } else if (event.isTransientForegroundChange(applicationContext)) {
            onTransientOverlay(packageName, event.className?.toString())
        } else {
            handleSuspectedLeave(blockedApps, packageName, event.className?.toString())
        }
    }

    private fun onTransientOverlay(packageName: String, className: String?) {
        lastTransientOverlayTime = System.currentTimeMillis()
        if (!notificationShadeOpen) {
            notificationShadeOpen = true
            logDebug("notification shade opened")
        } else {
            notificationShadeOpen = false
            lastShadeCloseTime = System.currentTimeMillis()
            logDebug("notification shade closed")
        }
        cancelPendingLeaveConfirmation()
        logEvent("ignored transient overlay", packageName, className)
    }

    private fun handleSuspectedLeave(
        blockedApps: Set<String>,
        destinationPackage: String,
        className: String?,
    ) {
        logEvent("suspected leave", destinationPackage, className)

        if (applicationContext.isLauncherPackage(destinationPackage) &&
            shouldIgnoreLauncherAfterNotificationShade()
        ) {
            return
        }

        scheduleLeaveConfirmation(blockedApps, destinationPackage)
    }

    private fun shouldIgnoreLauncherAfterNotificationShade(): Boolean {
        if (notificationShadeOpen) {
            notificationShadeOpen = false
            lastShadeCloseTime = System.currentTimeMillis()
            logDebug("ignored launcher triggered by notification shade close")
            return true
        }
        if (isWithinShadeCloseGracePeriod()) {
            logDebug("ignored launcher within shade close grace period")
            return true
        }
        return false
    }

    private fun onBlockedAppForeground(packageName: String) {
        if (SessionManager.allowedPackage == packageName ||
            SessionManager.activeBlockedPackage == packageName
        ) {
            lastBlockedAppForegroundTime = System.currentTimeMillis()
        }
    }

    private fun handleBlockedAppOpened(packageName: String) {
        if (SessionManager.allowedPackage == packageName &&
            !SessionManager.isAfterScreenShowing.get()
        ) {
            SessionManager.markActiveInBlockedApp(packageName)
            return
        }

        if (SessionManager.isAfterScreenShowing.get()) {
            SessionManager.cancelReflection()
        }

        if (SessionManager.isBeforeScreenShowing.get()) {
            SessionManager.cancelBeforeOpen()
        }

        if (SessionManager.isBeforeScreenShowing.compareAndSet(false, true)) {
            val intent = BeforeOpenActivity.createIntent(this, packageName).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }

    private fun scheduleLeaveConfirmation(blockedApps: Set<String>, destinationPackage: String) {
        val activePackage = SessionManager.activeBlockedPackage ?: return
        if (activePackage !in blockedApps) return
        if (destinationPackage == activePackage) return
        if (isTransientForegroundPackage(applicationContext, destinationPackage)) return

        pendingLeaveRunnable?.let { leaveConfirmationHandler.removeCallbacks(it) }
        pendingLeaveDestination = destinationPackage

        val delayMs = if (applicationContext.isLauncherPackage(destinationPackage)) {
            LAUNCHER_LEAVE_DELAY_MS
        } else {
            LEAVE_CONFIRMATION_DELAY_MS
        }

        pendingLeaveRunnable = Runnable {
            pendingLeaveRunnable = null
            val destination = pendingLeaveDestination
            pendingLeaveDestination = null
            if (destination != null) {
                confirmLeftBlockedApp(activePackage, destination, blockedApps)
            }
        }
        leaveConfirmationHandler.postDelayed(pendingLeaveRunnable!!, delayMs)
    }

    private fun cancelPendingLeaveConfirmation() {
        pendingLeaveRunnable?.let { leaveConfirmationHandler.removeCallbacks(it) }
        pendingLeaveRunnable = null
        pendingLeaveDestination = null
    }

    private fun confirmLeftBlockedApp(
        activePackage: String,
        destinationPackage: String,
        blockedApps: Set<String>,
    ) {
        if (SessionManager.activeBlockedPackage != activePackage) return
        if (SessionManager.isAfterScreenShowing.get()) return
        if (destinationPackage == activePackage) return
        if (isTransientForegroundPackage(applicationContext, destinationPackage)) return

        if (applicationContext.isLauncherPackage(destinationPackage) &&
            isWithinShadeCloseGracePeriod()
        ) {
            logDebug("leave confirmation aborted: shade close grace period")
            return
        }
        if (isWithinOverlayGracePeriod()) {
            logDebug("leave confirmation aborted: overlay grace period")
            return
        }
        if (isWithinRecentBlockedAppGracePeriod()) {
            logDebug("leave confirmation aborted: recent blocked app foreground")
            return
        }

        val foregroundPackage = getForegroundPackage()
        logDebug(
            "confirm leave active=$activePackage destination=$destinationPackage foreground=$foregroundPackage",
        )

        if (foregroundPackage == activePackage) return
        if (foregroundPackage != null &&
            isTransientForegroundPackage(applicationContext, foregroundPackage)
        ) {
            return
        }

        showAfterCloseScreen(activePackage)
    }

    private fun showAfterCloseScreen(activePackage: String) {
        logDebug("leave confirmed for $activePackage")
        SessionManager.activeBlockedPackage = null

        if (SessionManager.isAfterScreenShowing.compareAndSet(false, true)) {
            val intent = AfterCloseActivity.createIntent(this, activePackage).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }

    private fun isWithinOverlayGracePeriod(): Boolean {
        return System.currentTimeMillis() - lastTransientOverlayTime < OVERLAY_GRACE_PERIOD_MS
    }

    private fun isWithinShadeCloseGracePeriod(): Boolean {
        return System.currentTimeMillis() - lastShadeCloseTime < SHADE_CLOSE_GRACE_MS
    }

    private fun isWithinRecentBlockedAppGracePeriod(): Boolean {
        return System.currentTimeMillis() - lastBlockedAppForegroundTime < RECENT_BLOCKED_APP_GRACE_MS
    }

    private fun getForegroundPackage(): String? {
        val root = rootInActiveWindow ?: return null
        return try {
            root.packageName?.toString()
        } finally {
            @Suppress("DEPRECATION")
            root.recycle()
        }
    }

    private fun logEvent(message: String, packageName: String, className: String?) {
        logDebug("$message pkg=$packageName class=$className")
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        cancelPendingLeaveConfirmation()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "StillnessMonitor"
        private const val LEAVE_CONFIRMATION_DELAY_MS = 400L
        private const val LAUNCHER_LEAVE_DELAY_MS = 750L
        private const val OVERLAY_GRACE_PERIOD_MS = 1_000L
        private const val SHADE_CLOSE_GRACE_MS = 1_500L
        private const val RECENT_BLOCKED_APP_GRACE_MS = 800L

        fun isEnabled(context: android.content.Context): Boolean {
            return isAccessibilityServiceEnabled(context, StillnessAccessibilityService::class.java)
        }
    }
}
