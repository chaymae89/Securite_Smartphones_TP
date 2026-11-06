package edu.lab.mobilevault

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.UUID
import android.util.Log

/** Version corrigée du TP : aucune donnée bancaire réelle. */
class MainActivity : Activity() {
    private lateinit var store: SecureTokenStore
    private lateinit var status: TextView
    private lateinit var details: TextView
    private var expiresAt = 0L // Horloge de démonstration, PAS validation serveur

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = SecureTokenStore(this)
        deleteSharedPreferences("vault_lab")
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 24)
        }
        root.addView(TextView(this).apply { text = "MobileVault · TP guidé"; textSize = 23f })
        status = TextView(this).apply { textSize = 18f; setPadding(0, 18, 0, 12) }
        details = TextView(this).apply { textSize = 13f }
        root.addView(status)
        root.addView(details)
        fun addButton(label: String, action: () -> Unit) {
            root.addView(Button(this).apply { text = label; setOnClickListener { action() } })
        }
        addButton("1. Connexion fictive") {
            store.save("demo_" + UUID.randomUUID().toString())
            Log.d("MobileVaultLab", "Connexion fictive réussie (session enregistrée)")
            expiresAt = System.currentTimeMillis() + 60_000
            refresh()
        }
        addButton("2. Restaurer session") { refresh() }
        addButton("3. Altérer le chiffré (test)") { store.tamperForLab(); refresh() }
        addButton("4. Simuler expiration locale") { expiresAt = 1L; refresh() }
        addButton("5. Déconnexion") { store.clear(); expiresAt = 0L; refresh() }
        addButton("6. Test intégrité (P3)") {
            val result = store.integrityTestForLab()
            Log.d("MobileVaultLab", "P3 -> $result")
            details.text = result
        }
        addButton("7. Perte de clé (P4)") {
            val result = store.keyLossTestForLab()
            Log.d("MobileVaultLab", "P4 -> $result")
            status.text = "Aucune session : reconnectez-vous"
            details.text = result
        }
        addButton("8. Scénarios serveur (P5)") {
            val result = runP5Scenarios()
            result.lines().forEach { Log.d("MobileVaultLab", "P5 -> $it") }
            details.text = result
        }
        setContentView(root)
        refresh()
    }
    /** Réaction du client Android à chaque code HTTP. */
    private fun handleResponse(code: Int): String = when (code) {
        200 -> "200 OK → données affichées"
        401 -> { store.clear(); "401 → session invalidée, reconnexion demandée" }
        403 -> "403 → accès refusé, session conservée, droits inchangés"
        else -> "$code → erreur inattendue"
    }

    private fun runP5Scenarios(): String {
        val server = FakeServer()
        val alice = server.login("alice", ttlMs = 60_000)     // session valide 1 min
        val expired = server.login("alice", ttlMs = -1)       // session déjà expirée
        val out = StringBuilder()
        out.appendLine("S1 Alice → son compte : " + handleResponse(server.getAccount(alice, "ACC-ALICE")))
        out.appendLine("S2 Jeton expiré → son compte : " + handleResponse(server.getAccount(expired, "ACC-ALICE")))
        out.appendLine("S3 Alice → compte de Bob : " + handleResponse(server.getAccount(alice, "ACC-BOB")))
        server.authorizationCheckEnabled = false               // on retire le contrôle ②
        out.append("S4 SANS contrôle serveur, Alice → compte de Bob : " + handleResponse(server.getAccount(alice, "ACC-BOB")))
        return out.toString()
    }
    private fun refresh() {
        val token = store.load()
        status.text = when {
            token == null -> "Aucune session (ou donnée invalide)"
            expiresAt != 0L && System.currentTimeMillis() >= expiresAt -> "Session expirée (simulation locale)"
            else -> "Session locale restaurée"
        }
        details.text = "Le token n'est jamais affiché. La validité serveur n'est pas simulée ici."
    }
}