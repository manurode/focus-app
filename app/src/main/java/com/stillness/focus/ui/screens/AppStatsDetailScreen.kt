package com.stillness.focus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.ui.components.AccomplishmentProgressCard
import com.stillness.focus.ui.components.MindfulPausesCard
import com.stillness.focus.ui.components.StatCard
import com.stillness.focus.ui.components.StillnessTopBar
import com.stillness.focus.ui.components.reflectionMessage
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.TertiaryLavender

@Composable
fun AppStatsDetailScreen(
    appLabel: String,
    stats: PurposeStats,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
    ) {
        StillnessTopBar(title = appLabel, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            AccomplishmentProgressCard(stats = stats)

            if (stats.preventedEntries > 0) {
                Spacer(modifier = Modifier.height(20.dp))
                MindfulPausesCard(
                    count = stats.preventedEntries,
                    description = "Stillness stopped you from opening $appLabel without a clear purpose — " +
                        "catching those automatic opens before they start.",
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = reflectionMessage(stats, appLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
