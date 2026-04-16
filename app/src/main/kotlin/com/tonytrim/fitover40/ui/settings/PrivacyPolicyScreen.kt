package com.tonytrim.fitover40.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "FitOver40 Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Effective Date: April 16, 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                PolicySection(
                    title = "Data Collection",
                    content = "Your workout history, plans, and training levels are stored on-device. If you sign in, your email address and authentication session are also stored locally so the app can reconnect to your backend."
                )
            }

            item {
                PolicySection(
                    title = "What is NOT Collected",
                    content = "FitOver40 does not include ads or third-party analytics. Outdoor location is used for active workout tracking only. Account data is only sent to your configured backend for authentication."
                )
            }

            item {
                PolicySection(
                    title = "Data Deletion",
                    content = "You have full control over your data. You can clear all workout history and settings at any time from the 'Settings' screen within the app."
                )
            }

            item {
                PolicySection(
                    title = "Contact",
                    content = "If you have any questions about this policy, please contact us at furbert.trim@gmail.com"
                )
            }
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
