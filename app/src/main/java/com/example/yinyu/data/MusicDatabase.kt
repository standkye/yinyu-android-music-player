package com.example.yinyu.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal data class SavedPlaylist(val id: Long, val name: String, val songIds: List<String>)

internal class MusicDatabase(context: Context) : SQLiteOpenHelper(context, "yinyu_library.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE favorites (song_id TEXT PRIMARY KEY)")
        db.execSQL("CREATE TABLE recent (song_id TEXT PRIMARY KEY, played_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE playlists (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE playlist_songs (playlist_id INTEGER NOT NULL, song_id TEXT NOT NULL, position INTEGER NOT NULL, PRIMARY KEY (playlist_id, song_id), FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    fun favoriteIds(): Set<String> = buildSet {
        readableDatabase.rawQuery("SELECT song_id FROM favorites", null).use { cursor ->
            while (cursor.moveToNext()) add(cursor.getString(0))
        }
    }

    fun toggleFavorite(songId: String): Boolean {
        val db = writableDatabase
        val removed = db.delete("favorites", "song_id = ?", arrayOf(songId)) > 0
        if (!removed) db.insertOrThrow("favorites", null, ContentValues().apply { put("song_id", songId) })
        return !removed
    }

    fun markPlayed(songId: String) {
        writableDatabase.insertWithOnConflict(
            "recent", null,
            ContentValues().apply { put("song_id", songId); put("played_at", System.currentTimeMillis()) },
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun recentIds(limit: Int = 30): List<String> = buildList {
        readableDatabase.rawQuery("SELECT song_id FROM recent ORDER BY played_at DESC LIMIT ?", arrayOf(limit.toString())).use { cursor ->
            while (cursor.moveToNext()) add(cursor.getString(0))
        }
    }

    fun createPlaylist(name: String): Long = writableDatabase.insertOrThrow(
        "playlists", null,
        ContentValues().apply { put("name", name.trim()); put("created_at", System.currentTimeMillis()) },
    )

    fun deletePlaylist(id: Long) {
        writableDatabase.delete("playlists", "id = ?", arrayOf(id.toString()))
    }

    fun addToPlaylist(playlistId: Long, songId: String) {
        val db = writableDatabase
        val next = db.rawQuery("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_songs WHERE playlist_id = ?", arrayOf(playlistId.toString()))
            .use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
        db.insertWithOnConflict(
            "playlist_songs", null,
            ContentValues().apply { put("playlist_id", playlistId); put("song_id", songId); put("position", next) },
            SQLiteDatabase.CONFLICT_IGNORE,
        )
    }

    fun removeFromPlaylist(playlistId: Long, songId: String) {
        writableDatabase.delete("playlist_songs", "playlist_id = ? AND song_id = ?", arrayOf(playlistId.toString(), songId))
    }

    fun playlists(): List<SavedPlaylist> {
        val db = readableDatabase
        val result = mutableListOf<SavedPlaylist>()
        db.rawQuery("SELECT id, name FROM playlists ORDER BY created_at DESC", null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val ids = buildList {
                    db.rawQuery("SELECT song_id FROM playlist_songs WHERE playlist_id = ? ORDER BY position", arrayOf(id.toString()))
                        .use { songs -> while (songs.moveToNext()) add(songs.getString(0)) }
                }
                result += SavedPlaylist(id, cursor.getString(1), ids)
            }
        }
        return result
    }
}
