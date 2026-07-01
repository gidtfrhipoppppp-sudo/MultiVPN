package com.multivpn.app.plugin

class PluginRepository(private val pluginManager: PluginManager) {
    private val cores = listOf(
        XrayCore(pluginManager),
        SingCore(pluginManager),
        BixCore(pluginManager),
        ClashCore(pluginManager)
    )

    fun availableCores(): List<PluginCore> = cores

    fun installCore(id: String): Boolean = pluginManager.installPlugin(id)

    fun enableCore(id: String): Boolean = pluginManager.enablePlugin(id)

    fun disableCore(id: String): Boolean = pluginManager.disablePlugin(id)
}
