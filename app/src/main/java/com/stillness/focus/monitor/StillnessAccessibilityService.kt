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
import com.stillness.focus.util.ForegroundState
import com.stillness.focus.util.detectForegroundState
import com.stillness.focus.util.isAccessibilityServiceEnabled

class StillnessAccessibilityService : AccessibilityService() {

    private val leaveVerificationHandler = Handler(Looper.getMainLooper())
    private var pendingLeaveVerification: Runnable? = null
    private var sessionTimerRunnable: Runnable? = null

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
            cancelPendingLeaveVerification()
            handleBlockedAppOpened(packageName)
        } else if (SessionManager.activeBlockedPackage != null) {
            scheduleLeaveVerification()
        }
    }

    private fun handleBlockedAppOpened(packageName: String) {
        if (SessionManager.allowedPackage == packageName &&
            !SessionManager.isAfterScreenShowing.get()
        ) {
            SessionManager.markActiveInBlockedApp(packageName)
            startSessionTimerIfNeeded()
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

    private fun scheduleLeaveVerification() {
        val activePackage = SessionManager.activeBlockedPackage ?: return

        pendingLeaveVerification?.let { leaveVerificationHandler.removeCallbacks(it) }

        pendingLeaveVerification = Runnable {
            pendingLeaveVerification = null
            verifyUserLeftBlockedApp(activePackage)
        }
        leaveVerificationHandler.postDelayed(
            pendingLeaveVerification!!,
            LEAVE_VERIFICATION_DELAY_MS,
        )
    }

    private fun cancelPendingLeaveVerification() {
        pendingLeaveVerification?.let { leaveVerificationHandler.removeCallbacks(it) }
        pendingLeaveVerification = null
    }

    private fun startSessionTimerIfNeeded() {
        cancelSessionTimer()
        if (!SessionManager.hasActiveTimeLimit()) return
        if (SessionManager.sessionAfterCloseTriggered.get()) return

        val activePackage = SessionManager.allowedPackage ?: return
        val remainingMs = SessionManager.sessionEndTimeMs - System.currentTimeMillis()
        if (remainingMs <= 0) {
            onSessionTimerExpired(activePackage)
            return
        }

        sessionTimerRunnable = Runnable {
            sessionTimerRunnable = null
            onSessionTimerExpired(activePackage)
        }
        leaveVerificationHandler.postDelayed(sessionTimerRunnable!!, remainingMs)
    }

    private fun onSessionTimerExpired(activePackage: String) {
        if (SessionManager.sessionAfterCloseTriggered.get()) return
        if (SessionManager.isAfterScreenShowing.get()) return
        if (SessionManager.activeBlockedPackage != activePackage) return

        logDebug("session time limit reached for $activePackage")
        SessionManager.sessionEndedByTimer = true
        goHome()
        leaveVerificationHandler.postDelayed({
            showAfterCloseScreen(activePackage)
        }, HOME_TRANSITION_DELAY_MS)
    }

    private fun goHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun cancelSessionTimer() {
        sessionTimerRunnable?.let { leaveVerificationHandler.removeCallbacks(it) }
        sessionTimerRunnable = null
    }

    private fun showAfterCloseScreen(activePackage: String) {
        cancelSessionTimer()
        SessionManager.markAfterCloseTriggered()
        SessionManager.activeBlockedPackage = null

        if (!SessionManager.isAfterScreenShowing.compareAndSet(false, true)) return

        val intent = AfterCloseActivity.createIntent(this, activePackage).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }
    private fun verifyUserLeftBlockedApp(activePackage: String, attempt: Int = 0) {
        if (SessionManager.sessionAfterCloseTriggered.get()) return
        if (SessionManager.activeBlockedPackage != activePackage) return
        if (SessionManager.isAfterScreenShowing.get()) return

        when (
            val state = detectForegroundState(
                activeBlockedPackage = activePackage,
                ownPackage = applicationContext.packageName,
            )
        ) {
            ForegroundState.InBlockedApp -> {
                logDebug("still in blocked app: $activePackage")
            }

            is ForegroundState.InOtherApp -> {
                logDebug("left blocked app for ${state.packageName}")
                showAfterCloseScreen(activePackage)
            }

            ForegroundState.Unknown -> {
                logDebug("foreground unknown while checking leave from $activePackage")
                if (attempt < MAX_VERIFICATION_ATTEMPTS) {
                    leaveVerificationHandler.postDelayed({
                        verifyUserLeftBlockedApp(activePackage, attempt + 1)
                    }, VERIFICATION_RETRY_DELAY_MS)
                }
            }
        }
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        cancelPendingLeaveVerification()
        cancelSessionTimer()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "StillnessMonitor"
        private const val LEAVE_VERIFICATION_DELAY_MS = 500L
        private const val VERIFICATION_RETRY_DELAY_MS = 400L
        private const val MAX_VERIFICATION_ATTEMPTS = 2
        private const val HOME_TRANSITION_DELAY_MS = 300L

        fun isEnabled(context: android.content.Context): Boolean {
            return isAccessibilityServiceEnabled(context, StillnessAccessibilityService::class.java)
        }
    }
}
