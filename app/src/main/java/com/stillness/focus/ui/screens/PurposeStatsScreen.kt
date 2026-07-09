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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.ui.components.StillnessPrimaryButton
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.SurfaceContainerHigh
import com.stillness.focus.ui.theme.TertiaryLavender

@Composable
fun PurposeStatsScreen(
    appLabel: String,
    stats: PurposeStats,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Your track record",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Every time you open $appLabel, Stillness asks why. Here's how it's going.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    label = "Accomplished",
                    count = stats.accomplished,
                    accentColor = SecondaryTeal,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Not accomplished",
                    count = stats.notAccomplished,
                    accentColor = TertiaryLavender,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${stats.total} visits",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${(stats.accomplishmentRate * 100).toInt()}% on purpose",
                            style = MaterialTheme.typography.labelLarge,
                            color = SecondaryTeal,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { stats.accomplishmentRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SecondaryTeal,
                        trackColor = TertiaryLavender.copy(alpha = 0.35f),
                    )
                }
            }

            if (stats.preventedEntries > 0) {
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stats.preventedEntries.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            color = SecondaryTeal,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (stats.preventedEntries == 1) {
                                "Mindful pause"
                            } else {
                                "Mindful pauses"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Stillness stopped you from opening $appLabel without a clear purpose — " +
                                "catching those automatic opens before they start.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = reflectionMessage(stats),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        StillnessPrimaryButton(
            text = "Continue",
            onClick = onContinue,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth(0.4f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun reflectionMessage(stats: PurposeStats): String {
    if (stats.total <= 1) {
        return "This is just the beginning. Pay attention to whether ${
            if (stats.notAccomplished > 0) "this app actually helps you" else "you stay intentional"
        }."
    }

    val failureRate = 1f - stats.accomplishmentRate
    return when {
        failureRate >= 0.7f ->
            "Most of the time you leave without accomplishing your purpose. " +
                "Maybe this app isn't helping you — consider using it less."
        failureRate >= 0.5f ->
            "More often than not, you don't accomplish what you set out to do. " +
                "Is opening this app worth it?"
        failureRate >= 0.3f ->
            "You're not always achieving your purpose here. " +
                "Notice the pattern before it becomes a habit."
        else ->
            "You're mostly staying intentional. Keep holding yourself to that standard."
    }
}
