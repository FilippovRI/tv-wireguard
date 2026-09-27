package com.tvwireguard.app.vpn

sealed class VpnUiState {
    data object Idle : VpnUiState()
    data object Connecting : VpnUiState()
    data object Connected : VpnUiState()
    data object Disconnecting : VpnUiState()
    data class Error(val message: String) : VpnUiState()
}
