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

    @Volatile
    var unlockSessionActive = false

    @Volatile
    var unlockPurposeNote: String = ""

    @Volatile
    var unlockPurposeAudioPath: String? = null

    @Volatile
    var unlockPurposeAudioDurationMs: Long = 0L

    @Volatile
    var unlockPurposeWaveformSamples: List<Float> = emptyList()

    val isUnlockBeforeShowing = AtomicBoolean(false)
    val isUnlockAfterShowing = AtomicBoolean(false)

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

    fun grantUnlockAccess(
        purpose: String,
        audioPath: String? = null,
        audioDurationMs: Long = 0L,
        waveformSamples: List<Float> = emptyList(),
    ) {
        unlockSessionActive = true
        unlockPurposeNote = purpose
        unlockPurposeAudioPath = audioPath
        unlockPurposeAudioDurationMs = audioDurationMs
        unlockPurposeWaveformSamples = waveformSamples
        isUnlockBeforeShowing.set(false)
    }

    fun loadPendingUnlockReflection(
        purpose: String,
        audioPath: String?,
        audioDurationMs: Long,
        waveformSamples: List<Float>,
    ) {
        unlockPurposeNote = purpose
        unlockPurposeAudioPath = audioPath
        unlockPurposeAudioDurationMs = audioDurationMs
        unlockPurposeWaveformSamples = waveformSamples
    }

    fun clearUnlockSession() {
        unlockPurposeAudioPath?.let { path ->
            runCatching { File(path).delete() }
        }
        unlockSessionActive = false
        unlockPurposeNote = ""
        unlockPurposeAudioPath = null
        unlockPurposeAudioDurationMs = 0L
        unlockPurposeWaveformSamples = emptyList()
        isUnlockBeforeShowing.set(false)
        isUnlockAfterShowing.set(false)
    }

    fun onUnlockBeforeScreenDismissed() {
        isUnlockBeforeShowing.set(false)
    }

    fun cancelUnlockBeforeOpen() = onUnlockBeforeScreenDismissed()

    fun onUnlockAfterScreenDismissed() {
        isUnlockAfterShowing.set(false)
        clearUnlockSession()
    }

    fun cancelUnlockReflection() = onUnlockAfterScreenDismissed()
}
