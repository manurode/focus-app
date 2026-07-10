package com.stillness.focus.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.stillness.focus.data.InstalledApp
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.data.aggregateStats
import com.stillness.focus.ui.components.AccomplishmentProgressCard
import com.stillness.focus.ui.components.MindfulPausesCard
import com.stillness.focus.ui.components.StatCard
import com.stillness.focus.ui.components.StillnessTopBar
import com.stillness.focus.ui.components.reflectionMessage
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.SurfaceContainerHigh
import com.stillness.focus.ui.theme.TertiaryLavender

data class AppStatsEntry(
    val app: InstalledApp,
    val stats: PurposeStats,
)

@Composable
fun StatsOverviewScreen(
    appStats: List<AppStatsEntry>,
    unlockStats: PurposeStats,
    unlockMonitoringEnabled: Boolean,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit,
) {
    val globalStats = aggregateStats(appStats.associate { it.app.packageName to it.stats }) + unlockStats
    val appsWithData = appStats.filter { it.stats.hasData }
        .sortedByDescending { it.stats.total + it.stats.preventedEntries }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
    ) {
        StillnessTopBar(title = "Statistics", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Your progress",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "How intentional you've been across all monitored apps.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (!globalStats.hasData) {
                EmptyStatsCard()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label = "Accomplished",
                        count = globalStats.accomplished,
                        accentColor = SecondaryTeal,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Not accomplished",
                        count = globalStats.notAccomplished,
                        accentColor = TertiaryLavender,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AccomplishmentProgressCard(stats = globalStats)

                if (globalStats.preventedEntries > 0) {
                    Spacer(modifier = Modifier.height(20.dp))
                    MindfulPausesCard(
                        count = globalStats.preventedEntries,
                        description = "Stillness stopped you from opening apps without a clear purpose — " +
                            "catching those automatic opens before they start.",
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = reflectionMessage(globalStats),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                )
            }

            if (appsWithData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "By app",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Tap an app to see its full breakdown.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )

                appsWithData.forEach { entry ->
                    AppStatsRow(
                        entry = entry,
                        onClick = { onAppClick(entry.app.packageName) },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (unlockMonitoringEnabled || unlockStats.hasData) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Phone unlock",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "How intentional you've been when unlocking your phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )

                UnlockStatsCard(
                    stats = unlockStats,
                    monitoringEnabled = unlockMonitoringEnabled,
                )
            } else if (appStats.isNotEmpty() && !globalStats.hasData) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Open a monitored app to start collecting statistics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmptyStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Statistics appear after you open monitored apps and reflect on your purpose.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AppStatsRow(
    entry: AppStatsEntry,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                bitmap = entry.app.icon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
            ) {
                Text(
                    text = entry.app.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = buildAppStatsSummary(entry.stats),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            if (entry.stats.total > 0) {
                Text(
                    text = "${(entry.stats.accomplishmentRate * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryTeal,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun buildAppStatsSummary(stats: PurposeStats): String {
    val parts = mutableListOf<String>()
    if (stats.accomplished > 0) {
        parts += "${stats.accomplished} accomplished"
    }
    if (stats.notAccomplished > 0) {
        parts += "${stats.notAccomplished} not accomplished"
    }
    if (stats.preventedEntries > 0) {
        parts += "${stats.preventedEntries} paused"
    }
    return parts.joinToString(" · ").ifEmpty { "No visits yet" }
}

@Composable
private fun UnlockStatsCard(
    stats: PurposeStats,
    monitoringEnabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .size(40.dp),
                )
                Column {
                    Text(
                        text = "Device unlock",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (monitoringEnabled) "Monitoring active" else "Monitoring off",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (!stats.hasData) {
                Text(
                    text = "Unlock your phone to start collecting statistics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = buildAppStatsSummary(stats),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (stats.total > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AccomplishmentProgressCard(stats = stats)
                }

                if (stats.preventedEntries > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    MindfulPausesCard(
                        count = stats.preventedEntries,
                        description = "Stillness stopped you from unlocking your phone without a clear purpose.",
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = reflectionMessage(stats, "your phone"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
