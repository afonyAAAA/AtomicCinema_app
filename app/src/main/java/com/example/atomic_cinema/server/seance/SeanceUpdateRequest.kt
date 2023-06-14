package com.example.atomic_cinema.server.seance

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

data class SeanceUpdateRequest(
    val id : Int,
    val dateEnd : LocalDate,
    val dateStart : LocalDate,
    val timeStart : LocalTime,
    val timeEnd : LocalTime,
    val price : String,
    val idMovie : Int,
    val idHall : Int
)
