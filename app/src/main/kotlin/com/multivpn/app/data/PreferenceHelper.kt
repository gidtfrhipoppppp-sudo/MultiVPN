package com.multivpn.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Experience level chosen by the user during the OOBE flow.
 *
 * The level controls how many settings are exposed in the Settings screen:
 * - BASIC: minimal set (connect/disconnect, kill switch).
 * - INTERMEDIATE: standard VPN client settings (DNS, MTU, protocol, auto-start).
 * - PROFESSIONAL: everything possible on a non-root device (debug, routing,
 *   per-app split tunnel, custom config).
 */
enum class ExperienceLevel(val id: String, val order: Int) {
    BASIC("basic", 0),
    INTERMEDIATE("intermediate", 1),
    PROFESSIONAL("professional", 2);

    companion object {
        fun fromId(id: String?): ExperienceLevel =
            values().firstOrNull { it.id == id } ?: BASIC

        fun fromOrder(order: Int): ExperienceLevel =
            values().firstOrNull { it.order == order } ?: BASIC
    }
}

/**
 * Centralised access to the app's SharedPreferences so every screen reads
 * and writes the same keys.
 */
class PreferenceHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var experienceLevel: ExperienceLevel
        get() = ExperienceLevel.fromId(prefs.getString(KEY_LEVEL, null))
        set(value) = prefs.edit { putString(KEY_LEVEL, value.id) }

    var oobeCompleted: Boolean
        get() = prefs.getBoolean(KEY_OOBE_DONE, false)
        set(value) = prefs.edit { putBoolean(KEY_OOBE_DONE, value) }

    var darkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, false)
        set(value) = prefs.edit { putBoolean(KEY_DARK_THEME, value) }

    var autoStart: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START, true)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_START, value) }

    var notifications: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit { putBoolean(KEY_NOTIFICATIONS, value) }

    var debugMode: Boolean
        get() = prefs.getBoolean(KEY_DEBUG_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_DEBUG_MODE, value) }

    var killSwitch: Boolean
        get() = prefs.getBoolean(KEY_KILL_SWITCH, false)
        set(value) = prefs.edit { putBoolean(KEY_KILL_SWITCH, value) }

    var autoConnect: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CONNECT, false)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_CONNECT, value) }

    var dns: String
        get() = prefs.getString(KEY_DNS, "8.8.8.8") ?: "8.8.8.8"
        set(value) = prefs.edit { putString(KEY_DNS, value) }

    var mtu: Int
        get() = prefs.getInt(KEY_MTU, 1400)
        set(value) = prefs.edit { putInt(KEY_MTU, value) }

    var dataLimitMb: Int
        get() = prefs.getInt(KEY_DATA_LIMIT, 0)
        set(value) = prefs.edit { putInt(KEY_DATA_LIMIT, value) }

    var protocol: String
        get() = prefs.getString(KEY_PROTOCOL, "auto") ?: "auto"
        set(value) = prefs.edit { putString(KEY_PROTOCOL, value) }

    /** Pro-only: raw config passed to the core binary. */
    var customConfig: String
        get() = prefs.getString(KEY_CUSTOM_CONFIG, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CUSTOM_CONFIG, value) }

    /** Pro-only: comma-separated package names to route through the tunnel. */
    var perAppPackages: String
        get() = prefs.getString(KEY_PER_APP, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PER_APP, value) }

    /** Pro-only: allow LAN bypass while the tunnel is up. */
    var bypassLan: Boolean
        get() = prefs.getBoolean(KEY_BYPASS_LAN, true)
        set(value) = prefs.edit { putBoolean(KEY_BYPASS_LAN, value) }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_LEVEL = "experience_level"
        private const val KEY_OOBE_DONE = "oobe_completed"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_AUTO_START = "auto_start"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_DEBUG_MODE = "debug_mode"
        private const val KEY_KILL_SWITCH = "kill_switch"
        private const val KEY_AUTO_CONNECT = "auto_connect"
        private const val KEY_DNS = "dns"
        private const val KEY_MTU = "mtu"
        private const val KEY_DATA_LIMIT = "data_limit_mb"
        private const val KEY_PROTOCOL = "protocol"
        private const val KEY_CUSTOM_CONFIG = "custom_config"
        private const val KEY_PER_APP = "per_app_packages"
        private const val KEY_BYPASS_LAN = "bypass_lan"
    }
}
