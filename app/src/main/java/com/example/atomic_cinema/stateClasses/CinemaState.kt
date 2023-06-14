package com.example.atomic_cinema.stateClasses

data class CinemaState(
    val isLoading : Boolean = false,
    val id : Int = 0,
    val addressCinema : String = "",
    val numbersPhone : List<String> = listOf()
)


data class RevenueDetails(
    val isLoading : Boolean = false,
    val sumMoney : String = "",
    val sumTickets : String = ""
)


data class HallState(
    val id : Int = 0,
    val idCinema : Int = 0,
    val nameTypeHall : String = "",
    val capacity : String = ""
)
