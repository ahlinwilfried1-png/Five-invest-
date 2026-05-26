package com.example.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var db: AppDatabase? = null
    private var repository: AppRepository? = null

    fun getDatabase(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "finvest_database"
            )
            .fallbackToDestructiveMigration()
            .build()
            db = instance
            instance
        }
    }

    fun getRepository(context: Context): AppRepository {
        return repository ?: synchronized(this) {
            val repo = AppRepository(getDatabase(context))
            repository = repo
            repo
        }
    }
}
