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
import com.stillness.focus.data.InstalledAppsRepository
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.monitor.SessionManager
import com.stillness.focus.ui.screens.AfterCloseScreen
import com.stillness.focus.ui.screens.PurposeStatsScreen
import com.stillness.focus.ui.theme.StillnessTheme
import com.stillness.focus.util.PurposeAudioPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class AfterCloseActivity : ComponentActivity() {
    private val audioPlayer = PurposeAudioPlayer()
    private var isPlaying by mutableStateOf(false)
    private var playbackProgress by mutableFloatStateOf(0f)
    private var reflectionCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val closedPackage = intent.getStringExtra(EXTRA_CLOSED_PACKAGE)
        val purposeNote = SessionManager.purposeNote
        val audioPath = SessionManager.purposeAudioPath
        val audioDurationMs = SessionManager.purposeAudioDurationMs
        val waveformSamples = SessionManager.purposeWaveformSamples
        var hasPlayableAudio = false

        if (closedPackage == null) {
            SessionManager.isAfterScreenShowing.set(false)
            finish()
            return
        }

        if (audioPath != null) {
            hasPlayableAudio = runCatching {
                audioPlayer.prepare(audioPath) {
                    isPlaying = false
                    playbackProgress = 0f
                }
            }.isSuccess
        }

        val preferences = (application as StillnessApp).preferences
        val appLabel = InstalledAppsRepository(this).getAppLabel(closedPackage)

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
                        appLabel = appLabel,
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
                            preferences.recordNotAccomplishedBlocking(closedPackage)
                            stats = preferences.getStatsBlocking(closedPackage)
                            showStats = true
                        },
                        onYes = {
                            stopPlayback()
                            preferences.recordAccomplishedBlocking(closedPackage)
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
            SessionManager.cancelReflection()
        }
        super.onDestroy()
    }

    private fun abandonReflectionIfIncomplete() {
        if (!reflectionCompleted && !isChangingConfigurations) {
            stopPlayback()
            SessionManager.cancelReflection()
        }
    }

    private fun finishReflection() {
        reflectionCompleted = true
        SessionManager.onAfterScreenDismissed()
        finish()
    }

    companion object {
        private const val EXTRA_CLOSED_PACKAGE = "extra_closed_package"

        fun createIntent(context: Context, packageName: String): Intent {
            return Intent(context, AfterCloseActivity::class.java).apply {
                putExtra(EXTRA_CLOSED_PACKAGE, packageName)
            }
        }
    }
}
