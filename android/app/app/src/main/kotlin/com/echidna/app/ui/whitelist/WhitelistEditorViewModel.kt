package com.echidna.app.ui.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echidna.app.data.ControlStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppEntry(
    val packageName: String,
    val enabled: Boolean,
    val presetId: String
)

class WhitelistEditorViewModel : ViewModel() {
    private val repo = ControlStateRepository

    private val _entries = MutableStateFlow<List<AppEntry>>(emptyList())
    val entries: StateFlow<List<AppEntry>> = _entries.asStateFlow()

    val presets = repo.presets

    fun loadEntries(whitelistMap: Map<String, Boolean>, bindingsMap: Map<String, String>) {
        val combined = mutableMapOf<String, AppEntry>()
        whitelistMap.forEach { (pkg, enabled) ->
            combined[pkg] = AppEntry(pkg, enabled, bindingsMap[pkg].orEmpty())
        }
        bindingsMap.forEach { (pkg, presetId) ->
            if (pkg !in combined) {
                combined[pkg] = AppEntry(pkg, whitelistMap[pkg] ?: false, presetId)
            }
        }
        _entries.value = combined.values.sortedBy { it.packageName }
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        _entries.value = _entries.value.map {
            if (it.packageName == packageName) it.copy(enabled = enabled) else it
        }
        repo.updateWhitelist(packageName, enabled)
    }

    fun setAppPreset(packageName: String, presetId: String) {
        _entries.value = _entries.value.map {
            if (it.packageName == packageName) it.copy(presetId = presetId) else it
        }
        repo.setAppPresetBinding(packageName, presetId)
    }

    fun addApp(packageName: String) {
        if (!isValidPackageName(packageName)) return
        if (_entries.value.any { it.packageName == packageName }) return
        _entries.value = _entries.value + AppEntry(packageName, true, "")
        repo.updateWhitelist(packageName, true)
    }

    fun removeApp(packageName: String) {
        _entries.value = _entries.value.filterNot { it.packageName == packageName }
        repo.updateWhitelist(packageName, false)
    }

    private fun isValidPackageName(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank() || trimmed.length > 128) return false
        if (!trimmed.contains('.')) return false
        return trimmed.all { it.isLetterOrDigit() || it == '.' || it == '_' || it == ':' }
    }
}
