package com.stillness.focus.util

import android.media.MediaPlayer

class PurposeAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    val durationMs: Int
        get() = mediaPlayer?.duration?.coerceAtLeast(0) ?: 0

    val currentPositionMs: Int
        get() = mediaPlayer?.currentPosition?.coerceAtLeast(0) ?: 0

    val playbackProgress: Float
        get() {
            val duration = durationMs
            if (duration <= 0) return 0f
            return currentPositionMs.toFloat() / duration
        }

    fun prepare(filePath: String, onComplete: () -> Unit) {
        release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            setOnCompletionListener {
                seekTo(0)
                onComplete()
            }
            prepare()
        }
    }

    fun play() {
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun togglePlayback(): Boolean {
        val player = mediaPlayer ?: return false
        if (player.isPlaying) {
            player.pause()
        } else {
            player.start()
        }
        return player.isPlaying
    }

    fun release() {
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }
}

fun formatAudioDurationMs(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    } else {
        "0:${seconds.toString().padStart(2, '0')}"
    }
}
