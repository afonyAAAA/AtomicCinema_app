package com.example.atomic_cinema.server.cinema

interface CinemaRepository {
    suspend fun getAllCinema() : CinemaResults<List<CinemaResponse>>

    suspend fun getRevenueReport() : CinemaResults<RevenueResponse>

    suspend fun getHallsCinema(request : HallCinemaRequest) : CinemaResults<List<HallsResponse>>
}