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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.ui.components.AccomplishmentProgressCard
import com.stillness.focus.ui.components.StillnessOutlineButton
import com.stillness.focus.ui.components.StillnessPrimaryButton
import com.stillness.focus.ui.components.StillnessTopBar
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.SurfaceContainerHigh

@Composable
fun PermissionsScreen(
    accessibilityEnabled: Boolean,
    onEnableAccessibility: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
    ) {
        StillnessTopBar(title = "Stillness")

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (accessibilityEnabled) "You're all set" else "Accessibility required",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = if (accessibilityEnabled) {
                "Stillness can now detect when you open or leave your selected apps."
            } else {
                "Stillness needs Accessibility access to work. Without it, the app cannot intercept your selected apps. Your data stays on your device."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!accessibilityEnabled) {
            StillnessPrimaryButton(
                text = "Enable in Settings",
                onClick = onEnableAccessibility,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            )
        } else {
            StillnessPrimaryButton(
                text = "Continue",
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            )
        }
    }
}

@Composable
fun HomeScreen(
    blockedCount: Int,
    globalStats: PurposeStats,
    onViewStats: () -> Unit,
    onEditApps: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
    ) {
        StillnessTopBar(title = "Stillness")

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You're protected",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = if (blockedCount > 0) {
                "Stillness is watching $blockedCount selected app${if (blockedCount == 1) "" else "s"}."
            } else {
                "Select apps to start using Stillness."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProtectionStatusCard(blockedCount = blockedCount)

        if (globalStats.hasData) {
            Spacer(modifier = Modifier.height(16.dp))
            AccomplishmentProgressCard(stats = globalStats)
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StillnessPrimaryButton(
                text = "View statistics",
                onClick = onViewStats,
            )
            StillnessOutlineButton(
                text = "Edit selected apps",
                onClick = onEditApps,
            )
        }
    }
}

@Composable
private fun ProtectionStatusCard(blockedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SecondaryTeal,
                modifier = Modifier.padding(end = 16.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (blockedCount > 0) {
                        "$blockedCount app${if (blockedCount == 1) "" else "s"} monitored"
                    } else {
                        "No apps selected"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (blockedCount > 0) {
                        "Active protection"
                    } else {
                        "Choose apps to get started"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
