package com.example.weatherapp
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [SearchHistory::class], version = 2)
abstract  class AppDatabase : RoomDatabase() {
    abstract fun historyDAO(): HistoryDAO

    companion object{
        @Volatile
        private var INSTANCE : AppDatabase? = null
        fun getDatabase(context: Context):AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE= instance
                instance
            }
        }
    }

}