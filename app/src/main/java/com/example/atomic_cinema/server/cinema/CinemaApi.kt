package com.example.atomic_cinema.server.cinema

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface CinemaApi {
    @GET("allCinema")
    suspend fun getAllCinema() : List<CinemaResponse>

    @POST("hallsCinema")
    suspend fun getHallsCinema(
        @Header("Authorization") token : String,
        @Body request: HallCinemaRequest
    ) : List<HallsResponse>

    @GET("revenueReport")
    suspend fun getFormedRevenueReport(
        @Header("Authorization") token : String
    ) : RevenueResponse

}