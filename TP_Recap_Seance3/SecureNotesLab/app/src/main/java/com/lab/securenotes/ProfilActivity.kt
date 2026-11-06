package com.lab.securenotes

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        findViewById<TextView>(R.id.txtProfil).text =
            "Profil interne\nUtilisateur : ${Session.utilisateurActuel}\nToken de session : ${Session.token}\nNiveau : administrateur"
    }
}
