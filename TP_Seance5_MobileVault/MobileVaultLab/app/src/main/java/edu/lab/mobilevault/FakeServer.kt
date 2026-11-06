package edu.lab.mobilevault

import java.util.UUID

/** P5 — Faux serveur LOCAL (mock). Ce n'est PAS une vraie API réseau. */
class FakeServer {
    var authorizationCheckEnabled = true          // contrôle ② : propriété de la ressource

    private data class Session(val userId: String, val expiresAt: Long)
    private val sessions = mutableMapOf<String, Session>()
    private val accountOwner = mapOf("ACC-ALICE" to "alice", "ACC-BOB" to "bob")

    /** Le serveur crée la session et décide de sa durée de validité. */
    fun login(userId: String, ttlMs: Long): String {
        val token = "srv_" + UUID.randomUUID()
        sessions[token] = Session(userId, System.currentTimeMillis() + ttlMs)
        return token
    }

    /** Renvoie un code HTTP : 200, 401 ou 403. */
    fun getAccount(token: String, accountId: String): Int {
        val s = sessions[token] ?: return 401                       // ① session inconnue
        if (System.currentTimeMillis() >= s.expiresAt) return 401   // ① session expirée
        if (authorizationCheckEnabled && accountOwner[accountId] != s.userId) return 403 // ② pas propriétaire
        return 200
    }
}