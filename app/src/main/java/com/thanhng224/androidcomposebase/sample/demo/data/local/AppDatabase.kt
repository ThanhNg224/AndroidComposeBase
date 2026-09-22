package com.thanhng224.androidcomposebase.sample.demo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeatherEntity::class], version = 1, exportSchema = false)
public abstract class AppDatabase : RoomDatabase() {
    public abstract fun weatherDao(): WeatherDao
}
