package com.example.atomic_cinema.server.ticket

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TicketsUsersResponse(
    val id : Int,
    val idUser : Int,
    val idSeance : Int,
    val nameStatus : String,
    val count : Int,
    val dateTime : String,
    val returned : Boolean,
    val addressCinema : String,
    val nameMovie : String,
    val linkImage : String,
    val duration : Int,
    val ageRating : String,
    val timeStart : String,
    val timeEnd : String,
    val price : Double,
    val nameTypeHall : String,
    val dateStartSeance : String
)
