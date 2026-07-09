package com.stillness.focus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillness.focus.ui.components.StillnessPrimaryButton
import com.stillness.focus.ui.components.StillnessTopBar
import com.stillness.focus.ui.theme.SecondaryTeal
import com.stillness.focus.ui.theme.SurfaceContainerHigh

@Composable
fun BeforeOpenScreen(
    appLabel: String,
    purpose: String,
    onPurposeChange: (String) -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
    ) {
        StillnessTopBar(title = "Stillness", onBack = onBack)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "What is your purpose?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Speak naturally or type below to clarify your intent for opening $appLabel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 48.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SecondaryTeal.copy(alpha = 0.35f),
                                SecondaryTeal.copy(alpha = 0f),
                            ),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .border(1.dp, SecondaryTeal.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = SecondaryTeal,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }

        OutlinedTextField(
            value = purpose,
            onValueChange = onPurposeChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("I want to focus on…") },
            leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = null, tint = SecondaryTeal)
            },
            singleLine = false,
            minLines = 1,
            maxLines = 3,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SecondaryTeal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = SurfaceContainerHigh,
                unfocusedContainerColor = SurfaceContainerHigh,
            ),
        )

        StillnessPrimaryButton(
            text = "Proceed",
            onClick = onProceed,
            enabled = purpose.isNotBlank(),
            modifier = Modifier.padding(vertical = 24.dp),
        )
    }
}
