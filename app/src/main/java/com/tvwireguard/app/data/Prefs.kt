package com.tvwireguard.app.data

import android.content.Context
import androidx.core.content.edit

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var configText: String
        get() = sp.getString(KEY_CONFIG, "") ?: ""
        set(value) = sp.edit { putString(KEY_CONFIG, value.trim()) }

    var autoConnectOnBoot: Boolean
        get() = sp.getBoolean(KEY_AUTO_BOOT, true)
        set(value) = sp.edit { putBoolean(KEY_AUTO_BOOT, value) }

    var openYoutubeAfterConnect: Boolean
        get() = sp.getBoolean(KEY_YT_AFTER, true)
        set(value) = sp.edit { putBoolean(KEY_YT_AFTER, value) }

    companion object {
        private const val PREFS = "tv_wireguard"
        private const val KEY_CONFIG = "wg_config"
        private const val KEY_AUTO_BOOT = "auto_boot"
        private const val KEY_YT_AFTER = "yt_after_connect"
    }
}
