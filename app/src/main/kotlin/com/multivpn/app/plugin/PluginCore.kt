package com.multivpn.app.plugin

interface PluginCore {
    val id: String
    val displayName: String
    val description: String
    fun isEnabled(): Boolean
    fun onEnable()
    fun onDisable()
}

class XrayCore(private val pluginManager: PluginManager) : PluginCore {
    override val id = "xray"
    override val displayName = "XRAY"
    override val description = "Downloadable XRAY core"
    override fun isEnabled() = pluginManager.isEnabled(id)
    override fun onEnable() = pluginManager.enablePlugin(id)
    override fun onDisable() = pluginManager.disablePlugin(id)
}

class SingCore(private val pluginManager: PluginManager) : PluginCore {
    override val id = "sing"
    override val displayName = "SING"
    override val description = "Downloadable SING core"
    override fun isEnabled() = pluginManager.isEnabled(id)
    override fun onEnable() = pluginManager.enablePlugin(id)
    override fun onDisable() = pluginManager.disablePlugin(id)
}

class BixCore(private val pluginManager: PluginManager) : PluginCore {
    override val id = "bix"
    override val displayName = "BIX"
    override val description = "Downloadable BIX core"
    override fun isEnabled() = pluginManager.isEnabled(id)
    override fun onEnable() = pluginManager.enablePlugin(id)
    override fun onDisable() = pluginManager.disablePlugin(id)
}

class ClashCore(private val pluginManager: PluginManager) : PluginCore {
    override val id = "clash"
    override val displayName = "CLASH"
    override val description = "Downloadable CLASH core"
    override fun isEnabled() = pluginManager.isEnabled(id)
    override fun onEnable() = pluginManager.enablePlugin(id)
    override fun onDisable() = pluginManager.disablePlugin(id)
}
