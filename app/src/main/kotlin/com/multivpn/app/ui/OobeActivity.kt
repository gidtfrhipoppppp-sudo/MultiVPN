package com.multivpn.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.multivpn.app.R
import com.multivpn.app.data.ExperienceLevel
import com.multivpn.app.data.PreferenceHelper
import com.multivpn.app.databinding.ActivityOobeBinding
import com.multivpn.app.plugin.PluginCore

/**
 * First-run out-of-box experience: the user picks an experience level, after
 * which the plugin manager screen is shown describing each downloadable core.
 */
class OobeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOobeBinding
    private val prefs by lazy { PreferenceHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOobeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.levelBasic.setOnClickListener { chooseLevel(ExperienceLevel.BASIC) }
        binding.levelIntermediate.setOnClickListener { chooseLevel(ExperienceLevel.INTERMEDIATE) }
        binding.levelProfessional.setOnClickListener { chooseLevel(ExperienceLevel.PROFESSIONAL) }
    }

    private fun chooseLevel(level: ExperienceLevel) {
        prefs.experienceLevel = level
        prefs.oobeCompleted = true

        startActivity(Intent(this, PluginInfoActivity::class.java))
        finish()
    }

    companion object {
        /** The catalogue shown during the OOBE plugin step. */
        val OOBE_CORES: List<PluginCore> = listOf(
            PluginCore(
                id = "sing-box",
                displayName = "Sing-box core",
                description = "Universal proxy platform by SagerNet. Supports VMess, VLESS, Trojan, Shadowsocks, Hysteria and more. Recommended for most setups.",
                downloadUrl = "",
                fileName = "sing-box.tar.gz",
                archiveFormat = com.multivpn.app.plugin.ArchiveFormat.TAR_GZ,
                binaryName = "sing-box",
                version = "1.13.18",
                architecture = "android-arm64"
            ),
            PluginCore(
                id = "xray",
                displayName = "Xray core",
                description = "XTLS/Xray-core. Best-in-class for VLESS/XTLS and Reality. Use when your server uses XTLS-Reality or VLESS.",
                downloadUrl = "",
                fileName = "xray.zip",
                archiveFormat = com.multivpn.app.plugin.ArchiveFormat.ZIP,
                binaryName = "xray",
                version = "26.3.27",
                architecture = "android-arm64-v8a"
            ),
            PluginCore(
                id = "clash",
                displayName = "Clash (mihomo) core",
                description = "MetaCubeX/mihomo (Clash Meta). Rich rule-based routing and subscription support. Best for multi-server configs.",
                downloadUrl = "",
                fileName = "mihomo.gz",
                archiveFormat = com.multivpn.app.plugin.ArchiveFormat.GZ,
                binaryName = "mihomo",
                version = "1.19.29",
                architecture = "android-arm64-v"
            )
        )
    }
}
