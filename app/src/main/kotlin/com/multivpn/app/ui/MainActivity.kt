package com.multivpn.app.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.multivpn.app.R
import com.multivpn.app.plugin.CoreState
import com.multivpn.app.plugin.CoreStatus
import com.multivpn.app.plugin.PluginCore
import com.multivpn.app.plugin.ServerBinaryException
import com.multivpn.app.vpn.MultiVpnService
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
    private lateinit var addCustomPluginButton: Button
    private lateinit var addonRecyclerView: RecyclerView
    private lateinit var connectionListView: ListView
    private lateinit var logTextView: TextView
    private lateinit var autoConnectCheckBox: CheckBox
    private lateinit var killSwitchCheckBox: CheckBox
    private lateinit var analyticsCheckBox: CheckBox

    private val pluginAdapter by lazy { PluginListAdapter(emptyList()) { core -> downloadCore(core) } }

    private var vpnActive = false
    private var selectedCoreId: String? = null
    private var vpnBinaryPath: String? = null
    private var vpnPrepareIntent: Intent? = null

    private val pickBinaryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) installCustomBinary(uri)
        }


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
        addCustomPluginButton = findViewById(R.id.addCustomPluginButton)
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

        addCustomPluginButton.setOnClickListener {
            pickBinaryLauncher.launch(arrayOf("*/*"))
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

    private fun installCustomBinary(uri: Uri) {
        val sourceName = uri.lastPathSegment ?: uri.toString()
        val displayName = sourceName.substringAfterLast('/').ifBlank { "Custom core" }
        val binaryName = displayName.substringAfterLast('.').let {
            if (it.isBlank() || it == displayName) displayName else displayName
        }
        val id = "custom-" + System.currentTimeMillis()

        appendLog("Installing custom plugin: $displayName")
        executor.execute {
            val result = try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    pluginManager.installCustomBinary(
                        id = id,
                        displayName = displayName,
                        binaryName = binaryName,
                        sourceName = sourceName,
                        input = stream,
                        onProgress = { line -> runOnUiThread { appendLog(line) } }
                    )
                } ?: Result.failure(java.io.IOException("Could not open selected file"))
            } catch (e: Exception) {
                Result.failure<String>(e)
            }

            runOnUiThread {
                result.onSuccess { path ->
                    appendLog("$displayName installed: $path")
                    pluginAdapter.update(pluginRepository.statuses())
                }.onFailure { error ->
                    if (error is ServerBinaryException) {
                        AlertDialog.Builder(this)
                            .setTitle(R.string.server_binary_title)
                            .setMessage(R.string.server_binary_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                        appendLog("Rejected: ${error.message}")
                    } else {
                        appendLog("Install failed: ${error.message}")
                    }
                    pluginAdapter.update(pluginRepository.statuses())
                }
            }
        }
    }

    private fun toggleVpnState() {
        if (vpnActive) {
            stopVpn()
            return
        }

        // Find the first installed core to use as the proxy engine.
        val statuses = pluginRepository.statuses()
        val installed = statuses.firstOrNull {
            it.state == com.multivpn.app.plugin.CoreState.INSTALLED && !it.binaryPath.isNullOrEmpty()
        }
        if (installed == null) {
            appendLog("No core installed. Download a core (sing-box/Xray/Clash) first.")
            AlertDialog.Builder(this)
                .setTitle("No core installed")
                .setMessage("Download and install a VPN core (sing-box, Xray, or Clash) before connecting.")
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        selectedCoreId = installed.core.id
        val binaryPath = installed.binaryPath!!
        appendLog("Starting ${installed.core.displayName}...")

        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPrepareIntent = prepareIntent
            vpnBinaryPath = binaryPath
            startActivityForResult(prepareIntent, REQ_VPN_PERMISSION)
        } else {
            // Already authorised — start the service directly.
            startVpnService(binaryPath)
        }
    }

    private fun startVpnService(binaryPath: String) {
        val serviceIntent = Intent(this, MultiVpnService::class.java).apply {
            action = MultiVpnService.ACTION_CONNECT
            putExtra(MultiVpnService.EXTRA_BINARY_PATH, binaryPath)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        vpnActive = true
        vpnToggleButton.text = getString(R.string.disconnect)
        appendLog("VPN service started")
    }

    private fun stopVpn() {
        val serviceIntent = Intent(this, MultiVpnService::class.java).apply {
            action = MultiVpnService.ACTION_DISCONNECT
        }
        startService(serviceIntent)
        vpnActive = false
        vpnToggleButton.text = getString(R.string.connect)
        appendLog("VPN service stopped")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_VPN_PERMISSION) {
            if (resultCode == RESULT_OK && vpnBinaryPath != null) {
                startVpnService(vpnBinaryPath!!)
            } else {
                appendLog("VPN permission denied")
            }
            vpnBinaryPath = null
            vpnPrepareIntent = null
        }
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
        if (vpnActive) stopVpn()
        pluginRepository.shutdown()
        executor.shutdownNow()
        Timber.d("MainActivity destroyed")
    }

    companion object {
        private const val REQ_VPN_PERMISSION = 1001
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
