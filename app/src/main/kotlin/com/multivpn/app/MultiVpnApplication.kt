package com.multivpn.app

import android.app.Application
import timber.log.Timber

/**
 * Application class for MultiVPN
 */
class MultiVpnApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())

        Timber.d("MultiVPN Application created")
    }
}
