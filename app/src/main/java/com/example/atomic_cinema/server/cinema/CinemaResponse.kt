package com.example.atomic_cinema.server.cinema

data class CinemaResponse(
    val id : Int,
    val addressCinema : String,
    val numbersPhone : List<String>
)
