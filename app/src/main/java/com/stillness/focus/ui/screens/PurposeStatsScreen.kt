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
import com.stillness.focus.ui.components.StillnessPrimaryButton
import com.stillness.focus.ui.components.reflectionMessage
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.TertiaryLavender

@Composable
fun PurposeStatsScreen(
    subjectLabel: String,
    stats: PurposeStats,
    onContinue: () -> Unit,
    statsDescription: String = "Every time you open $subjectLabel, Stillness asks why. Here's how it's going.",
    mindfulPauseDescription: String = "Stillness stopped you from opening $subjectLabel without a clear purpose — " +
        "catching those automatic opens before they start.",
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
                text = statsDescription,
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

            AccomplishmentProgressCard(stats = stats)

            if (stats.preventedEntries > 0) {
                Spacer(modifier = Modifier.height(24.dp))
                MindfulPausesCard(
                    count = stats.preventedEntries,
                    description = mindfulPauseDescription,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = reflectionMessage(stats, subjectLabel),
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
