// AppModule.kt (updated)
package com.example.delhitransit.di

import android.content.Context
import androidx.room.Room
import com.example.delhitransit.data.local.MetroDatabase
import com.example.delhitransit.data.local.StationDao
import com.example.delhitransit.data.local.dao.*
import com.example.delhitransit.data.repository.BusRepository
import com.example.delhitransit.data.repository.MetroRepository
import com.example.delhitransit.services.AccessibilityService
import com.example.delhitransit.viewmodel.BusJourneyViewModel
import com.example.delhitransit.viewmodel.MetroJourneyViewModel
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
    @Provides
    @Singleton
    fun provideMetroDatabase(@ApplicationContext context: Context): MetroDatabase {
        return Room.databaseBuilder(
            context,
            MetroDatabase::class.java,
            "metro_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    // Metro DAO providers
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

    // Bus DAO providers
    @Provides
    @Singleton
    fun provideBusAgencyDao(database: MetroDatabase): BusAgencyDao {
        return database.busAgencyDao()
    }

    @Provides
    @Singleton
    fun provideBusRouteDao(database: MetroDatabase): BusRouteDao {
        return database.busRouteDao()
    }

    @Provides
    @Singleton
    fun provideBusStopDao(database: MetroDatabase): BusStopDao {
        return database.busStopDao()
    }

    @Provides
    @Singleton
    fun provideBusTripDao(database: MetroDatabase): BusTripDao {
        return database.busTripDao()
    }

    @Provides
    @Singleton
    fun provideStopTimeDao(database: MetroDatabase): StopTimeDao {
        return database.stopTimeDao()
    }

    @Provides
    @Singleton
    fun provideCombinedBusDataDao(database: MetroDatabase): BusRouteTripDao {
        return database.combinedBusDataDao()
    }

    @Provides
    @Singleton
    fun provideBusStopSequenceDao(database: MetroDatabase): BusStopSequenceDao {
        return database.busStopSequenceDao()
    }


    // Repository providers
    @Provides
    @Singleton
    fun provideMetroRepository(
        stationDao: StationDao,
        metroLineDao: MetroLineDao,
        @ApplicationContext context: Context
    ): MetroRepository {
        return MetroRepository(stationDao, metroLineDao, context)
    }

    @Provides
    @Singleton
    fun provideBusRepository(
        busAgencyDao: BusAgencyDao,
        busRouteDao: BusRouteDao,
        busStopDao: BusStopDao,
        busTripDao: BusTripDao,
        busStopSequenceDao: BusStopSequenceDao,
        stopTimeDao: StopTimeDao,
        busRouteTripDao: BusRouteTripDao,
        @ApplicationContext context: Context
    ): BusRepository {
        return BusRepository(
            busAgencyDao,
            busRouteDao,
            busStopDao,
            busTripDao,
            busStopSequenceDao,
            stopTimeDao,
            busRouteTripDao,
            context
        )
    }

    // Accessibility service provider
    @Provides
    @Singleton
    fun provideAccessibilityService(@ApplicationContext context: Context): AccessibilityService {
        return AccessibilityService(context)
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object JourneyModule {
    @Provides
    @ActivityRetainedScoped
    fun provideJourneyViewModel(@ApplicationContext context: Context): MetroJourneyViewModel {
        return MetroJourneyViewModel(context)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideBusJourneyViewModel(@ApplicationContext context: Context): BusJourneyViewModel {
        return BusJourneyViewModel(context)
    }
}