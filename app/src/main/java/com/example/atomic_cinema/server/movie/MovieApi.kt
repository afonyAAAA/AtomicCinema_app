package com.example.atomic_cinema.server.movie

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MovieApi {

    @GET("allMovie")
    suspend fun getAllMovie() : List<MovieResponse>

    @GET("allGenre")
    suspend fun getAllGenre() : List<GenreResponse>

    @POST("filterMovie")
    suspend fun getFilteredMovie(
        @Body request: MovieFilterRequest
    ) : List<MovieResponse>

    @POST("addMovie")
    suspend fun addMovie(
        @Header("Authorization") token : String,
        @Body request: MovieAddRequest
    )

    @POST("updateMovie")
    suspend fun updateMovie(
        @Header("Authorization") token : String,
        @Body request: MovieUpdateRequest
    )

    @POST("deleteMovie")
    suspend fun deleteMovie(
        @Header("Authorization") token : String,
        @Body request: MovieDeleteRequest
    )



}