package com.thanhng224.androidcomposebase.sample.demo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
public interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE id = 1")
    public fun observeWeather(): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun saveWeather(entity: WeatherEntity)

    @Query("DELETE FROM weather_cache")
    public suspend fun clearWeather()
}
