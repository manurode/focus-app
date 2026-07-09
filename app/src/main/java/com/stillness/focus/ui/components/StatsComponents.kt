package com.stillness.focus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.SurfaceContainerHigh
import com.stillness.focus.ui.theme.TertiaryLavender

@Composable
fun StatCard(
    label: String,
    count: Int,
    accentColor: Color,
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

@Composable
fun AccomplishmentProgressCard(
    stats: PurposeStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = if (stats.total == 1) "1 visit" else "${stats.total} visits",
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
}

@Composable
fun MindfulPausesCard(
    count: Int,
    description: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = SecondaryTeal,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (count == 1) "Mindful pause" else "Mindful pauses",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

fun reflectionMessage(stats: PurposeStats, appLabel: String? = null): String {
    val subject = appLabel ?: "this app"

    if (stats.total <= 1) {
        return "This is just the beginning. Pay attention to whether ${
            if (stats.notAccomplished > 0) "$subject actually helps you" else "you stay intentional"
        }."
    }

    val failureRate = 1f - stats.accomplishmentRate
    return when {
        failureRate >= 0.7f ->
            "Most of the time you leave without accomplishing your purpose. " +
                "Maybe $subject isn't helping you — consider using it less."
        failureRate >= 0.5f ->
            "More often than not, you don't accomplish what you set out to do. " +
                "Is opening $subject worth it?"
        failureRate >= 0.3f ->
            "You're not always achieving your purpose here. " +
                "Notice the pattern before it becomes a habit."
        else ->
            "You're mostly staying intentional. Keep holding yourself to that standard."
    }
}
