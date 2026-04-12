package com.flacconverter.db

import android.content.Context
import androidx.room.*
import com.flacconverter.model.ConversionHistory

@Dao
interface HistoryDao {
    @Query("SELECT * FROM conversion_history ORDER BY id DESC")
    suspend fun getAll(): List<ConversionHistory>

    @Insert
    suspend fun insert(entry: ConversionHistory)

    @Query("DELETE FROM conversion_history")
    suspend fun clearAll()

    @Delete
    suspend fun delete(entry: ConversionHistory)
}

@Database(entities = [ConversionHistory::class], version = 1, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: HistoryDatabase? = null
        fun getInstance(context: Context): HistoryDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext,
                    HistoryDatabase::class.java, "flac_converter_db")
                    .build().also { INSTANCE = it }
            }
    }
}
