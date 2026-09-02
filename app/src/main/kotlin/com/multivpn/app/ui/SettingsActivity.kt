package com.multivpn.app.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.multivpn.app.R
import com.multivpn.app.data.ExperienceLevel
import com.multivpn.app.data.PreferenceHelper
import com.multivpn.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val prefs by lazy { PreferenceHelper(this) }
    private val rawPrefs by lazy { getSharedPreferences("app_settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        binding.toolbar.setNavigationOnClickListener { finish() }

        applyLevelVisibility()
        loadPreferences()
        bindListeners()
    }

    /**
     * Show settings sections based on the chosen experience level.
     * BASIC -> only the basic card.
     * INTERMEDIATE -> basic + intermediate cards.
     * PROFESSIONAL -> all cards.
     */
    private fun applyLevelVisibility() {
        val level = prefs.experienceLevel
        binding.levelBadgeText.text = "${getString(R.string.level_label)}: ${levelLabel(level)}"

        binding.basicSection.visibility = View.VISIBLE
        binding.intermediateSection.visibility =
            if (level.order >= ExperienceLevel.INTERMEDIATE.order) View.VISIBLE else View.GONE
        binding.professionalSection.visibility =
            if (level.order >= ExperienceLevel.PROFESSIONAL.order) View.VISIBLE else View.GONE
    }

    private fun levelLabel(level: ExperienceLevel): String = when (level) {
        ExperienceLevel.BASIC -> getString(R.string.level_basic_title)
        ExperienceLevel.INTERMEDIATE -> getString(R.string.level_intermediate_title)
        ExperienceLevel.PROFESSIONAL -> getString(R.string.level_professional_title)
    }

    private fun loadPreferences() {
        binding.themeSwitch.isChecked = prefs.darkTheme
        binding.killSwitchSwitch.isChecked = prefs.killSwitch
        binding.autoStartSwitch.isChecked = prefs.autoStart
        binding.notificationsSwitch.isChecked = prefs.notifications
        binding.debugModeSwitch.isChecked = prefs.debugMode
        binding.bypassLanSwitch.isChecked = prefs.bypassLan
        binding.dnsEdit.setText(prefs.dns)
        binding.mtuEdit.setText(prefs.mtu.toString())
        binding.limitEdit.setText(prefs.dataLimitMb.toString())
        binding.perAppEdit.setText(prefs.perAppPackages)
        binding.customConfigEdit.setText(prefs.customConfig)
        binding.protocolSpinner.setSelection(getProtocolIndex(prefs.protocol))
    }

    private fun bindListeners() {
        binding.themeSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.darkTheme = checked
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        binding.killSwitchSwitch.setOnCheckedChangeListener { _, checked -> prefs.killSwitch = checked }
        binding.autoStartSwitch.setOnCheckedChangeListener { _, checked -> prefs.autoStart = checked }
        binding.notificationsSwitch.setOnCheckedChangeListener { _, checked -> prefs.notifications = checked }
        binding.debugModeSwitch.setOnCheckedChangeListener { _, checked -> prefs.debugMode = checked }
        binding.bypassLanSwitch.setOnCheckedChangeListener { _, checked -> prefs.bypassLan = checked }

        binding.saveButton.setOnClickListener {
            prefs.dns = binding.dnsEdit.text.toString()
            binding.mtuEdit.text.toString().toIntOrNull()?.let { prefs.mtu = it }
            binding.limitEdit.text.toString().toIntOrNull()?.let { prefs.dataLimitMb = it }
            prefs.protocol = binding.protocolSpinner.selectedItem?.toString()?.lowercase() ?: "auto"
            prefs.perAppPackages = binding.perAppEdit.text.toString()
            prefs.customConfig = binding.customConfigEdit.text.toString()
            finish()
        }
    }

    private fun getProtocolIndex(value: String): Int = when (value.lowercase()) {
        "wireguard" -> 1
        "openvpn" -> 2
        else -> 0
    }
}
