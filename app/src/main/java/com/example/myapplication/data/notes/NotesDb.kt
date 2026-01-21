package com.example.myapplication.data.notes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1)
abstract class NotesDb : RoomDatabase() {
    abstract fun dao(): NotesDao

    companion object {
        fun create(ctx: Context): NotesDb =
            Room.databaseBuilder(ctx, NotesDb::class.java, "notes.db").build()
    }
}