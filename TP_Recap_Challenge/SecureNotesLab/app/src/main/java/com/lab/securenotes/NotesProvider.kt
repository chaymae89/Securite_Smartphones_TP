package com.lab.securenotes

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class NotesDbHelper(context: Context) :
    SQLiteOpenHelper(context, "notes.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY, proprietaire TEXT, contenu TEXT)")
        db.execSQL("INSERT INTO notes (proprietaire, contenu) VALUES ('alice', 'Rendez vous client reporté a vendredi.')")
        db.execSQL("INSERT INTO notes (proprietaire, contenu) VALUES ('bob', 'Code d acces entrepot : 4471.')")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {}
}

class NotesProvider : ContentProvider() {
    private lateinit var dbHelper: NotesDbHelper

    override fun onCreate(): Boolean {
        dbHelper = NotesDbHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return db.query("notes", projection, null, null, null, null, sortOrder)
    }

    override fun getType(uri: Uri) = "vnd.android.cursor.dir/notes"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0
}
