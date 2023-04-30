package com.example.atomic_cinema.di

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.example.atomic_cinema.server.auth.AuthApi
import com.example.atomic_cinema.server.auth.AuthRepository
import com.example.atomic_cinema.server.auth.AuthRepositoryImpl
import com.example.atomic_cinema.server.auth.LocalDateTypeAdapter
import com.example.atomic_cinema.server.profile.ProfileApi
import com.example.atomic_cinema.server.profile.ProfileRepository
import com.example.atomic_cinema.server.profile.ProfileRepositoryImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.time.LocalDate
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    private fun buildRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


    @SuppressLint("NewApi")
    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi{
        val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe()).create()

        return buildRetrofit(gson).create()
    }

    @SuppressLint("NewApi")
    @Provides
    @Singleton
    fun provideProfileApi(): ProfileApi{
        val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe()).create()

        return buildRetrofit(gson).create()
    }

    @Provides
    @Singleton
    fun provideSharedPref(app : Application) : SharedPreferences{
        return app.getSharedPreferences("prefs", MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api : AuthApi, prefs : SharedPreferences) : AuthRepository{
        return AuthRepositoryImpl(api, prefs)
    }
    @Provides
    @Singleton
    fun provideProfileRepository(api: ProfileApi, prefs : SharedPreferences) : ProfileRepository{
        return ProfileRepositoryImpl(api, prefs)
    }
}