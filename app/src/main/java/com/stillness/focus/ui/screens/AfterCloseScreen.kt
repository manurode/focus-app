package com.stillness.focus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillness.focus.ui.components.StillnessOutlineButton
import com.stillness.focus.ui.components.StillnessPrimaryButton
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.SurfaceContainerHigh
import com.stillness.focus.util.formatAudioDurationMs

@Composable
fun AfterCloseScreen(
    purposeNote: String,
    audioDurationMs: Long = 0L,
    waveformSamples: List<Float> = emptyList(),
    isPlaying: Boolean = false,
    playbackProgress: Float = 0f,
    onPlayPause: () -> Unit = {},
    onNo: () -> Unit,
    onYes: () -> Unit,
) {
    val hasAudio = audioDurationMs > 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Have you accomplished\nyour purpose?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(48.dp))

        PurposeNoteCard(
            purposeNote = purposeNote,
            hasAudio = hasAudio,
            audioDurationMs = audioDurationMs,
            waveformSamples = waveformSamples,
            isPlaying = isPlaying,
            playbackProgress = playbackProgress,
            onPlayPause = onPlayPause,
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp),
        ) {
            StillnessPrimaryButton(text = "NO", onClick = onNo)
            StillnessOutlineButton(text = "YES", onClick = onYes)
        }
    }
}

@Composable
private fun PurposeNoteCard(
    purposeNote: String,
    hasAudio: Boolean,
    audioDurationMs: Long,
    waveformSamples: List<Float>,
    isPlaying: Boolean,
    playbackProgress: Float,
    onPlayPause: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (hasAudio) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface),
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause purpose note" else "Play purpose note",
                            tint = MaterialTheme.colorScheme.background,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "PURPOSE NOTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatAudioDurationMs(audioDurationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { playbackProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SecondaryTeal,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (purposeNote.isNotBlank()) {
                Text(
                    text = purposeNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else if (!hasAudio) {
                Text(
                    text = "No purpose recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (hasAudio) {
                Spacer(modifier = Modifier.height(16.dp))
                AudioWaveform(
                    samples = waveformSamples,
                    playbackProgress = playbackProgress,
                )
            }
        }
    }
}

@Composable
private fun AudioWaveform(
    samples: List<Float>,
    playbackProgress: Float,
) {
    val displaySamples = if (samples.isNotEmpty()) {
        samples
    } else {
        listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.45f, 0.75f, 0.55f, 0.85f, 0.5f, 0.65f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
    ) {
        displaySamples.forEachIndexed { index, sample ->
            val barProgress = index.toFloat() / displaySamples.size
            val isPlayed = barProgress <= playbackProgress
            val height = (12 + sample * 20).dp

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPlayed) {
                            SecondaryTeal
                        } else {
                            SecondaryTeal.copy(alpha = 0.45f)
                        },
                    ),
            )
        }
    }
}
