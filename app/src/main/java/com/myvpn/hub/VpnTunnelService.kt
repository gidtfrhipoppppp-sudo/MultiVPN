package com.multivpn.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

class VpnTunnelService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val host = intent?.getStringExtra(EXTRA_PROXY_HOST) ?: "127.0.0.1"
        val port = intent?.getIntExtra(EXTRA_PROXY_PORT, 10808) ?: 10808
        establishTunnel(host, port)
        return START_STICKY
    }

    private fun establishTunnel(host: String, port: Int) {
        if (vpnInterface != null) {
            return
        }

        val builder = Builder()
            .setSession("MyVPN")
            .setMtu(1500)
            .addAddress("10.8.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)
            .addDnsServer("8.8.8.8")
            .addDnsServer("1.1.1.1")

        vpnInterface = builder.establish()
        val message = "VPN tunnel active. Routing traffic to $host:$port"
        updateNotification(message)
    }

    override fun onDestroy() {
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun buildForegroundNotification(): Notification {
        val channelId = "vpn_service"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "VPN service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("MyVPN")
            .setContentText("VPN tunnel is running")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "vpn_service")
            .setContentTitle("MyVPN")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
        notificationManager.notify(1, notification)
    }

    companion object {
        const val EXTRA_PROXY_HOST = "extra_proxy_host"
        const val EXTRA_PROXY_PORT = "extra_proxy_port"
    }
}
