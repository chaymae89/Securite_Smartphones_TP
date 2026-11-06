package com.fintrust.vault

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// V4 — Le jeton de session ne devrait jamais figurer dans un Intent implicite :
// il est interceptable par n'importe quelle application concurrente.
fun partagerNote(activity: Activity, contenuNote: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "$contenuNote\n\n(session: ${Session.token})")
    }
    activity.startActivity(Intent.createChooser(intent, "Partager la note"))
}

class NotesListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }
        layout.addView(TextView(this).apply { text = "Mes notes"; textSize = 22f })

        val note = "Réunion budget vendredi"
        layout.addView(TextView(this).apply {
            text = note; textSize = 16f; setPadding(0, 20, 0, 20)
        })
        layout.addView(Button(this).apply {
            text = "Partager"
            setOnClickListener { partagerNote(this@NotesListActivity, note) }
        })
        layout.addView(Button(this).apply {
            text = "Paramètres du compte"
            setOnClickListener { startActivity(Intent(this@NotesListActivity, SettingsActivity::class.java)) }
        })

        setContentView(layout)
    }
}