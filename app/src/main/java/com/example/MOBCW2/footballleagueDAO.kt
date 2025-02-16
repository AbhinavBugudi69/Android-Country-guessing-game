package com.example.MOBCW2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
//Creating DAO interface for queries
@Dao
interface footballleagueDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertleagues(footballLeagues: List<LeagueInfo>)

    @Query("SELECT * FROM football_leagues")
    fun getAllLeagues(): List<LeagueInfo>
}