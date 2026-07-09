package com.stillness.focus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillness.focus.ui.components.StillnessPrimaryButton
import com.stillness.focus.ui.components.StillnessTopBar

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
            text = "One more step",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Stillness needs Accessibility access to detect when you open or leave your selected apps. Your data stays on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!accessibilityEnabled) {
            StillnessPrimaryButton(
                text = "Enable in Settings",
                onClick = onEnableAccessibility,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        StillnessPrimaryButton(
            text = if (accessibilityEnabled) "Continue" else "I've enabled it",
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        )
    }
}

@Composable
fun HomeScreen(
    blockedCount: Int,
    accessibilityEnabled: Boolean,
    onEditApps: () -> Unit,
    onEnableAccessibility: () -> Unit,
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
            modifier = Modifier.padding(top = 12.dp),
        )

        if (!accessibilityEnabled) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Accessibility is disabled. Stillness cannot intercept apps until you enable it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(16.dp))
            StillnessPrimaryButton(
                text = "Enable in Settings",
                onClick = onEnableAccessibility,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        StillnessPrimaryButton(
            text = "Edit selected apps",
            onClick = onEditApps,
            modifier = Modifier.padding(bottom = 32.dp),
        )
    }
}
