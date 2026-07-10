package com.stillness.focus

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.stillness.focus.data.InstalledAppsRepository
import com.stillness.focus.monitor.SessionManager
import com.stillness.focus.ui.screens.BeforeOpenScreen
import com.stillness.focus.ui.theme.StillnessTheme
import com.stillness.focus.util.PurposeAudioRecorder
import com.stillness.focus.util.PurposeRecording
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class BeforeOpenActivity : ComponentActivity() {
    private val audioRecorder = PurposeAudioRecorder(this)
    private var recording: PurposeRecording? = null
    private var proceeded = false
    private var preventedEntryRecorded = false
    private var targetPackage: String? = null

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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        if (packageName == null) {
            SessionManager.onBeforeScreenDismissed()
            finish()
            return
        }
        targetPackage = packageName

        val appLabel = InstalledAppsRepository(this).getAppLabel(packageName)
        var purpose by mutableStateOf("")
        var selectedTimeLimitMs by mutableStateOf<Long?>(null)

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
                    appLabel = appLabel,
                    purpose = purpose,
                    isRecording = isRecording,
                    hasRecording = hasRecording,
                    selectedTimeLimitMs = selectedTimeLimitMs,
                    onPurposeChange = { purpose = it },
                    onMicClick = { handleMicClick() },
                    onTimeLimitSelected = { selectedTimeLimitMs = it },
                    onProceed = {
                        if (isRecording) {
                            finishRecording()
                        }
                        proceeded = true
                        val currentRecording = recording
                        SessionManager.grantAccess(
                            packageName = packageName,
                            purpose = purpose.trim(),
                            audioPath = currentRecording?.filePath,
                            audioDurationMs = currentRecording?.durationMs ?: 0L,
                            waveformSamples = currentRecording?.waveformSamples ?: emptyList(),
                            timeLimitMs = selectedTimeLimitMs,
                        )
                        launchTargetApp(packageName)
                        finish()
                    },
                    onBack = {
                        recordPreventedEntryIfNeeded()
                        abandonBeforeOpenIfIncomplete()
                        goHome()
                        finish()
                    },
                )
            }
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
        recordPreventedEntryIfNeeded()
        abandonBeforeOpenIfIncomplete()
    }

    override fun onStop() {
        super.onStop()
        if (!proceeded && !isChangingConfigurations) {
            abandonBeforeOpenIfIncomplete()
            finish()
        }
    }

    override fun onDestroy() {
        if (!proceeded) {
            cancelRecording()
            SessionManager.cancelBeforeOpen()
        }
        super.onDestroy()
    }

    private fun abandonBeforeOpenIfIncomplete() {
        if (!proceeded && !isChangingConfigurations) {
            cancelRecording()
            SessionManager.cancelBeforeOpen()
        }
    }

    private fun recordPreventedEntryIfNeeded() {
        if (proceeded || preventedEntryRecorded || isChangingConfigurations) return
        val packageName = targetPackage ?: return
        preventedEntryRecorded = true
        (application as StillnessApp).preferences.recordPreventedEntryBlocking(packageName)
    }

    private fun deleteRecordingFile(path: String) {
        runCatching { java.io.File(path).delete() }
    }

    private fun launchTargetApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        }
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
        private const val EXTRA_TARGET_PACKAGE = "extra_target_package"

        fun createIntent(context: Context, packageName: String): Intent {
            return Intent(context, BeforeOpenActivity::class.java).apply {
                putExtra(EXTRA_TARGET_PACKAGE, packageName)
            }
        }
    }
}
