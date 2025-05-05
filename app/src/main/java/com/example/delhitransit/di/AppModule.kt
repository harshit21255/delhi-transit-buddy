package com.example.delhitransit.di

import android.content.Context
import androidx.room.Room
import com.example.delhitransit.data.local.MetroDatabase
import com.example.delhitransit.data.local.StationDao
import com.example.delhitransit.data.local.dao.MetroLineDao
import com.example.delhitransit.data.repository.MetroRepository
import com.example.delhitransit.viewmodel.JourneyViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Database provider
    @Provides
    @Singleton
    fun provideMetroDatabase(@ApplicationContext context: Context): MetroDatabase {
        return Room.databaseBuilder(
            context,
            MetroDatabase::class.java,
            "metro_database"
        ).build()
    }

    // DAO providers
    @Provides
    @Singleton
    fun provideStationDao(database: MetroDatabase): StationDao {
        return database.stationDao()
    }

    @Provides
    @Singleton
    fun provideMetroLineDao(database: MetroDatabase): MetroLineDao {
        return database.metroLineDao()
    }

    // Repository provider with new dependencies
    @Provides
    @Singleton
    fun provideMetroRepository(
        stationDao: StationDao,
        metroLineDao: MetroLineDao,
        @ApplicationContext context: Context
    ): MetroRepository {
        return MetroRepository(stationDao, metroLineDao, context)
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object JourneyModule {
    @Provides
    @ActivityRetainedScoped
    fun provideJourneyViewModel(@ApplicationContext context: Context): JourneyViewModel {
        return JourneyViewModel(context)
    }
}