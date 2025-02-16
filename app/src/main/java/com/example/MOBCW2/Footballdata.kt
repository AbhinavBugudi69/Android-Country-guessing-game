package com.example.MOBCW2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//actually creating database
@Database(entities = [LeagueInfo::class, Club::class], version = 1)
abstract class footballdata : RoomDatabase() {
    abstract fun footballLeagueDao(): footballleagueDAO
    abstract fun ClubsDAO(): ClubsDAO

    companion object {
        @Volatile
        private var INSTANCE: footballdata? = null

        fun getDatabase(context: Context): footballdata {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    footballdata::class.java,
                    "football_leagues_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}