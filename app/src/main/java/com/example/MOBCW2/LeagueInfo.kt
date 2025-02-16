package com.example.MOBCW2

import androidx.room.Entity
import androidx.room.PrimaryKey
//creating entity class
@Entity(tableName = "football_leagues")
data class LeagueInfo(
    @PrimaryKey val idLeague: String,
    val strLeague: String,
    val strSport: String,
    val strLeagueAlternate: String
)
