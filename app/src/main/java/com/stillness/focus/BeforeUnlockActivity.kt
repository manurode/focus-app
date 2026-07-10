package com.stillness.focus

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.stillness.focus.data.AppPreferences
import com.stillness.focus.monitor.SessionManager
import com.stillness.focus.ui.screens.BeforeOpenScreen
import com.stillness.focus.ui.theme.StillnessTheme
import com.stillness.focus.util.PurposeAudioRecorder
import com.stillness.focus.util.PurposeRecording
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class BeforeUnlockActivity : ComponentActivity() {
    private val audioRecorder = PurposeAudioRecorder(this)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recording: PurposeRecording? = null
    private var proceeded = false
    private var preventedEntryRecorded = false
    private var createdAt = 0L
    private var bringToFrontRunnable: Runnable? = null

    private var isRecording by mutableStateOf(false)
    private var hasRecording by mutableStateOf(false)

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            beginRecording()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        super.onCreate(savedInstanceState)
        createdAt = System.currentTimeMillis()
        enableEdgeToEdge()

        Log.d(TAG, "onCreate")

        var purpose by mutableStateOf("")

        setContent {
            StillnessTheme {
                LaunchedEffect(isRecording) {
                    if (isRecording) {
                        while (isActive && audioRecorder.isRecording) {
                            audioRecorder.captureAmplitude()
                            delay(100)
                        }
                    }
                }

                BeforeOpenScreen(
                    contextDescription = "using your phone",
                    purpose = purpose,
                    isRecording = isRecording,
                    hasRecording = hasRecording,
                    onPurposeChange = { purpose = it },
                    onMicClick = { handleMicClick() },
                    onProceed = {
                        if (isRecording) {
                            finishRecording()
                        }
                        proceeded = true
                        val currentRecording = recording
                        SessionManager.grantUnlockAccess(
                            purpose = purpose.trim(),
                            audioPath = currentRecording?.filePath,
                            audioDurationMs = currentRecording?.durationMs ?: 0L,
                            waveformSamples = currentRecording?.waveformSamples ?: emptyList(),
                        )
                        finish()
                    },
                    onBack = {
                        recordPreventedEntryIfNeeded()
                        abandonBeforeUnlockIfIncomplete()
                        goHome()
                        finish()
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: brought back to front")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            cancelBringToFront()
        }
    }

    private fun handleMicClick() {
        if (isRecording) {
            finishRecording()
            return
        }

        if (hasMicPermission()) {
            beginRecording()
        } else {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun beginRecording() {
        recording?.let { deleteRecordingFile(it.filePath) }
        recording = null
        hasRecording = false
        if (audioRecorder.start()) {
            isRecording = true
        }
    }

    private fun finishRecording() {
        recording = audioRecorder.stop()
        hasRecording = recording != null
        isRecording = false
    }

    private fun cancelRecording() {
        if (isRecording) {
            audioRecorder.cancel()
            isRecording = false
        }
        recording?.let { deleteRecordingFile(it.filePath) }
        recording = null
        hasRecording = false
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isWithinLaunchGracePeriod()) return
        recordPreventedEntryIfNeeded()
        abandonBeforeUnlockIfIncomplete()
        finish()
    }

    override fun onStop() {
        super.onStop()
        if (proceeded || isChangingConfigurations) return

        if (isWithinLaunchGracePeriod()) {
            Log.d(TAG, "onStop: within launch grace period, scheduling bring-to-front")
            scheduleBringToFront()
            return
        }

        if (isDeviceLocked()) {
            Log.d(TAG, "onStop: device locked, closing")
            recordPreventedEntryIfNeeded()
            abandonBeforeUnlockIfIncomplete()
            finish()
        }
    }

    override fun onDestroy() {
        cancelBringToFront()
        if (!proceeded) {
            cancelRecording()
            SessionManager.cancelUnlockBeforeOpen()
        }
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun isWithinLaunchGracePeriod(): Boolean {
        return System.currentTimeMillis() - createdAt < LAUNCH_GRACE_MS
    }

    private fun scheduleBringToFront() {
        cancelBringToFront()
        bringToFrontRunnable = Runnable {
            if (proceeded || isFinishing || isDestroyed) return@Runnable
            if (hasWindowFocus()) return@Runnable

            if (isDeviceLocked()) {
                Log.d(TAG, "bringToFront: device locked, closing")
                recordPreventedEntryIfNeeded()
                abandonBeforeUnlockIfIncomplete()
                finish()
                return@Runnable
            }

            Log.d(TAG, "bringToFront: reordering activity to front")
            val intent = createIntent(this).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION,
                )
            }
            startActivity(intent)
        }
        mainHandler.postDelayed(bringToFrontRunnable!!, BRING_TO_FRONT_DELAY_MS)
    }

    private fun cancelBringToFront() {
        bringToFrontRunnable?.let { mainHandler.removeCallbacks(it) }
        bringToFrontRunnable = null
    }

    private fun isDeviceLocked(): Boolean {
        val keyguardManager = getSystemService(KeyguardManager::class.java)
        return keyguardManager.isDeviceLocked
    }

    private fun abandonBeforeUnlockIfIncomplete() {
        if (!proceeded && !isChangingConfigurations) {
            cancelRecording()
            SessionManager.cancelUnlockBeforeOpen()
        }
    }

    private fun recordPreventedEntryIfNeeded() {
        if (proceeded || preventedEntryRecorded || isChangingConfigurations) return
        preventedEntryRecorded = true
        (application as StillnessApp).preferences
            .recordPreventedEntryBlocking(AppPreferences.UNLOCK_STATS_ID)
    }

    private fun deleteRecordingFile(path: String) {
        runCatching { java.io.File(path).delete() }
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }

    companion object {
        private const val TAG = "BeforeUnlockActivity"
        private const val LAUNCH_GRACE_MS = 2500L
        private const val BRING_TO_FRONT_DELAY_MS = 400L

        fun createIntent(context: Context): Intent {
            return Intent(context, BeforeUnlockActivity::class.java)
        }
    }
}
