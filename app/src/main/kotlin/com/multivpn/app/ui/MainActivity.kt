package com.multivpn.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.multivpn.app.R
import com.multivpn.app.plugin.PluginManager
import com.multivpn.app.plugin.PluginRepository
import timber.log.Timber

/**
 * Main activity for the MultiVPN application
 */
class MainActivity : AppCompatActivity() {

    private val pluginManager by lazy { PluginManager(cacheDir) }
    private val pluginRepository by lazy { PluginRepository(pluginManager) }

    private lateinit var startStopButton: Button
    private lateinit var oobeButton: Button
    private lateinit var addonListView: ListView
    private lateinit var connectionListView: ListView
    private lateinit var logTextView: TextView
    private lateinit var autoConnectCheckBox: CheckBox
    private lateinit var killSwitchCheckBox: CheckBox
    private lateinit var analyticsCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startStopButton = findViewById(R.id.startStopButton)
        oobeButton = findViewById(R.id.oobeButton)
        addonListView = findViewById(R.id.addonListView)
        connectionListView = findViewById(R.id.connectionListView)
        logTextView = findViewById(R.id.logTextView)
        autoConnectCheckBox = findViewById(R.id.autoConnectCheckBox)
        killSwitchCheckBox = findViewById(R.id.killSwitchCheckBox)
        analyticsCheckBox = findViewById(R.id.analyticsCheckBox)

        setupUI()
        bindPluginInfo()

        Timber.d("MainActivity created")
    }

    private fun setupUI() {
        startStopButton.setOnClickListener {
            toggleVpnState()
        }

        oobeButton.setOnClickListener {
            showSetupGuide()
        }
    }

    private fun bindPluginInfo() {
        val pluginNames = pluginRepository.availableCores().map { core ->
            val state = when {
                pluginManager.isEnabled(core.id) -> "enabled"
                pluginManager.isInstalled(core.id) -> "installed"
                else -> "downloadable"
            }
            "${core.displayName}: $state"
        }
        addonListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pluginNames)

        val connectionNames = listOf("Tunnel: default", "Last seen: just now")
        connectionListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, connectionNames)
        logTextView.text = "MultiVPN ready\nWaiting for connection"
    }

    private fun toggleVpnState() {
        val currentText = startStopButton.text.toString()
        val nextText = if (currentText.equals(getString(R.string.connect), ignoreCase = true)) {
            getString(R.string.disconnect)
        } else {
            getString(R.string.connect)
        }
        startStopButton.text = nextText
        logTextView.text = "VPN state changed\n$nextText"
    }

    private fun showSetupGuide() {
        logTextView.text = "Setup guide\nTap Start VPN to begin"
    }

    private fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
        Timber.d("Navigate to settings")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MainActivity destroyed")
    }
}
