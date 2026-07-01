package com.multivpn.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * Broadcast receiver for system events (e.g., device boot)
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Timber.d("Device boot detected")
                onDeviceBootCompleted(context)
            }
        }
    }

    private fun onDeviceBootCompleted(context: Context?) {
        context?.let {
            Timber.d("Starting VPN service after device boot")
            // TODO: Implement auto-start VPN logic if enabled in preferences
            // Example:
            // if (PreferencesManager.isAutoStartVpnEnabled()) {
            //     startVpn(context)
            // }
        }
    }
}
