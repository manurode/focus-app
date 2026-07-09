package com.stillness.focus.monitor

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object SessionManager {
    @Volatile
    var allowedPackage: String? = null

    @Volatile
    var activeBlockedPackage: String? = null

    @Volatile
    var purposeNote: String = ""

    @Volatile
    var purposeAudioPath: String? = null

    @Volatile
    var purposeAudioDurationMs: Long = 0L

    @Volatile
    var purposeWaveformSamples: List<Float> = emptyList()

    val isBeforeScreenShowing = AtomicBoolean(false)
    val isAfterScreenShowing = AtomicBoolean(false)

    fun grantAccess(
        packageName: String,
        purpose: String,
        audioPath: String? = null,
        audioDurationMs: Long = 0L,
        waveformSamples: List<Float> = emptyList(),
    ) {
        allowedPackage = packageName
        activeBlockedPackage = packageName
        purposeNote = purpose
        purposeAudioPath = audioPath
        purposeAudioDurationMs = audioDurationMs
        purposeWaveformSamples = waveformSamples
        isBeforeScreenShowing.set(false)
    }

    fun markActiveInBlockedApp(packageName: String) {
        activeBlockedPackage = packageName
    }

    fun clearSession() {
        purposeAudioPath?.let { path ->
            runCatching { File(path).delete() }
        }
        allowedPackage = null
        activeBlockedPackage = null
        purposeNote = ""
        purposeAudioPath = null
        purposeAudioDurationMs = 0L
        purposeWaveformSamples = emptyList()
        isBeforeScreenShowing.set(false)
        isAfterScreenShowing.set(false)
    }

    fun onBeforeScreenDismissed() {
        isBeforeScreenShowing.set(false)
    }

    fun cancelBeforeOpen() = onBeforeScreenDismissed()

    fun onAfterScreenDismissed() {
        isAfterScreenShowing.set(false)
        clearSession()
    }

    fun cancelReflection() = onAfterScreenDismissed()
}
