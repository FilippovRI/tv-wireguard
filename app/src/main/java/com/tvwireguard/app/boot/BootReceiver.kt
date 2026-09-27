package com.tvwireguard.app.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tvwireguard.app.TvWireguardApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val app = context.applicationContext as? TvWireguardApp ?: return
        if (!app.prefs.autoConnectOnBoot) {
            Log.i(TAG, "Auto-connect on boot disabled")
            return
        }
        if (app.prefs.configText.isBlank()) {
            Log.i(TAG, "No config saved, skip boot connect")
            return
        }

        val pending = goAsync()
        app.appScope.launch {
            try {
                // Give network stack a moment after boot.
                delay(5_000)
                app.tunnelController.connect(reason = "boot")
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
