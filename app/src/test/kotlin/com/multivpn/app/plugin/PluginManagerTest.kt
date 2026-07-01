package com.multivpn.app.plugin

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PluginManagerTest {

    @Test
    fun installPlugin_marksPluginAsInstalledAndSavesManifest() {
        val storageDir = createTempDir()
        val manager = PluginManager(storageDir)

        val installed = manager.installPlugin("xray")

        assertTrue(installed)
        assertTrue(manager.isInstalled("xray"))
        assertTrue(File(storageDir, "xray.json").exists())
    }

    @Test
    fun enablePlugin_requiresInstallationFirst() {
        val storageDir = createTempDir()
        val manager = PluginManager(storageDir)

        val enabled = manager.enablePlugin("sing")

        assertFalse(enabled)
        assertFalse(manager.isEnabled("sing"))
    }
}
