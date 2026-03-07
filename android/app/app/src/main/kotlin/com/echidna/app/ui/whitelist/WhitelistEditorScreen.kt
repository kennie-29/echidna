package com.echidna.app.ui.whitelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.echidna.app.model.Preset

@Composable
fun WhitelistEditorScreen(viewModel: WhitelistEditorViewModel) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()

    var newPackageName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Per-App Whitelist", style = MaterialTheme.typography.headlineSmall)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newPackageName,
                onValueChange = { newPackageName = it },
                label = { Text("Package name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    viewModel.addApp(newPackageName.trim())
                    newPackageName = ""
                },
                enabled = newPackageName.isNotBlank()
            ) {
                Text("Add")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(entries, key = { it.packageName }) { entry ->
                AppWhitelistCard(
                    entry = entry,
                    presets = presets,
                    onToggle = { viewModel.toggleApp(entry.packageName, it) },
                    onPresetChange = { viewModel.setAppPreset(entry.packageName, it) },
                    onRemove = { viewModel.removeApp(entry.packageName) }
                )
            }
        }
    }
}

@Composable
private fun AppWhitelistCard(
    entry: AppEntry,
    presets: List<Preset>,
    onToggle: (Boolean) -> Unit,
    onPresetChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val boundPresetName = presets.firstOrNull { it.id == entry.presetId }?.name ?: "Default"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.packageName,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Preset: $boundPresetName",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = entry.enabled, onCheckedChange = onToggle)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { expanded = true }) { Text("Bind Preset") }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Default") },
                        onClick = {
                            onPresetChange("")
                            expanded = false
                        }
                    )
                    presets.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset.name) },
                            onClick = {
                                onPresetChange(preset.id)
                                expanded = false
                            }
                        )
                    }
                }
                TextButton(onClick = onRemove) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
