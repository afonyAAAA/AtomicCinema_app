package com.example.atomic_cinema.events

sealed class CinemaUIEvent {
    data class IdCinemaChanged(val value: Int) : CinemaUIEvent()
    data class AddressCinemaChanged(val value: String) : CinemaUIEvent()
    data class HallCinemaChanged(val value: Int) : CinemaUIEvent()
    object GetHallsCinema : CinemaUIEvent()
}