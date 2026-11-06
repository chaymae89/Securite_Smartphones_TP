package edu.lab.mobilevault

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.AEADBadTagException
import android.util.Log

/** Coffre de démonstration : clé Keystore et AES-GCM. */
class SecureTokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)
    private val alias = "mobilevault_lab_aes_v1"
    private val keyName = "encrypted_token"

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun existingKey(): SecretKey? =
        (keyStore().getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey

    private fun createKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    fun save(token: String) {
        val key = existingKey() ?: createKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        require(iv.size == 12) { "IV inattendu" }
        val ciphertextAndTag = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val record = byteArrayOf(1) + iv + ciphertextAndTag
        val encoded = Base64.encodeToString(record, Base64.NO_WRAP)
        check(prefs.edit().putString(keyName, encoded).commit()) { "Écriture impossible" }
    }

    fun load(): String? {
        val encoded = prefs.getString(keyName, null) ?: return null
        try {
            val record = Base64.decode(encoded, Base64.NO_WRAP)
            require(record.size >= 1 + 12 + 16) { "Enregistrement trop court" }
            require(record[0].toInt() == 1) { "Version inconnue" }
            val key = existingKey() ?: error("Clé Keystore absente")
            val iv = record.copyOfRange(1, 13)
            val ciphertextAndTag = record.copyOfRange(13, record.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            return String(cipher.doFinal(ciphertextAndTag), Charsets.UTF_8)
        } catch (ex: Exception) {
            Log.d("MobileVaultLab", "load() refusé : ${ex.javaClass.simpleName} - ${ex.message}")
            clear() // Politique du labo : reconnexion après corruption ou perte de clé
            return null
        }
    }

    fun clear() { check(prefs.edit().remove(keyName).commit()) }

    /** Uniquement pour tester l'intégrité : ne pas exposer en production. */
    fun tamperForLab() {
        val encoded = prefs.getString(keyName, null) ?: return
        val record = Base64.decode(encoded, Base64.NO_WRAP)
        if (record.size < 30) return
        record[13] = (record[13].toInt() xor 1).toByte()
        check(prefs.edit().putString(keyName, Base64.encodeToString(record, Base64.NO_WRAP)).commit())
    }

    /** Uniquement pour observer la différence entre deux chiffrements. */
    fun encryptedRecordForLab(): String? = prefs.getString(keyName, null)
    /** P3 — Test d'intégrité : enregistrement intact puis modifié. Ne jamais garder en production. */
    fun integrityTestForLab(): String {
        val encoded = prefs.getString(keyName, null) ?: return "Aucun enregistrement : faites d'abord une connexion fictive"
        val record = Base64.decode(encoded, Base64.NO_WRAP)
        val key = existingKey() ?: return "Clé Keystore absente"

        fun tryDecrypt(r: ByteArray): String = try {
            val iv = r.copyOfRange(1, 13)
            val ciphertextAndTag = r.copyOfRange(13, r.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            cipher.doFinal(ciphertextAndTag)   // résultat volontairement NON affiché
            "OK (déchiffrement réussi)"
        } catch (e: AEADBadTagException) {
            "REFUS : AEADBadTagException (tag invalide)"
        } catch (e: Exception) {
            "REFUS : ${e.javaClass.simpleName}"
        }

        val intact = tryDecrypt(record)
        val modified = record.copyOf()
        modified[13] = (modified[13].toInt() xor 1).toByte()   // inverse 1 bit dans la partie chiffrée
        val altered = tryDecrypt(modified)
        return "Intact : $intact\nModifié : $altered"
    }
    /** P4 — Simule la perte de clé. Émulateur uniquement, jamais en production. */
    fun keyLossTestForLab(): String {
        prefs.getString(keyName, null) ?: return "Aucun enregistrement : faites d'abord une connexion fictive"
        keyStore().deleteEntry(alias)          // supprime UNIQUEMENT l'alias de la clé
        val result = load()                    // load() ne régénère PAS de clé
        val stillStored = prefs.getString(keyName, null) != null
        return "Clé supprimée → load() = " +
                (if (result == null) "null (refus contrôlé)" else "TOKEN RENVOYÉ (anomalie !)") +
                "\nEnregistrement effacé : " + (!stillStored)
    }
}