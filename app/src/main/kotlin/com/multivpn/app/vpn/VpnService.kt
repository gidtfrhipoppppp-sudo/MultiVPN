package com.multivpn.app.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService as AndroidVpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.multivpn.app.R
import com.multivpn.app.data.PreferenceHelper
import com.multivpn.app.network.ConnectionTrackerHolder
import com.multivpn.app.ui.MainActivity
import timber.log.Timber
import java.io.File

/**
 * VPN service that runs the selected core binary as a local proxy process and
 * routes system traffic through it via a TUN interface.
 */
class MultiVpnService : AndroidVpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val coreRunner = CoreProcessRunner()

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification("Starting VPN..."))
        Timber.d("VPN Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> connect(intent)
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }

    private fun connect(intent: Intent?) {
        val binaryPath = intent?.getStringExtra(EXTRA_BINARY_PATH)
        if (binaryPath.isNullOrEmpty()) {
            Timber.e("No core binary path provided")
            updateNotification("No core selected")
            stopSelf()
            return
        }

        if (coreRunner.isRunning()) {
            Timber.d("Core already running")
            return
        }

        ConnectionTrackerHolder.tracker.start()

        val prefs = PreferenceHelper(this)
        val configContent = CoreConfig.build(prefs.customConfig)
        val configPath = File(filesDir, "core-config.json")
        configPath.writeText(configContent)

        try {
            coreRunner.start(binaryPath, configPath.absolutePath) { line ->
                Timber.d("core: $line")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to start core")
            updateNotification("Core failed to start: ${e.message}")
            disconnect()
            return
        }

        establishTunnel(prefs)
        updateNotification("VPN active — traffic routed via ${CoreConfig.LOCAL_HOST}:${CoreConfig.LOCAL_PORT}")
    }

    private fun establishTunnel(prefs: PreferenceHelper) {
        if (vpnInterface != null) return

        val builder = Builder()
            .setSession(getString(R.string.app_name))
            .setMtu(prefs.mtu.coerceIn(68, 1500))
            .addAddress(VPN_ADDRESS, 32)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)
            .addDnsServer(prefs.dns.ifBlank { "8.8.8.8" })
            .addSearchDomain(DEFAULT_DOMAIN)

        if (prefs.bypassLan) {
            // Allow LAN traffic to bypass the tunnel.
            builder.addRoute("192.168.0.0", 16)
            builder.addRoute("10.0.0.0", 8)
            builder.addRoute("172.16.0.0", 12)
            builder.allowFamily(android.system.OsConstants.AF_INET)
        }

        vpnInterface = builder.establish()
        if (vpnInterface == null) {
            Timber.e("Failed to establish VPN interface")
            updateNotification("Failed to establish tunnel")
        }
    }

    private fun disconnect() {
        Timber.d("Disconnecting VPN...")
        coreRunner.stop()
        vpnInterface?.close()
        vpnInterface = null
        ConnectionTrackerHolder.tracker.stop()
        updateNotification("VPN disconnected")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
        Timber.d("VPN Service destroyed")
    }

    override fun onRevoke() {
        disconnect()
    }

    private fun buildNotification(text: String): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "VPN service", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    companion object {
        const val ACTION_CONNECT = "com.multivpn.app.VPN_CONNECT"
        const val ACTION_DISCONNECT = "com.multivpn.app.VPN_DISCONNECT"
        const val EXTRA_BINARY_PATH = "extra_binary_path"
        private const val CHANNEL_ID = "vpn_service"
        private const val NOTIFICATION_ID = 1
        private const val VPN_ADDRESS = "10.8.0.2"
        private const val DEFAULT_DOMAIN = "."
    }
}
