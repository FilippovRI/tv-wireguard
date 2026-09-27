package com.tvwireguard.app

import android.app.Application
import android.util.Log
import com.tvwireguard.app.data.Prefs
import com.tvwireguard.app.vpn.TunnelController
import com.wireguard.android.backend.GoBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TvWireguardApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var prefs: Prefs
        private set
    lateinit var tunnelController: TunnelController
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = Prefs(this)
        tunnelController = TunnelController(this, prefs)

        GoBackend.setAlwaysOnCallback {
            Log.i(TAG, "Always-on VPN triggered by system")
            appScope.launch {
                tunnelController.connect(reason = "always-on")
            }
        }
    }

    companion object {
        private const val TAG = "TvWireguardApp"
        lateinit var instance: TvWireguardApp
            private set
    }
}
