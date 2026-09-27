package com.tvwireguard.app.vpn

import com.wireguard.android.backend.Tunnel

class AppTunnel(
    private val onChanged: (Tunnel.State) -> Unit = {}
) : Tunnel {
    override fun getName(): String = NAME

    override fun onStateChange(newState: Tunnel.State) {
        onChanged(newState)
    }

    companion object {
        const val NAME = "tvwg"
    }
}
