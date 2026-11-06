package com.fintrust.vault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Accepte n'importe quel identifiant/mot de passe non vide (démonstration),
// puis ouvre la liste des notes.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }
        val user = EditText(this).apply { hint = "Identifiant" }
        val pass = EditText(this).apply { hint = "Mot de passe" }
        val btn = Button(this).apply { text = "Se connecter" }

        layout.addView(TextView(this).apply { text = "FinTrustVault"; textSize = 24f })
        layout.addView(user)
        layout.addView(pass)
        layout.addView(btn)

        btn.setOnClickListener {
            if (user.text.isNotEmpty() && pass.text.isNotEmpty()) {
                Session.estConnecte = true
                Session.utilisateur = user.text.toString()
                startActivity(Intent(this, NotesListActivity::class.java))
            } else {
                Toast.makeText(this, "Identifiant et mot de passe requis", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(layout)
    }
}