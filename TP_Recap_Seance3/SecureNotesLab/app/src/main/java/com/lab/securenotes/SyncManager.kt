package com.lab.securenotes

import android.util.Log

object SyncManager {
    const val API_KEY = "SNL-SYNC-98213"

    fun lancerSynchronisation() {
        Log.d("SecureNotesLab", "Synchronisation lancée avec la clé $API_KEY")
    }
}
