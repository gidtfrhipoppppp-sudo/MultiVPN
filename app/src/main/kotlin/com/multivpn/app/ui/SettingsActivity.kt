package com.multivpn.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.multivpn.app.R
import com.multivpn.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val prefs by lazy { getSharedPreferences("app_settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadPreferences()
        bindListeners()
    }

    private fun loadPreferences() {
        binding.themeSwitch.isChecked = prefs.getBoolean("dark_theme", false)
        binding.autoStartSwitch.isChecked = prefs.getBoolean("auto_start", true)
        binding.notificationsSwitch.isChecked = prefs.getBoolean("notifications", true)
        binding.debugModeSwitch.isChecked = prefs.getBoolean("debug_mode", false)
        binding.dnsEdit.setText(prefs.getString("dns", "8.8.8.8") ?: "8.8.8.8")
        binding.mtuEdit.setText(prefs.getInt("mtu", 1400).toString())
        binding.limitEdit.setText(prefs.getInt("data_limit_mb", 0).toString())
        binding.protocolSpinner.setSelection(getProtocolIndex(prefs.getString("protocol", "auto") ?: "auto"))
    }

    private fun bindListeners() {
        binding.themeSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit { putBoolean("dark_theme", checked) }
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.autoStartSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit { putBoolean("auto_start", checked) }
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit { putBoolean("notifications", checked) }
        }

        binding.debugModeSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit { putBoolean("debug_mode", checked) }
        }

        binding.saveButton.setOnClickListener {
            prefs.edit {
                putString("dns", binding.dnsEdit.text.toString())
                putInt("mtu", binding.mtuEdit.text.toString().toIntOrNull() ?: 1400)
                putInt("data_limit_mb", binding.limitEdit.text.toString().toIntOrNull() ?: 0)
                putString("protocol", binding.protocolSpinner.selectedItem?.toString()?.lowercase() ?: "auto")
            }
            finish()
        }
    }

    private fun getProtocolIndex(value: String): Int = when (value.lowercase()) {
        "wireguard" -> 1
        "openvpn" -> 2
        else -> 0
    }
}
