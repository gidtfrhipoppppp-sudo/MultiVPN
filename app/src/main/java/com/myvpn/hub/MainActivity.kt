package com.multivpn.app

import android.app.AlertDialog
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayList
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var startStopButton: Button
    private lateinit var oobeButton: Button
    private lateinit var logTextView: TextView
    private lateinit var addonListView: ListView
    private lateinit var connectionListView: ListView
    private lateinit var proficiencyText: TextView
    private lateinit var autoConnectCheckBox: CheckBox
    private lateinit var killSwitchCheckBox: CheckBox
    private lateinit var analyticsCheckBox: CheckBox
    private lateinit var oscilloscopeView: OscilloscopeView
    private lateinit var addonManager: AddonManager
    private lateinit var processRunner: CoreProcessRunner
    private var isVpnActive = false
    private var proficiencyLevel = "Beginner"
    private val addons = ArrayList<AddonManager.AddonSpec>()
    private val connectionItems = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startStopButton = findViewById(R.id.startStopButton)
        oobeButton = findViewById(R.id.oobeButton)
        logTextView = findViewById(R.id.logTextView)
        addonListView = findViewById(R.id.addonListView)
        connectionListView = findViewById(R.id.connectionListView)
        proficiencyText = findViewById(R.id.proficiencyText)
        autoConnectCheckBox = findViewById(R.id.autoConnectCheckBox)
        killSwitchCheckBox = findViewById(R.id.killSwitchCheckBox)
        analyticsCheckBox = findViewById(R.id.analyticsCheckBox)
        oscilloscopeView = findViewById(R.id.oscilloscopeView)
        logTextView.movementMethod = ScrollingMovementMethod()

        addonManager = AddonManager()
        processRunner = CoreProcessRunner()

        val emptyPluginAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, ArrayList<String>())
        addonListView.adapter = emptyPluginAdapter
        addonListView.choiceMode = ListView.CHOICE_MODE_SINGLE

        val connectionAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, connectionItems)
        connectionListView.adapter = connectionAdapter

        loadPluginsFromRepository()

        startStopButton.setOnClickListener {
            if (isVpnActive) {
                stopVpn()
            } else {
                startVpn()
            }
        }

        oobeButton.setOnClickListener { showOobeDialog() }
        autoConnectCheckBox.setOnCheckedChangeListener { _, isChecked -> appendLog("Auto-connect set to $isChecked") }
        killSwitchCheckBox.setOnCheckedChangeListener { _, isChecked -> appendLog("Kill switch set to $isChecked") }
        analyticsCheckBox.setOnCheckedChangeListener { _, isChecked -> appendLog("Telemetry set to $isChecked") }

        val pluginRoot = addonManager.pluginRootDirectory(this)
        appendLog("Plugin folder: ${pluginRoot.absolutePath}")
        appendLog("App started. Loading plugins from repository...")
        showOobeDialog()
    }

    private fun loadPluginsFromRepository() {
        val repositoryUrl = "https://raw.githubusercontent.com/gidtfrhipoppppp-sudo/MultiVPN/main"
        addonManager.fetchAvailablePlugins(repositoryUrl) { result ->
            result.onSuccess { plugins ->
                addons.clear()
                addons.addAll(plugins)
                val names = plugins.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, names)
                addonListView.adapter = adapter
                if (names.isNotEmpty()) {
                    addonListView.setItemChecked(0, true)
                    appendLog("Loaded ${plugins.size} plugin(s) from repository")
                } else {
                    appendLog("No plugins found in repository manifest")
                }
            }.onFailure { error ->
                appendLog("Could not load plugins: ${error.message}")
            }
        }
    }

    private fun startVpn() {
        val selectedPosition = addonListView.checkedItemPosition
        val selectedAddon = addons.getOrNull(selectedPosition)
        if (selectedAddon == null) {
            appendLog("Please select an addon first.")
            return
        }

        appendLog("Preparing ${selectedAddon.name}...")
        CoroutineScope(Dispatchers.IO).launch {
            addonManager.downloadAndInstall(this@MainActivity, selectedAddon) { result ->
                result.onSuccess { installedAddon ->
                    appendLog("Installed: ${installedAddon.path}")
                    val intent = VpnService.prepare(this@MainActivity)
                    if (intent != null) {
                        startActivityForResult(intent, REQ_VPN)
                        return@onSuccess
                    }

                    launchCoreAndStartService(installedAddon.path)
                }.onFailure { error ->
                    appendLog("Install failed: ${error.message}")
                }
            }
        }
    }

    private fun launchCoreAndStartService(binaryPath: String) {
        val binaryFile = File(binaryPath)
        if (!binaryFile.exists()) {
            appendLog("Binary not found: $binaryPath")
            return
        }

        val configPath = File(filesDir, "config.json")
        configPath.writeText("{\"inbounds\":[{\"type\":\"socks\",\"listen\":\"127.0.0.1\",\"port\":10808}]}")

        processRunner.start(
            binaryPath = binaryPath,
            args = listOf("run", "-c", configPath.absolutePath),
            logger = { line ->
                appendLog(line)
                oscilloscopeView.pushSample(line.hashCode().toFloat() % 2f)
            }
        )

        val serviceIntent = Intent(this, VpnTunnelService::class.java)
        serviceIntent.putExtra(VpnTunnelService.EXTRA_PROXY_HOST, "127.0.0.1")
        serviceIntent.putExtra(VpnTunnelService.EXTRA_PROXY_PORT, 10808)
        startService(serviceIntent)
        isVpnActive = true
        startStopButton.text = "STOP VPN"
        connectionItems.clear()
        connectionItems.add("Connected • 127.0.0.1:10808")
        connectionItems.add("Protocol • SOCKS5")
        (connectionListView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        appendLog("VPN started")
    }

    private fun stopVpn() {
        processRunner.stop()
        stopService(Intent(this, VpnTunnelService::class.java))
        isVpnActive = false
        startStopButton.text = "START VPN"
        connectionItems.clear()
        connectionItems.add("Disconnected")
        (connectionListView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        appendLog("VPN stopped")
    }

    private fun appendLog(message: String) {
        runOnUiThread {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.US).format(java.util.Date())
            logTextView.append("[$timestamp] $message\n")
            val scrollAmount = logTextView.layout?.getLineTop(logTextView.lineCount) ?: 0
            logTextView.scrollTo(0, scrollAmount)
        }
    }

    private fun showOobeDialog() {
        val levels = arrayOf("Beginner", "Intermediate", "Pro")
        AlertDialog.Builder(this)
            .setTitle("Choose your experience level")
            .setItems(levels) { _, which ->
                proficiencyLevel = levels[which]
                proficiencyText.text = "$proficiencyLevel • Personalized experience"
                appendLog("Proficiency set to $proficiencyLevel")
            }
            .setPositiveButton("Continue") { _, _ ->
                appendLog("Onboarding complete")
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_VPN) {
            if (resultCode == RESULT_OK) {
                val selectedPosition = addonListView.checkedItemPosition
                val selectedAddon = addons.getOrNull(selectedPosition)
                if (selectedAddon != null) {
                    val installedPath = File(addonManager.pluginRootDirectory(this), "${selectedAddon.name.lowercase().replace(Regex("[^a-z0-9.-]+"), "-")}/${selectedAddon.fileName}")
                    launchCoreAndStartService(installedPath.absolutePath)
                }
            } else {
                appendLog("VPN permission denied")
            }
        }
    }

    companion object {
        private const val REQ_VPN = 1001
    }
}
