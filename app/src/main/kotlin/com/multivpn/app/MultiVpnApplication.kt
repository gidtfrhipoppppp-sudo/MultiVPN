package com.multivpn.app

import android.app.Application
import com.multivpn.app.network.ConnectionTrackerHolder
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for MultiVPN
 */
@HiltAndroidApp
class MultiVpnApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
        Timber.d("MultiVPN Application created")

        ConnectionTrackerHolder.init(this)
    }
}
