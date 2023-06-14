package com.example.atomic_cinema.server.seance

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SeanceApi {

    @POST("getSeanceMovie")
    suspend fun getSeanceMovie(
        @Body request: SeanceMovieRequest
    ) : List<SeanceResponse>

    @POST("getSeanceCinema")
    suspend fun getSeanceCinema(
        @Body request: SeanceCinemaRequest
    ) : List<SeanceResponse>

    @POST("addSeance")
    suspend fun addSeance(
        @Header("Authorization") token : String,
        @Body request: SeanceAddRequest
    )

    @POST("updateSeance")
    suspend fun updateSeance(
        @Header("Authorization") token : String,
        @Body request: SeanceUpdateRequest
    )

    @POST("deleteSeance")
    suspend fun deleteSeance(
        @Header("Authorization") token : String,
        @Body request: SeanceDeleteRequest
    )

}