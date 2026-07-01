package com.multivpn.app.plugin

import java.io.File
import java.util.concurrent.ConcurrentHashMap

class PluginManager(private val storageDir: File) {

    private val installedPlugins = ConcurrentHashMap<String, PluginState>()

    init {
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        loadPersistedPlugins()
    }

    fun installPlugin(id: String): Boolean {
        if (id.isBlank()) return false
        val pluginId = id.lowercase()
        val manifestFile = File(storageDir, "$pluginId.json")
        val manifest = PluginManifest(pluginId, "downloadable", enabled = false)
        manifestFile.writeText(manifest.toJson())
        installedPlugins[pluginId] = PluginState(installed = true, enabled = false)
        return true
    }

    fun enablePlugin(id: String): Boolean {
        val pluginId = id.lowercase()
        val pluginState = installedPlugins[pluginId] ?: return false
        if (!pluginState.installed) return false
        installedPlugins[pluginId] = pluginState.copy(enabled = true)
        persistPluginState(pluginId, installedPlugins[pluginId]!!)
        return true
    }

    fun disablePlugin(id: String): Boolean {
        val pluginId = id.lowercase()
        val pluginState = installedPlugins[pluginId] ?: return false
        installedPlugins[pluginId] = pluginState.copy(enabled = false)
        persistPluginState(pluginId, installedPlugins[pluginId]!!)
        return true
    }

    fun isInstalled(id: String): Boolean = installedPlugins[id.lowercase()]?.installed == true

    fun isEnabled(id: String): Boolean = installedPlugins[id.lowercase()]?.enabled == true

    fun listPlugins(): List<String> = installedPlugins.keys.sorted()

    private fun loadPersistedPlugins() {
        storageDir.listFiles()?.filter { it.extension == "json" && it.nameWithoutExtension != "" }?.forEach { file ->
            val pluginId = file.nameWithoutExtension.lowercase()
            val state = PluginState(installed = true, enabled = false)
            installedPlugins[pluginId] = state
        }
    }

    private fun persistPluginState(id: String, state: PluginState) {
        val manifestFile = File(storageDir, "$id.json")
        manifestFile.writeText(PluginManifest(id, "downloadable", state.enabled).toJson())
    }
}

data class PluginState(val installed: Boolean, val enabled: Boolean)
data class PluginManifest(val id: String, val source: String, val enabled: Boolean) {
    fun toJson(): String = """{"id":"$id","source":"$source","enabled":$enabled}"""
}
