package com.example.atomic_cinema.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic_cinema.events.CinemaUIEvent
import com.example.atomic_cinema.server.cinema.CinemaRepository
import com.example.atomic_cinema.server.cinema.CinemaResults
import com.example.atomic_cinema.server.cinema.HallCinemaRequest
import com.example.atomic_cinema.stateClasses.CinemaState
import com.example.atomic_cinema.stateClasses.HallState
import com.example.atomic_cinema.stateClasses.MovieState
import com.example.atomic_cinema.stateClasses.RevenueDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CinemaViewModel @Inject constructor(
private val repository : CinemaRepository
) : ViewModel() {

    var state by mutableStateOf(CinemaState())
    var listStateCinema = mutableListOf<CinemaState>()
    var listStateHall = mutableListOf<HallState>()

    private val resultChannel = Channel<CinemaResults<Unit>>()
    val cinemaResults = resultChannel.receiveAsFlow()

    init {
        getAllCinema()
    }

    fun onEvent(event : CinemaUIEvent){
        when(event){
            is CinemaUIEvent.AddressCinemaChanged -> {
                state = state.copy(addressCinema = event.value)
            }
            is CinemaUIEvent.HallCinemaChanged -> {
                TODO()
            }
            is CinemaUIEvent.IdCinemaChanged -> {
                state = state.copy(id = event.value)
            }
            CinemaUIEvent.GetHallsCinema -> {
                getCinemaHalls()
            }
        }
    }


    private fun getAllCinema(){
        viewModelScope.launch {

            state = state.copy(isLoading = true)

            if(listStateCinema.isNotEmpty()){
                listStateCinema.clear()
            }

            val result = repository.getAllCinema()

            if(result.data != null){
               result.data.forEach { cinema ->
                   listStateCinema.add(CinemaState(
                       addressCinema = cinema.addressCinema,
                       id = cinema.id,
                       numbersPhone = cinema.numbersPhone
                   ))
               }
            }else{
                resultChannel.send(CinemaResults.UnknownError())
            }

            resultChannel.send(CinemaResults.OK())

            state = state.copy(isLoading = false)
        }
    }

    private fun getCinemaHalls(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            if(listStateHall.isNotEmpty()){
                listStateHall.clear()
            }

            val result = repository.getHallsCinema(HallCinemaRequest(state.id))

            if(result.data != null){
                result.data.forEach { hall ->
                    listStateHall.add(HallState(
                        id = hall.idHall,
                        nameTypeHall = hall.nameTypeHall,
                        capacity = hall.capacity
                    ))
                }
            }

            state = state.copy(isLoading = false)
        }
    }

}