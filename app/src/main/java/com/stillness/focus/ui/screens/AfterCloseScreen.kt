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
import com.stillness.focus.ui.theme.OnSecondary
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.SurfaceContainerHigh

@Composable
fun AfterCloseScreen(
    purposeNote: String,
    onNo: () -> Unit,
    onYes: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        Text(
            text = "Have you accomplished\nyour purpose?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(48.dp))

        PurposeNoteCard(purposeNote = purposeNote)

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
private fun PurposeNoteCard(purposeNote: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play purpose note",
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
                    text = formatDuration(purposeNote),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { 0.4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SecondaryTeal,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = purposeNote.ifBlank { "No purpose recorded." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            WaveformPlaceholder()
        }
    }
}

@Composable
private fun WaveformPlaceholder() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
    ) {
        listOf(12, 24, 16, 32, 20, 28, 14, 26, 18, 30, 16, 22).forEach { height ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SecondaryTeal.copy(alpha = 0.45f)),
            )
        }
    }
}

private fun formatDuration(text: String): String {
    val seconds = (text.length.coerceAtMost(60)).coerceAtLeast(1)
    return "0:${seconds.toString().padStart(2, '0')}"
}
