package com.example.atomic_cinema.server.seance

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

data class SeanceDeleteRequest(
    val id : Int,
)
