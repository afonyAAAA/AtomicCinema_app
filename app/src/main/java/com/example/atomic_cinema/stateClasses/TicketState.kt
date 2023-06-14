package com.example.atomic_cinema.stateClasses

import java.time.LocalDateTime


data class TicketState(
    val isLoading : Boolean = false,
    val idTicket : Int = 0,
    val count : Int = 1,
    val dateTime : LocalDateTime = LocalDateTime.now(),
    val idUser : Int = 0,
    val idStatusPayment : Int = 0,
    val idSeance : Int = 0,
    val expandedNumberTextField : Boolean = false,
    val sumPay : String = "",
    val showPayAlertDialog : Boolean = false,
    val loadingPay : Boolean = false,
    val nameStatus : String = "",
    val returned : Boolean = false,
    val addressCinema : String = "",
    val nameMovie : String = "",
    val linkImage : String = "",
    val duration : Int = 0,
    val ageRating : String = "",
    val timeStart : String = "",
    val timeEnd : String = "",
    val price : Double = 0.0,
    val nameTypeHall : String = "",
    val dateStartSeance : String = "",
    val detailsTicketChanged : Boolean = false
)

