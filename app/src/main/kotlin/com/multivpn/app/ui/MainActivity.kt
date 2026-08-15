package com.multivpn.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.multivpn.app.R
import com.multivpn.app.plugin.CoreState
import com.multivpn.app.plugin.CoreStatus
import com.multivpn.app.plugin.PluginCore
import com.multivpn.app.plugin.PluginManager
import com.multivpn.app.data.PreferenceHelper
import com.multivpn.app.plugin.PluginRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * Main activity for the MultiVPN application
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val pluginManager by lazy { PluginManager(filesDir) }
    private val pluginRepository by lazy { PluginRepository(pluginManager) }

    private lateinit var vpnToggleButton: Button
    private lateinit var settingsButton: Button
    private lateinit var connectionsButton: Button
    private lateinit var addonRecyclerView: RecyclerView
    private lateinit var connectionListView: ListView
    private lateinit var logTextView: TextView
    private lateinit var autoConnectCheckBox: CheckBox
    private lateinit var killSwitchCheckBox: CheckBox
    private lateinit var analyticsCheckBox: CheckBox

    private val pluginAdapter by lazy { PluginListAdapter(emptyList()) { core -> downloadCore(core) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = PreferenceHelper(this)
        if (!prefs.oobeCompleted) {
            startActivity(Intent(this, OobeActivity::class.java))
            finish()
            return
        }

        vpnToggleButton = findViewById(R.id.vpn_toggle_button)
        settingsButton = findViewById(R.id.settings_button)
        connectionsButton = findViewById(R.id.connections_button)
        addonRecyclerView = findViewById(R.id.addonListView)
        connectionListView = findViewById(R.id.connectionListView)
        logTextView = findViewById(R.id.logTextView)
        autoConnectCheckBox = findViewById(R.id.autoConnectCheckBox)
        killSwitchCheckBox = findViewById(R.id.killSwitchCheckBox)
        analyticsCheckBox = findViewById(R.id.analyticsCheckBox)

        setupUI()
        loadPluginCatalog()
    }

    private fun setupUI() {
        vpnToggleButton.setOnClickListener {
            toggleVpnState()
        }

        settingsButton.setOnClickListener {
            navigateToSettings()
        }

        connectionsButton.setOnClickListener {
            startActivity(Intent(this, ConnectionStatsActivity::class.java))
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

    private fun loadPluginCatalog() {
        addonRecyclerView.layoutManager = LinearLayoutManager(this)
        addonRecyclerView.adapter = pluginAdapter

        val manifestUrl =
            "https://raw.githubusercontent.com/gidtfrhipoppppp-sudo/MultiVPN/main/Plugins/manifest.json"
        appendLog("Loading core catalogue from repository...")
        pluginRepository.fetchCatalog(manifestUrl) { result ->
            result.onSuccess { cores ->
                appendLog("Loaded ${cores.size} core(s): ${cores.joinToString { it.displayName }}")
                runOnUiThread { pluginAdapter.update(pluginRepository.statuses()) }
            }.onFailure { error ->
                appendLog("Could not load catalogue: ${error.message}")
            }
        }
    }

    private fun downloadCore(core: PluginCore) {
        appendLog("Downloading ${core.displayName} ${core.version}...")
        pluginAdapter.setBusy(core.id, CoreState.DOWNLOADING)

        executor.execute {
            try {
                pluginManager.downloadAndInstall(
                    context = this,
                    core = core,
                    onProgress = { line -> runOnUiThread { appendLog(line) } },
                    onComplete = { result ->
                        runOnUiThread {
                            result.onSuccess { path ->
                                appendLog("${core.displayName} ready: $path")
                            }.onFailure { error ->
                                appendLog("Install failed for ${core.displayName}: ${error.message}")
                            }
                            pluginAdapter.update(pluginRepository.statuses())
                        }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Download failed for ${core.displayName}")
                runOnUiThread {
                    appendLog("Download failed for ${core.displayName}: ${e.message}")
                    pluginAdapter.update(pluginRepository.statuses())
                }
            }
        }
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

    private fun appendLog(message: String) {
        val current = logTextView.text.toString()
        logTextView.text = "$current\n$message"
    }

    override fun onDestroy() {
        super.onDestroy()
        pluginRepository.shutdown()
        executor.shutdownNow()
        Timber.d("MainActivity destroyed")
    }
}

/**
 * Adapter rendering the downloadable cores list with per-core state and a
 * download/install button.
 */
private class PluginListAdapter(
    private var items: List<CoreStatus>,
    private val onDownload: (PluginCore) -> Unit
) : RecyclerView.Adapter<PluginListAdapter.PluginHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PluginHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plugin, parent, false)
        return PluginHolder(view)
    }

    override fun onBindViewHolder(holder: PluginHolder, position: Int) {
        holder.bind(items[position], onDownload)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<CoreStatus>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setBusy(id: String, state: CoreState) {
        val index = items.indexOfFirst { it.core.id == id }
        if (index >= 0) {
            items = items.toMutableList().also {
                it[index] = it[index].copy(state = state)
            }
            notifyItemChanged(index)
        }
    }

    class PluginHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.coreNameText)
        private val description: TextView = view.findViewById(R.id.coreDescriptionText)
        private val stateText: TextView = view.findViewById(R.id.coreStateText)
        private val button: Button = view.findViewById(R.id.downloadButton)

        fun bind(status: CoreStatus, onDownload: (PluginCore) -> Unit) {
            val core = status.core
            name.text = "${core.displayName} ${core.version}".trim()
            description.text = core.description.ifBlank { core.architecture }
            stateText.text = describe(status.state)
            button.isEnabled = status.state != CoreState.DOWNLOADING && status.state != CoreState.INSTALLING
            button.text = when (status.state) {
                CoreState.DOWNLOADING -> "Downloading..."
                CoreState.INSTALLING -> "Installing..."
                CoreState.INSTALLED, CoreState.ENABLED -> "Reinstall"
                CoreState.NOT_INSTALLED, CoreState.ERROR -> "Download"
            }
            button.setOnClickListener { onDownload(core) }
        }

        private fun describe(state: CoreState): String = when (state) {
            CoreState.NOT_INSTALLED -> "Not installed"
            CoreState.DOWNLOADING -> "Downloading..."
            CoreState.INSTALLING -> "Installing..."
            CoreState.INSTALLED -> "Installed"
            CoreState.ENABLED -> "Enabled"
            CoreState.ERROR -> "Error"
        }
    }
}
