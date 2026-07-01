package com.multivpn.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.multivpn.app.R
import com.multivpn.app.databinding.ActivityMainBinding
import com.multivpn.app.plugin.PluginManager
import com.multivpn.app.plugin.PluginRepository
import com.multivpn.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Main activity for the MultiVPN application
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val pluginManager by lazy { PluginManager(cacheDir) }
    private val pluginRepository by lazy { PluginRepository(pluginManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Set up UI
        setupUI()
        
        // Observe VPN state changes
        observeVpnState()
        bindPluginInfo()

        Timber.d("MainActivity created")
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        
        // Set up VPN toggle button
        binding.vpnToggleButton.setOnClickListener {
            viewModel.toggleVpn()
        }

        binding.statsButton.setOnClickListener {
            startActivity(Intent(this, ConnectionStatsActivity::class.java))
        }

        // Set up settings button
        binding.settingsButton.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun bindPluginInfo() {
        val pluginLines = pluginRepository.availableCores().joinToString(separator = "\n") { core ->
            val state = if (pluginManager.isEnabled(core.id)) "enabled" else if (pluginManager.isInstalled(core.id)) "installed" else "downloadable"
            "• ${core.displayName}: $state"
        }
        binding.pluginStatusText.text = pluginLines
        binding.vpnStatusText.text = getString(R.string.vpn_status) + "\nPlugins: ${pluginRepository.availableCores().joinToString { it.displayName }}"
    }

    private fun observeVpnState() {
        viewModel.vpnState.observe(this) { state ->
            when (state) {
                true -> {
                    binding.vpnStatusText.text = getString(R.string.vpn_connected)
                    binding.vpnToggleButton.text = getString(R.string.disconnect)
                }
                false -> {
                    binding.vpnStatusText.text = getString(R.string.vpn_disconnected)
                    binding.vpnToggleButton.text = getString(R.string.connect)
                }
            }
        }

        viewModel.vpnError.observe(this) { error ->
            error?.let {
                showErrorMessage(it)
                Timber.e("VPN Error: $it")
            }
        }
    }

    private fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
        Timber.d("Navigate to settings")
    }

    private fun showErrorMessage(message: String) {
        // TODO: Show error message using snackbar or dialog
        Timber.e("Error: $message")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MainActivity destroyed")
    }
}
