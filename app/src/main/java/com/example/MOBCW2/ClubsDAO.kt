package com.example.MOBCW2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
//creating DAO to retrieve data
@Dao
interface ClubsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertclubs(clubs: List<Club>)

    @Query("SELECT * FROM clubs WHERE name LIKE :searchQuery OR alternate LIKE :searchQuery")
    fun searchClubsWithQuery(searchQuery: String): List<Club>
}