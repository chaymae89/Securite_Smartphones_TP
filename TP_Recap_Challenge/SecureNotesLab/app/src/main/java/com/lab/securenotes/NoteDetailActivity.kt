package com.lab.securenotes

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class NoteDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val nomFichier = intent.getStringExtra("cheminFichier") ?: "note_alice.txt"
        val fichier = File(filesDir, "notes/$nomFichier")

        val contenu = if (fichier.exists()) {
            fichier.readText()
        } else {
            "Fichier introuvable."
        }

        findViewById<TextView>(R.id.txtContenuNote).text = contenu
    }
}
