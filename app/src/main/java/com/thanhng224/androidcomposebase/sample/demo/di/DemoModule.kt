package com.thanhng224.androidcomposebase.sample.demo.di

import com.thanhng224.androidcomposebase.sample.demo.data.datasource.DemoApiService
import com.thanhng224.androidcomposebase.sample.demo.data.datasource.DemoRemoteDataSource
import com.thanhng224.androidcomposebase.sample.demo.data.datasource.DemoRemoteDataSourceImpl
import com.thanhng224.androidcomposebase.sample.demo.data.repository.DemoRepositoryImpl
import com.thanhng224.androidcomposebase.sample.demo.domain.repository.DemoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DemoModule {
    @Binds
    @Singleton
    abstract fun bindDemoRemoteDataSource(implementation: DemoRemoteDataSourceImpl): DemoRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindDemoRepository(implementation: DemoRepositoryImpl): DemoRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DemoNetworkModule {
    @Provides
    @Singleton
    fun provideDemoApiService(retrofit: Retrofit): DemoApiService = retrofit.create(DemoApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DemoDatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
    ): com.thanhng224.androidcomposebase.sample.demo.data.local.AppDatabase =
        androidx.room.Room
            .databaseBuilder(
                context,
                com.thanhng224.androidcomposebase.sample.demo.data.local.AppDatabase::class.java,
                "demo_weather.db",
            ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideWeatherDao(
        database: com.thanhng224.androidcomposebase.sample.demo.data.local.AppDatabase,
    ): com.thanhng224.androidcomposebase.sample.demo.data.local.WeatherDao = database.weatherDao()
}
