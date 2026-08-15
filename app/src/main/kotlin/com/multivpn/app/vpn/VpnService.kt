package com.multivpn.app.vpn

import android.content.Intent
import android.net.VpnService as AndroidVpnService
import android.os.Binder
import android.os.IBinder
import timber.log.Timber

/**
 * VPN Service for handling VPN connections
 */
class MultiVpnService : AndroidVpnService() {

    companion object {
        private const val TAG = "VpnService"
        const val ACTION_CONNECT = "com.multivpn.app.VPN_CONNECT"
        const val ACTION_DISCONNECT = "com.multivpn.app.VPN_DISCONNECT"
    }

    private val binder = VpnBinder()
    private var vpnThread: VpnThread? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("VPN Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                connect()
            }
            ACTION_DISCONNECT -> {
                disconnect()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Timber.d("VPN Service bound")
        return binder
    }

    private fun connect() {
        if (vpnThread != null && vpnThread!!.isAlive) {
            Timber.d("VPN already connected")
            return
        }

        try {
            Timber.d("Starting VPN connection...")
            vpnThread = VpnThread(this)
            vpnThread?.start()
        } catch (e: Exception) {
            Timber.e(e, "Error starting VPN connection")
        }
    }

    private fun disconnect() {
        try {
            Timber.d("Disconnecting VPN...")
            vpnThread?.interrupt()
            vpnThread = null
        } catch (e: Exception) {
            Timber.e(e, "Error disconnecting VPN")
        }
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
        Timber.d("VPN Service destroyed")
    }

    /**
     * Binder for local communication
     */
    inner class VpnBinder : Binder() {
        fun getService(): MultiVpnService = this@MultiVpnService
    }

    /**
     * Thread for handling VPN operations
     */
    private class VpnThread(val vpnService: MultiVpnService) : Thread() {
        override fun run() {
            try {
                Timber.d("VPN thread started")

                while (!isInterrupted) {
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                Timber.d("VPN thread interrupted")
            } catch (e: Exception) {
                Timber.e(e, "Error in VPN thread")
            }
        }
    }
}
