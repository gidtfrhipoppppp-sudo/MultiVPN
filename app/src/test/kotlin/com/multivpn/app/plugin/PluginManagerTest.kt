package com.multivpn.app.plugin

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PluginManagerTest {

    @Test
    fun freshCore_isNotInstalled_andHasNoBinaryPath() {
        val storageDir = createTempDir()
        val manager = PluginManager(storageDir)

        assertFalse(manager.isInstalled("sing-box"))
        assertNull(manager.binaryPath("sing-box"))
    }

    @Test
    fun uninstall_removesPersistedState() {
        val storageDir = createTempDir()

        // Simulate a previously persisted core so loadPersistedCores picks it up.
        val coreDir = File(storageDir, "xray").apply { mkdirs() }
        val binary = File(coreDir, "xray").apply { writeText("stub") }
        val manifest = org.json.JSONObject()
            .put("id", "xray")
            .put("binaryPath", binary.absolutePath)
            .put("installed", true)
        File(storageDir, "xray.json").writeText(manifest.toString())

        val reloaded = PluginManager(storageDir)
        assertTrue(reloaded.isInstalled("xray"))

        reloaded.uninstall("xray")
        assertFalse(reloaded.isInstalled("xray"))
        assertFalse(File(storageDir, "xray.json").exists())
    }
}
