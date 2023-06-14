package com.example.atomic_cinema.server.seance

data class SeanceResponse(
    val id : Int,
    val dateEnd : String,
    val dateStart : String,
    val timeStart : String,
    val timeEnd : String,
    val price : Double,
    val idMovie : Int,
    val idHall : Int,
    val nameTypeHall : String,
    val addressCinema : String
)

