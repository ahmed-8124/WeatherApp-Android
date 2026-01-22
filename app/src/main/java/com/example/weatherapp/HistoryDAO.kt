package com.example.weatherapp
import android.icu.text.StringSearch
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface HistoryDAO {
    @Query("SELECT * FROM search_history WHERE cityName = :name LIMIT 1")
    suspend fun getCityByName(name: String): SearchHistory?

    @Query("SELECT* FROM search_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistory)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Delete
    suspend fun deleteSearch(search: SearchHistory)

}