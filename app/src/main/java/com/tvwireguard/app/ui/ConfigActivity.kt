package com.tvwireguard.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tvwireguard.app.R
import com.tvwireguard.app.TvWireguardApp
import com.tvwireguard.app.databinding.ActivityConfigBinding
import com.wireguard.config.Config
import java.io.ByteArrayInputStream

class ConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigBinding
    private val app get() = application as TvWireguardApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editConfig.setText(app.prefs.configText.ifBlank { SAMPLE_HINT })
        binding.btnSave.setOnClickListener { save() }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnInsertTemplate.setOnClickListener {
            binding.editConfig.setText(SAMPLE_TEMPLATE)
            binding.editConfig.setSelection(0)
        }
    }

    private fun save() {
        val text = binding.editConfig.text?.toString()?.trim().orEmpty()
        if (text.isBlank() || text.startsWith("# Вставьте")) {
            Toast.makeText(this, R.string.config_empty, Toast.LENGTH_LONG).show()
            return
        }
        try {
            Config.parse(ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.config_invalid, e.message ?: ""),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        app.prefs.configText = text
        Toast.makeText(this, R.string.config_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        private val SAMPLE_HINT = """
            # Вставьте сюда конфиг peer с Keenetic / сервера
            # Endpoint = ВАШ_ВНЕШНИЙ_IP:ПОРТ
            # AllowedIPs = 0.0.0.0/0
        """.trimIndent()

        private val SAMPLE_TEMPLATE = """
            [Interface]
            PrivateKey = CLIENT_PRIVATE_KEY
            Address = 10.10.10.2/32
            DNS = 1.1.1.1

            [Peer]
            PublicKey = SERVER_PUBLIC_KEY
            PresharedKey = OPTIONAL_PSK
            Endpoint = YOUR.PUBLIC.IP.OR.DDNS:51820
            AllowedIPs = 0.0.0.0/0
            PersistentKeepalive = 25
        """.trimIndent()
    }
}
