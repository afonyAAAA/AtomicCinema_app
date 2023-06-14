package com.example.atomic_cinema.events

import com.example.atomic_cinema.stateClasses.SeanceState
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

sealed class SeanceUIEvent {
    data class IdHallChanged(val value : Int) : SeanceUIEvent()
    data class IdMovieChanged(val value : Int) : SeanceUIEvent()
    data class DateEndChanged(val value: LocalDate) : SeanceUIEvent()
    data class DateStartChanged(val value: LocalDate) : SeanceUIEvent()
    data class TimeEndChanged(val value : LocalTime) : SeanceUIEvent()
    data class TimeStartChanged(val value : LocalTime) : SeanceUIEvent()
    data class PriceChanged(val value : Double) : SeanceUIEvent()
    data class SelectedDateChanged(val value : LocalDate) : SeanceUIEvent()
    data class ShowSeance(val value : Int) : SeanceUIEvent()
    data class PlaceHolderChanged(val value : Boolean) : SeanceUIEvent()
    data class TypeHallChanged(val value : String) : SeanceUIEvent()
    data class AddressCinemaChanged(val value : String) : SeanceUIEvent()
    data class BoxEmptyISVisible(val value : Boolean) : SeanceUIEvent()
    data class ShowDetailSeance(val value: Boolean) : SeanceUIEvent()
    data class SeanceChanged(val value: SeanceState) : SeanceUIEvent()
    data class DeleteSeance(val value: Int) : SeanceUIEvent()
    data class GetSeancesCinema(val value: String) : SeanceUIEvent()
    object AddSeance : SeanceUIEvent()
    object UpdateSeance : SeanceUIEvent()
}
