package com.argento.eoloapp

import android.app.Application
import com.mercadolibre.android.point_integration_sdk.nativesdk.MPManager
import com.mercadolibre.android.point_integration_sdk.nativesdk.configurable.MPConfigBuilder

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = MPConfigBuilder(this, DEMO_APP_CLIENT_ID)
            .withBluetoothConfig()
            .withBluetoothUIConfig()
            .build()
        MPManager.initialize(this, config)
    }

    companion object {
        private const val DEMO_APP_CLIENT_ID = "3639911048528427"
    }
}