package com.example.atomic_cinema.di

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.example.atomic_cinema.server.auth.*

import com.example.atomic_cinema.server.cinema.CinemaApi
import com.example.atomic_cinema.server.cinema.CinemaRepository
import com.example.atomic_cinema.server.cinema.CinemaRepositoryImpl
import com.example.atomic_cinema.server.movie.MovieApi
import com.example.atomic_cinema.server.movie.MovieRepository
import com.example.atomic_cinema.server.movie.MovieRepositoryImpl
import com.example.atomic_cinema.server.news.NewsApi
import com.example.atomic_cinema.server.news.NewsRepository
import com.example.atomic_cinema.server.news.NewsRepositoryImpl
import com.example.atomic_cinema.server.profile.ProfileApi
import com.example.atomic_cinema.server.profile.ProfileRepository
import com.example.atomic_cinema.server.profile.ProfileRepositoryImpl
import com.example.atomic_cinema.server.seance.SeanceApi
import com.example.atomic_cinema.server.seance.SeanceRepository
import com.example.atomic_cinema.server.seance.SeanceRepositoryImpl
import com.example.atomic_cinema.server.ticket.LocalDateTimeTypeAdapter
import com.example.atomic_cinema.server.ticket.TicketApi
import com.example.atomic_cinema.server.ticket.TicketRepository
import com.example.atomic_cinema.server.ticket.TicketRepositoryImpl
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
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private fun buildRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.184:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun buildRetrofitForNews(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://newsapi.org/")
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
    fun provideNewsApi(): NewsApi{
        val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe()).create()

        return buildRetrofitForNews(gson).create()
    }

    @SuppressLint("NewApi")
    @Provides
    @Singleton
    fun provideTicketApi(): TicketApi{
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter().nullSafe()).create()


        return buildRetrofit(gson).create()
    }

    @SuppressLint("NewApi")
    @Provides
    @Singleton
    fun provideCinemaApi(): CinemaApi {
        val gson = GsonBuilder().create()

        return buildRetrofit(gson).create()
    }

    @SuppressLint("NewApi")
    @Provides
    @Singleton
    fun provideProfileApi(): ProfileApi{
        val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe()).create()

        return buildRetrofit(gson).create()
    }

    @SuppressLint("NewApi")
    @Provides
    @Singleton
    fun provideMovieApi(): MovieApi{
        val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe(),).create()

        return buildRetrofit(gson).create()
    }

    @SuppressLint("NewApi")
    @Provides
    @Singleton
    fun provideSeanceApi(): SeanceApi{
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter().nullSafe())
            .create()

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
    @Provides
    @Singleton
    fun provideNewsRepository(api: NewsApi) : NewsRepository {
        return NewsRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideMovieRepository(api : MovieApi, prefs : SharedPreferences) : MovieRepository{
        return MovieRepositoryImpl(api, prefs)
    }

    @Provides
    @Singleton
    fun provideCinemaRepository(api : CinemaApi, prefs: SharedPreferences) : CinemaRepository{
        return CinemaRepositoryImpl(api, prefs)
    }

    @Provides
    @Singleton
    fun provideSeanceRepository(api : SeanceApi, prefs : SharedPreferences) : SeanceRepository{
        return SeanceRepositoryImpl(api, prefs)
    }

    @Provides
    @Singleton
    fun provideTicketRepository(api : TicketApi, prefs : SharedPreferences) : TicketRepository{
        return TicketRepositoryImpl(api, prefs)
    }


}