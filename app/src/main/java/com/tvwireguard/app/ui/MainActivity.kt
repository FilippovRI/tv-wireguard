package com.tvwireguard.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tvwireguard.app.R
import com.tvwireguard.app.TvWireguardApp
import com.tvwireguard.app.databinding.ActivityMainBinding
import com.tvwireguard.app.util.YoutubeLauncher
import com.tvwireguard.app.vpn.VpnUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val app get() = application as TvWireguardApp

    private val vpnPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch { connectAndMaybeYoutube(openYoutube = pendingYoutube) }
        } else {
            Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    private var pendingYoutube = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConnect.setOnClickListener {
            requestConnect(openYoutube = false)
        }
        binding.btnDisconnect.setOnClickListener {
            lifecycleScope.launch { app.tunnelController.disconnect() }
        }
        binding.btnYoutube.setOnClickListener {
            requestConnect(openYoutube = true)
        }
        binding.btnConfig.setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
        }
        binding.switchBoot.isChecked = app.prefs.autoConnectOnBoot
        binding.switchBoot.setOnCheckedChangeListener { _, checked ->
            app.prefs.autoConnectOnBoot = checked
        }

        lifecycleScope.launch {
            app.tunnelController.state.collectLatest { render(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { app.tunnelController.refreshState() }
        binding.txtConfigHint.text = if (app.prefs.configText.isBlank()) {
            getString(R.string.config_missing)
        } else {
            getString(R.string.config_ready)
        }
    }

    private fun requestConnect(openYoutube: Boolean) {
        if (app.prefs.configText.isBlank()) {
            Toast.makeText(this, R.string.config_missing, Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ConfigActivity::class.java))
            return
        }
        pendingYoutube = openYoutube
        val prepare = app.tunnelController.prepareVpnIntent()
        if (prepare != null) {
            vpnPermission.launch(prepare)
        } else {
            lifecycleScope.launch { connectAndMaybeYoutube(openYoutube) }
        }
    }

    private suspend fun connectAndMaybeYoutube(openYoutube: Boolean) {
        val ok = app.tunnelController.connect(reason = if (openYoutube) "youtube" else "manual")
        if (ok && openYoutube) {
            if (!YoutubeLauncher.launch(this)) {
                Toast.makeText(this, R.string.youtube_missing, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun render(state: VpnUiState) {
        val status = when (state) {
            VpnUiState.Idle -> getString(R.string.status_idle)
            VpnUiState.Connecting -> getString(R.string.status_connecting)
            VpnUiState.Connected -> getString(R.string.status_connected)
            VpnUiState.Disconnecting -> getString(R.string.status_disconnecting)
            is VpnUiState.Error -> getString(R.string.status_error, state.message)
        }
        binding.txtStatus.text = status
        val connected = state is VpnUiState.Connected
        val busy = state is VpnUiState.Connecting || state is VpnUiState.Disconnecting
        binding.btnConnect.isEnabled = !connected && !busy
        binding.btnDisconnect.isEnabled = connected && !busy
        binding.btnYoutube.isEnabled = !busy
        binding.progress.visibility = if (busy) View.VISIBLE else View.GONE
    }
}
