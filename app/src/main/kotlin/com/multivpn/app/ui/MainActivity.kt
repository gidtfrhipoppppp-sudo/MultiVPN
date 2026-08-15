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
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Main activity for the MultiVPN application
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val pluginManager by lazy { PluginManager(cacheDir) }
    private val pluginRepository by lazy { PluginRepository(pluginManager) }

    private lateinit var vpnToggleButton: Button
    private lateinit var settingsButton: Button
    private lateinit var addonListView: ListView
    private lateinit var connectionListView: ListView
    private lateinit var logTextView: TextView
    private lateinit var autoConnectCheckBox: CheckBox
    private lateinit var killSwitchCheckBox: CheckBox
    private lateinit var analyticsCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vpnToggleButton = findViewById(R.id.vpn_toggle_button)
        settingsButton = findViewById(R.id.settings_button)
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
        vpnToggleButton.setOnClickListener {
            toggleVpnState()
        }

        settingsButton.setOnClickListener {
            navigateToSettings()
        }

        autoConnectCheckBox.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("Auto-connect set to $isChecked")
        }
        killSwitchCheckBox.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("Kill switch set to $isChecked")
        }
        analyticsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("Telemetry set to $isChecked")
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
        val currentText = vpnToggleButton.text.toString()
        val nextText = if (currentText.equals(getString(R.string.connect), ignoreCase = true)) {
            getString(R.string.disconnect)
        } else {
            getString(R.string.connect)
        }
        vpnToggleButton.text = nextText
        logTextView.text = "VPN state changed\n$nextText"
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
