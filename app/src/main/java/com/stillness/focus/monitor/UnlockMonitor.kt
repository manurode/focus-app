package com.stillness.focus.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.stillness.focus.AfterUnlockActivity
import com.stillness.focus.BeforeUnlockActivity
import com.stillness.focus.StillnessApp
import com.stillness.focus.data.AppPreferences

class UnlockMonitor(private val context: Context) {
    private var receiver: BroadcastReceiver? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastUnlockHandledAt = 0L
    private var pendingLaunchRunnable: Runnable? = null

    fun register() {
        if (receiver != null) return

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "onReceive: ${intent.action}")
                when (intent.action) {
                    Intent.ACTION_USER_PRESENT -> handleUnlockEvent("USER_PRESENT")
                    Intent.ACTION_SCREEN_OFF -> handleScreenOff()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.applicationContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.applicationContext.registerReceiver(receiver, filter)
        }

        Log.d(TAG, "register: receiver registered")
    }

    fun unregister() {
        receiver?.let {
            runCatching { context.applicationContext.unregisterReceiver(it) }
        }
        receiver = null
        pendingLaunchRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingLaunchRunnable = null
        Log.d(TAG, "unregister: receiver unregistered")
    }

    private fun handleUnlockEvent(source: String) {
        val now = System.currentTimeMillis()
        if (now - lastUnlockHandledAt < UNLOCK_DEBOUNCE_MS) {
            Log.d(TAG, "$source: ignored (debounced)")
            return
        }

        val preferences = (context.applicationContext as StillnessApp).preferences
        if (!preferences.isSetupCompleteBlocking()) {
            Log.d(TAG, "$source: ignored (setup incomplete)")
            return
        }

        if (SessionManager.isUnlockBeforeShowing.get() || SessionManager.isUnlockAfterShowing.get()) {
            Log.d(TAG, "$source: ignored (unlock screen already showing)")
            return
        }

        if (SessionManager.isBeforeScreenShowing.get() || SessionManager.isAfterScreenShowing.get()) {
            Log.d(TAG, "$source: ignored (app screen showing)")
            return
        }

        lastUnlockHandledAt = now

        if (preferences.hasPendingUnlockReflectionBlocking()) {
            Log.d(TAG, "$source: scheduling after-unlock reflection")
            scheduleLaunch {
                launchAfterUnlock(chainToBeforeUnlock = preferences.isUnlockMonitoringEnabledBlocking())
            }
            return
        }

        if (preferences.isUnlockMonitoringEnabledBlocking()) {
            Log.d(TAG, "$source: scheduling before-unlock")
            scheduleLaunch { launchBeforeUnlock() }
        } else {
            Log.d(TAG, "$source: ignored (unlock monitoring disabled)")
        }
    }

    private fun scheduleLaunch(action: () -> Unit) {
        pendingLaunchRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingLaunchRunnable = Runnable {
            pendingLaunchRunnable = null
            action()
        }
        mainHandler.postDelayed(pendingLaunchRunnable!!, POST_UNLOCK_DELAY_MS)
    }

    private fun handleScreenOff() {
        Log.d(TAG, "SCREEN_OFF")
        val preferences = (context.applicationContext as StillnessApp).preferences
        if (!preferences.isSetupCompleteBlocking()) return

        if (SessionManager.isUnlockBeforeShowing.get()) {
            Log.d(TAG, "SCREEN_OFF: before-unlock showing, leaving activity to handle")
            return
        }

        if (SessionManager.isUnlockAfterShowing.get()) {
            SessionManager.cancelUnlockReflection()
            return
        }

        if (SessionManager.unlockSessionActive) {
            Log.d(TAG, "SCREEN_OFF: saving pending unlock reflection")
            preferences.savePendingUnlockReflectionBlocking(
                purpose = SessionManager.unlockPurposeNote,
                audioPath = SessionManager.unlockPurposeAudioPath,
                audioDurationMs = SessionManager.unlockPurposeAudioDurationMs,
                waveformSamples = SessionManager.unlockPurposeWaveformSamples,
            )
            SessionManager.unlockSessionActive = false
        }
    }

    private fun launchBeforeUnlock() {
        if (!SessionManager.isUnlockBeforeShowing.compareAndSet(false, true)) {
            Log.d(TAG, "launchBeforeUnlock: already showing")
            return
        }

        val intent = BeforeUnlockActivity.createIntent(context.applicationContext).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION,
            )
        }

        runCatching {
            context.applicationContext.startActivity(intent)
            Log.d(TAG, "launchBeforeUnlock: activity started")
        }.onFailure { error ->
            SessionManager.cancelUnlockBeforeOpen()
            Log.e(TAG, "launchBeforeUnlock: failed", error)
        }
    }

    private fun launchAfterUnlock(chainToBeforeUnlock: Boolean) {
        if (!SessionManager.isUnlockAfterShowing.compareAndSet(false, true)) {
            Log.d(TAG, "launchAfterUnlock: already showing")
            return
        }

        val intent = AfterUnlockActivity.createIntent(
            context = context.applicationContext,
            chainToBeforeUnlock = chainToBeforeUnlock,
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION,
            )
        }

        runCatching {
            context.applicationContext.startActivity(intent)
            Log.d(TAG, "launchAfterUnlock: activity started")
        }.onFailure { error ->
            SessionManager.cancelUnlockReflection()
            Log.e(TAG, "launchAfterUnlock: failed", error)
        }
    }

    companion object {
        private const val TAG = "UnlockMonitor"
        private const val UNLOCK_DEBOUNCE_MS = 1500L
        private const val POST_UNLOCK_DELAY_MS = 600L

        fun launchBeforeUnlockFromReflection(context: Context) {
            if (!SessionManager.isUnlockBeforeShowing.compareAndSet(false, true)) return

            val intent = BeforeUnlockActivity.createIntent(context.applicationContext).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION,
                )
            }

            runCatching {
                context.applicationContext.startActivity(intent)
            }.onFailure {
                SessionManager.cancelUnlockBeforeOpen()
            }
        }
    }
}
