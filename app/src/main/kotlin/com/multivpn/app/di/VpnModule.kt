package com.multivpn.app.di

import android.content.Context
import androidx.room.Room
import com.multivpn.app.data.database.VpnDatabase
import com.multivpn.app.data.repository.VpnRepositoryImpl
import com.multivpn.app.domain.repository.VpnRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Hilt dependency injection module
 */
@Module
@InstallIn(SingletonComponent::class)
object VpnModule {

    @Provides
    @Singleton
    fun provideVpnDatabase(
        @ApplicationContext context: Context
    ): VpnDatabase {
        return Room.databaseBuilder(
            context,
            VpnDatabase::class.java,
            "multivpn_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideVpnRepository(
        database: VpnDatabase
    ): VpnRepository {
        return VpnRepositoryImpl(database)
    }
}
