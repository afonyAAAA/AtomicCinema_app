package com.example.atomic_cinema.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic_cinema.events.SeanceUIEvent
import com.example.atomic_cinema.server.seance.*
import com.example.atomic_cinema.stateClasses.SeanceState
import com.example.atomic_cinema.utils.Support
import com.himanshoe.kalendar.model.KalendarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SeanceViewModel @Inject constructor(
    private val repository: SeanceRepository
) : ViewModel(){

    private val resultChannel = Channel<SeanceResult<Unit>>()
    val seanceResults = resultChannel.receiveAsFlow()

    var state by mutableStateOf(SeanceState())

    val copySeanceUpdate by mutableStateOf(Support.copySeanceUpdate)
    var listSeance : MutableList<SeanceState> = mutableListOf()

    fun onEvent(event : SeanceUIEvent){
        when(event){
            is SeanceUIEvent.TimeEndChanged -> {
                state = state.copy(timeEnd = event.value)
            }
            is SeanceUIEvent.TimeStartChanged -> {
                state = state.copy(timeStart = event.value)
            }
            is SeanceUIEvent.DateEndChanged -> {
                state = state.copy(dateEnd = event.value)
            }
            is SeanceUIEvent.DateStartChanged -> {
                state = state.copy(dateStart = event.value)
            }
            is SeanceUIEvent.PriceChanged -> {
                state = state.copy(price = event.value)
            }
            is SeanceUIEvent.SelectedDateChanged -> {
                state = state.copy(selectedDateSeance = event.value)
            }
            is SeanceUIEvent.PlaceHolderChanged -> {
                state = state.copy(placeHolderIsOpen = event.value)
            }
            is SeanceUIEvent.BoxEmptyISVisible ->{
                state = state.copy(emptyBoxIsVisible = event.value)
            }
            is SeanceUIEvent.TypeHallChanged -> {
                state = state.copy(typeHall = event.value)
            }
            is SeanceUIEvent.AddressCinemaChanged -> {
                state = state.copy(addressCinema = event.value)
            }
            is SeanceUIEvent.ShowSeance -> {
                getSeanceMovie(event.value)
            }
            is SeanceUIEvent.IdHallChanged -> {
                state = state.copy(idHall = event.value)
            }
            is SeanceUIEvent.IdMovieChanged ->{
                state = state.copy(idMovie = event.value)
            }
            is SeanceUIEvent.ShowDetailSeance -> {
                state = state.copy(showDetailsSeance = event.value)
            }
            is SeanceUIEvent.DeleteSeance -> {
                deleteSeance(event.value)
            }
            is SeanceUIEvent.SeanceChanged -> {
                state = event.value
            }
            is SeanceUIEvent.GetSeancesCinema -> {
                getSeancesCinema(event.value)
            }
            SeanceUIEvent.UpdateSeance -> {
                updateSeance()
            }
            SeanceUIEvent.AddSeance -> {
                addSeance()
            }

        }
    }

    private fun addCalendarEvents(){
        listSeance.forEach {
            var dateStart = it.dateStart
            while(dateStart!! <= it.dateEnd){
                state.calendarEvents.add(KalendarEvent(dateStart.toKotlinLocalDate(), "Seance"))
                dateStart = dateStart!!.plusDays(1)
            }
        }
        onEvent(SeanceUIEvent.SelectedDateChanged(state.calendarEvents.last().date.toJavaLocalDate()))
    }

    private fun getSeanceMovie(idMovie : Int){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            if(listSeance.isNotEmpty()){
                listSeance.clear()
                state.calendarEvents.clear()
            }

            val formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val formatterTime = DateTimeFormatter.ofPattern("HH:mm")

            val result = repository.getSeanceMovie(idMovie)

            if(result.data != null){

                result.data.forEach { seance ->
                    listSeance.add(
                        SeanceState(
                            id = seance.id,
                            idMovie = seance.idMovie,
                            idHall = seance.idHall,
                            dateStart = LocalDate.parse(seance.dateStart, formatterDate),
                            dateEnd = LocalDate.parse(seance.dateEnd, formatterDate),
                            timeStart = LocalTime.parse(seance.timeStart, formatterTime),
                            timeEnd = LocalTime.parse(seance.timeEnd, formatterTime),
                            price = seance.price,
                            typeHall = seance.nameTypeHall,
                            addressCinema = seance.addressCinema
                        )
                    )
                }

                addCalendarEvents()

                resultChannel.send(SeanceResult.OK())
            }else{
                resultChannel.send(SeanceResult.UnknownError())
            }

            state = state.copy(isLoading = false)
        }
    }

    private fun getSeancesCinema(addressCinema: String){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            if(listSeance.isNotEmpty()){
                listSeance.clear()
                state.calendarEvents.clear()
            }

            val request = SeanceCinemaRequest(addressCinema = addressCinema)
            val formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val formatterTime = DateTimeFormatter.ofPattern("HH:mm")

            val result = repository.getSeanceCinema(request)

            if(result.data != null){
                result.data.forEach { seance ->
                    listSeance.add(
                        SeanceState(
                            id = seance.id,
                            idMovie = seance.idMovie,
                            idHall = seance.idHall,
                            dateStart = LocalDate.parse(seance.dateStart, formatterDate),
                            dateEnd = LocalDate.parse(seance.dateEnd, formatterDate),
                            timeStart = LocalTime.parse(seance.timeStart, formatterTime),
                            timeEnd = LocalTime.parse(seance.timeEnd, formatterTime),
                            price = seance.price,
                            typeHall = seance.nameTypeHall,
                            addressCinema = seance.addressCinema
                        )
                    )
                }
                resultChannel.send(SeanceResult.OK())
            }else{
                resultChannel.send(SeanceResult.NotFoundSeances())
            }

            state = state.copy(isLoading = false)
        }
    }


    private fun addSeance(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val request = SeanceAddRequest(
                idMovie = state.idMovie,
                idHall = state.idHall,
                price = state.price.toString(),
                timeEnd = state.timeEnd!!,
                timeStart = state.timeStart!!,
                dateEnd = state.dateEnd!!,
                dateStart = state.dateStart!!
            )

            val result = repository.addSeance(request)

            resultChannel.send(result)

            state = state.copy(isLoading = false)
        }
    }

    private fun updateSeance(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val request = SeanceUpdateRequest(
                id = state.id,
                idMovie = state.idMovie,
                idHall = state.idHall,
                price = state.price.toString(),
                timeEnd = state.timeEnd!!,
                timeStart = state.timeStart!!,
                dateEnd = state.dateEnd!!,
                dateStart = state.dateStart!!
            )

            val result = repository.updateSeance(request)

            resultChannel.send(result)

            state = state.copy(isLoading = false)
        }
    }

    private fun deleteSeance(value: Int) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val request = SeanceDeleteRequest(value)

            val result = repository.deleteSeance(request)

            resultChannel.send(result)

            state = state.copy(isLoading = false)
        }
    }
}