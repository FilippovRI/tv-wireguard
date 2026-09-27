package com.tvwireguard.app.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import com.tvwireguard.app.data.Prefs
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class TunnelController(
    private val context: Context,
    private val prefs: Prefs
) {
    private val mutex = Mutex()
    private val backend: Backend by lazy { GoBackend(context.applicationContext) }
    private val tunnel = AppTunnel { state ->
        _state.value = when (state) {
            Tunnel.State.UP -> VpnUiState.Connected
            Tunnel.State.DOWN -> VpnUiState.Idle
            Tunnel.State.TOGGLE -> _state.value
        }
    }

    private val _state = MutableStateFlow<VpnUiState>(VpnUiState.Idle)
    val state: StateFlow<VpnUiState> = _state.asStateFlow()

    fun prepareVpnIntent(): Intent? = VpnService.prepare(context)

    suspend fun connect(reason: String = "manual"): Boolean = mutex.withLock {
        Log.i(TAG, "connect reason=$reason")
        val text = prefs.configText
        if (text.isBlank()) {
            _state.value = VpnUiState.Error("Сначала вставьте WireGuard-конфиг")
            return false
        }
        if (prepareVpnIntent() != null) {
            _state.value = VpnUiState.Error("Нужно разрешение VPN (откройте приложение)")
            return false
        }

        _state.value = VpnUiState.Connecting
        return try {
            val config = withContext(Dispatchers.IO) {
                Config.parse(ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)))
            }
            withContext(Dispatchers.IO) {
                backend.setState(tunnel, Tunnel.State.UP, config)
            }
            VpnKeepAliveService.start(context)
            _state.value = VpnUiState.Connected
            true
        } catch (e: Exception) {
            Log.e(TAG, "connect failed", e)
            _state.value = VpnUiState.Error(e.message ?: "Ошибка подключения")
            false
        }
    }

    suspend fun disconnect() = mutex.withLock {
        _state.value = VpnUiState.Disconnecting
        try {
            withContext(Dispatchers.IO) {
                backend.setState(tunnel, Tunnel.State.DOWN, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "disconnect failed", e)
            _state.value = VpnUiState.Error(e.message ?: "Ошибка отключения")
            return
        } finally {
            VpnKeepAliveService.stop(context)
        }
        _state.value = VpnUiState.Idle
    }

    suspend fun refreshState() {
        val up = withContext(Dispatchers.IO) {
            runCatching { backend.getState(tunnel) == Tunnel.State.UP }.getOrDefault(false)
        }
        if (up) {
            _state.value = VpnUiState.Connected
            VpnKeepAliveService.start(context)
        } else if (_state.value is VpnUiState.Connected) {
            _state.value = VpnUiState.Idle
        }
    }

    companion object {
        private const val TAG = "TunnelController"
    }
}
