package com.stillness.focus.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

data class PurposeRecording(
    val filePath: String,
    val durationMs: Long,
    val waveformSamples: List<Float>,
)

class PurposeAudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private val waveformSamples = mutableListOf<Float>()
    private var startTimeMs = 0L

    val isRecording: Boolean
        get() = mediaRecorder != null

    fun start(): Boolean {
        if (isRecording) return false

        val file = File(context.cacheDir, "purpose_${System.currentTimeMillis()}.m4a")
        outputFile = file
        waveformSamples.clear()
        startTimeMs = System.currentTimeMillis()

        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            true
        } catch (_: Exception) {
            cleanup()
            false
        }
    }

    fun captureAmplitude() {
        val recorder = mediaRecorder ?: return
        val maxAmplitude = recorder.maxAmplitude.coerceAtLeast(1)
        val normalized = (maxAmplitude / 32767f).coerceIn(0.1f, 1f)
        waveformSamples.add(normalized)
    }

    fun stop(): PurposeRecording? {
        val recorder = mediaRecorder ?: return null
        val file = outputFile ?: return null

        return try {
            recorder.stop()
            val durationMs = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(0L)
            if (file.exists() && file.length() > 0L && durationMs > 0L) {
                PurposeRecording(
                    filePath = file.absolutePath,
                    durationMs = durationMs,
                    waveformSamples = waveformSamples.toList(),
                )
            } else {
                file.delete()
                null
            }
        } catch (_: Exception) {
            file.delete()
            null
        } finally {
            releaseRecorder()
        }
    }

    fun cancel() {
        outputFile?.delete()
        cleanup()
    }

    private fun cleanup() {
        releaseRecorder()
        outputFile = null
        waveformSamples.clear()
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {
        }
        mediaRecorder = null
    }
}
