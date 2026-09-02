package com.multivpn.app.network

import android.content.Context

/**
 * Application-wide access to the {@link ConnectionTracker} so that the VPN
 * service, log parser and UI all share the same instance.
 */
object ConnectionTrackerHolder {
    @Volatile
    private var instance: ConnectionTracker? = null

    val tracker: ConnectionTracker
        get() = instance ?: error("ConnectionTracker not initialised. Call init(context) first.")

    fun init(context: Context) {
        if (instance == null) {
            synchronized(this) {
                if (instance == null) {
                    instance = ConnectionTracker(context)
                }
            }
        }
    }
}
