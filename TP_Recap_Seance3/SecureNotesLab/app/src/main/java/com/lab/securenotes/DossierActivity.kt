package com.lab.securenotes

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DossierActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val employeId = intent.getStringExtra("employeId") ?: ""

        // Vulnérable : vérifie seulement que l'identifiant existe,
        // jamais que l'utilisateur courant a le droit de voir CE dossier précis.
        val dossier = dossiers[employeId] ?: "Dossier introuvable"

        val vue = TextView(this)
        vue.setPadding(40, 80, 40, 40)
        vue.textSize = 16f
        vue.text = "Dossier employé ($employeId)\n\n$dossier"
        setContentView(vue)
    }

    companion object {
        val dossiers = mapOf(
            "alice" to "Alice Martin, CyberLab, poste : analyste sécurité",
            "bob" to "Bob Durand, CyberLab, poste : développeur"
        )
    }
}
