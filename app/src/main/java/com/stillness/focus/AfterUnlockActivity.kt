package com.stillness.focus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.stillness.focus.data.AppPreferences
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.monitor.SessionManager
import com.stillness.focus.monitor.UnlockMonitor
import com.stillness.focus.ui.screens.AfterCloseScreen
import com.stillness.focus.ui.screens.PurposeStatsScreen
import com.stillness.focus.ui.theme.StillnessTheme
import com.stillness.focus.util.PurposeAudioPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class AfterUnlockActivity : ComponentActivity() {
    private val audioPlayer = PurposeAudioPlayer()
    private var isPlaying by mutableStateOf(false)
    private var playbackProgress by mutableFloatStateOf(0f)
    private var reflectionCompleted = false
    private var chainToBeforeUnlock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        chainToBeforeUnlock = intent.getBooleanExtra(EXTRA_CHAIN_TO_BEFORE_UNLOCK, false)

        val preferences = (application as StillnessApp).preferences
        val pending = preferences.loadPendingUnlockReflectionBlocking()
        if (pending == null) {
            SessionManager.isUnlockAfterShowing.set(false)
            finish()
            return
        }

        SessionManager.loadPendingUnlockReflection(
            purpose = pending.purpose,
            audioPath = pending.audioPath,
            audioDurationMs = pending.audioDurationMs,
            waveformSamples = pending.waveformSamples,
        )

        val purposeNote = SessionManager.unlockPurposeNote
        val audioPath = SessionManager.unlockPurposeAudioPath
        val audioDurationMs = SessionManager.unlockPurposeAudioDurationMs
        val waveformSamples = SessionManager.unlockPurposeWaveformSamples
        var hasPlayableAudio = false

        if (audioPath != null) {
            hasPlayableAudio = runCatching {
                audioPlayer.prepare(audioPath) {
                    isPlaying = false
                    playbackProgress = 0f
                }
            }.isSuccess
        }

        var showStats by mutableStateOf(false)
        var stats by mutableStateOf(PurposeStats())

        setContent {
            StillnessTheme {
                LaunchedEffect(isPlaying) {
                    while (isActive && isPlaying) {
                        playbackProgress = audioPlayer.playbackProgress
                        delay(50)
                    }
                }

                if (showStats) {
                    PurposeStatsScreen(
                        subjectLabel = "your phone",
                        statsDescription = "Every time you unlock your phone, Stillness asks why. Here's how it's going.",
                        mindfulPauseDescription = "Stillness stopped you from unlocking your phone without a clear purpose — " +
                            "catching those automatic checks before they start.",
                        stats = stats,
                        onContinue = { finishReflection() },
                    )
                } else {
                    AfterCloseScreen(
                        purposeNote = purposeNote,
                        audioDurationMs = if (hasPlayableAudio) audioDurationMs else 0L,
                        waveformSamples = if (hasPlayableAudio) waveformSamples else emptyList(),
                        isPlaying = isPlaying,
                        playbackProgress = playbackProgress,
                        onPlayPause = { togglePlayback() },
                        onNo = {
                            stopPlayback()
                            preferences.recordNotAccomplishedBlocking(AppPreferences.UNLOCK_STATS_ID)
                            stats = preferences.getUnlockStatsBlocking()
                            showStats = true
                        },
                        onYes = {
                            stopPlayback()
                            preferences.recordAccomplishedBlocking(AppPreferences.UNLOCK_STATS_ID)
                            finishReflection()
                        },
                    )
                }
            }
        }
    }

    private fun togglePlayback() {
        isPlaying = audioPlayer.togglePlayback()
        if (!isPlaying) {
            playbackProgress = audioPlayer.playbackProgress
        }
    }

    private fun stopPlayback() {
        audioPlayer.pause()
        isPlaying = false
        playbackProgress = 0f
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        abandonReflectionIfIncomplete()
    }

    override fun onStop() {
        super.onStop()
        if (!reflectionCompleted && !isChangingConfigurations) {
            abandonReflectionIfIncomplete()
            finish()
        }
    }

    override fun onDestroy() {
        audioPlayer.release()
        if (!reflectionCompleted) {
            SessionManager.cancelUnlockReflection()
        }
        super.onDestroy()
    }

    private fun abandonReflectionIfIncomplete() {
        if (!reflectionCompleted && !isChangingConfigurations) {
            stopPlayback()
            SessionManager.cancelUnlockReflection()
        }
    }

    private fun finishReflection() {
        reflectionCompleted = true
        val preferences = (application as StillnessApp).preferences
        preferences.clearPendingUnlockReflectionBlocking()
        SessionManager.onUnlockAfterScreenDismissed()

        if (chainToBeforeUnlock && preferences.isUnlockMonitoringEnabledBlocking()) {
            UnlockMonitor.launchBeforeUnlockFromReflection(this)
        }

        finish()
    }

    companion object {
        private const val EXTRA_CHAIN_TO_BEFORE_UNLOCK = "extra_chain_to_before_unlock"

        fun createIntent(context: Context, chainToBeforeUnlock: Boolean): Intent {
            return Intent(context, AfterUnlockActivity::class.java).apply {
                putExtra(EXTRA_CHAIN_TO_BEFORE_UNLOCK, chainToBeforeUnlock)
            }
        }
    }
}
