package com.example.atomic_cinema.stateClasses

import com.himanshoe.kalendar.model.KalendarEvent
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime


data class SeanceState(
    val isLoading : Boolean = false,
    val id : Int = 0,
    val dateEnd : LocalDate? = null,
    val dateStart : LocalDate? = null,
    val timeStart : LocalTime? = null,
    val timeEnd : LocalTime? = null,
    val price : Double = 0.0,
    val idMovie : Int = 0,
    val idHall : Int = 0,
    val addressCinema : String = "",
    val typeHall : String = "",
    val placeHolderIsOpen : Boolean = false,
    val selectedDateSeance : LocalDate = LocalDate.now(),
    val calendarEvents : MutableList<KalendarEvent> = mutableListOf(),
    val emptyBoxIsVisible : Boolean = false,
    val showDetailsSeance : Boolean = false,
)

