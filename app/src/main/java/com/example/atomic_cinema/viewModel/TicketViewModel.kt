package com.example.atomic_cinema.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic_cinema.events.TicketUIEvent
import com.example.atomic_cinema.server.ticket.*
import com.example.atomic_cinema.stateClasses.TicketState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val repository : TicketRepository
) : ViewModel() {

    private val resultChannel = Channel<TicketResults<Unit>>()
    val ticketResults = resultChannel.receiveAsFlow()

    var state by mutableStateOf(TicketState())

    var listStateTicket : MutableList<TicketState> = mutableListOf()

    fun onEvent(event : TicketUIEvent){
        state = when(event){
            is TicketUIEvent.CountChanged -> {
                state.copy(count = event.value)
            }
            is TicketUIEvent.ExpandedTextFieldChanged -> {
                state.copy(expandedNumberTextField = event.value)
            }
            is TicketUIEvent.StatusPaymentChanged -> {
                state.copy(idStatusPayment = event.value)
            }
            is TicketUIEvent.SumPay -> {
                state.copy(sumPay = event.value)
            }
            is TicketUIEvent.ConfirmTicket -> {
                addTicket(
                    AddTicketRequest(
                        count = state.count,
                        dateTime = LocalDateTime.now(),
                        idUser = state.idUser,
                        idStatusPayment = 3,
                        idSeance = event.value.id,
                        returned = false,
                        dateStartSeance = event.value.selectedDateSeance
                    )
                )
                state
            }
            is TicketUIEvent.PayChanged -> {
                state.copy(showPayAlertDialog = event.value)
            }
            is TicketUIEvent.IdUserChanged -> {
                state.copy(idUser = event.value)
            }
            is TicketUIEvent.IdSeanceChanged -> {
                state.copy(idSeance = event.value)
            }
            is TicketUIEvent.LoadingPay -> {
                state.copy(loadingPay = event.value)
            }
            TicketUIEvent.GetTickets -> {
                getTicketsUser()
                state
            }
            is TicketUIEvent.DetailsScreenChanged -> {

                state = state.copy(isLoading = true)

                state = listStateTicket.find { it.idTicket == event.idTicket }!!

                state = state.copy(isLoading = false)

                state.copy(detailsTicketChanged = event.state)

            }
            TicketUIEvent.ReturnTicket -> {
                returnTicket()
                state
            }
            is TicketUIEvent.ConfirmTicketWithoutPay -> {
                addTicket(
                    AddTicketRequest(
                        count = state.count,
                        dateTime = LocalDateTime.now(),
                        idUser = state.idUser,
                        idStatusPayment = 2,
                        idSeance = event.value.id,
                        returned = false,
                        dateStartSeance = event.value.selectedDateSeance
                    )
                )
                state
            }
            is TicketUIEvent.GetTicketsByLogin -> {
                checkPresentsTicketUser(event.value)
                state
            }
            is TicketUIEvent.PayingTicketAwaitingPayment -> {
                updateTicket(UpdateTicketRequest(
                    idTicket = state.idTicket,
                    idStatusPayment = 3
                ))
                state
            }
            TicketUIEvent.ClearTicketsUser -> {

                state = state.copy(isLoading = true)

                listStateTicket.clear()

                state = state.copy(isLoading = false)

                state
            }
            TicketUIEvent.CheckTicketsOnSeances -> {
                checkTicketsOnSeances()
                state
            }
        }
    }

    private fun checkPresentsTicketUser(loginUser : String){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val request = TicketsUserRequest(login = loginUser)

            if(listStateTicket.isNotEmpty()){
                listStateTicket.clear()
            }

            val result = repository.getTicketsUser(request)

            if(result.data != null){
                result.data.forEach { ticket ->
                    listStateTicket.add(TicketState(idSeance = ticket.idSeance))
                }
            }else{
                resultChannel.send(TicketResults.NotFoundTickets())
            }

            state = state.copy(isLoading = false)
        }
    }

    private fun returnTicket(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            updateTicket(UpdateTicketRequest(
                idTicket = state.idTicket,
                returned = true
            ))

            getTicketsUser()

            state = state.copy(isLoading = true, detailsTicketChanged = false)
        }
    }

    private fun checkTicketsOnSeances(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = repository.checkTickets()

            if(result is TicketResults.OK){
                 resultChannel.send(TicketResults.NotifyCustomer())
            }

            state = state.copy(isLoading = false)
        }
    }

    private fun addTicket(ticket : AddTicketRequest){
        viewModelScope.launch {

            state = state.copy(isLoading = true)

            val result = repository.addTicket(ticket)

            resultChannel.send(result)

            state = state.copy(isLoading = false)

        }
    }

    private fun updateTicket(ticket: UpdateTicketRequest){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = repository.updateTicket(ticket)

            resultChannel.send(result)

            state = state.copy(isLoading = false)
        }
    }

    private fun getTicketsUser(){
        viewModelScope.launch {

            state = state.copy(isLoading = true)

            if(listStateTicket.isNotEmpty()){
                listStateTicket.clear()
            }

            val result = repository.getTicketsUser()

            if(result.data != null){

                val formatterDateTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                val formatterTime = DateTimeFormatter.ofPattern("HH:mm")

                result.data.forEach { ticket ->
                    listStateTicket.add(
                        TicketState(
                            idUser = ticket.idUser,
                            idTicket = ticket.id,
                            returned = ticket.returned,
                            nameStatus = ticket.nameStatus,
                            nameMovie = ticket.nameMovie,
                            nameTypeHall = ticket.nameTypeHall,
                            count = ticket.count,
                            linkImage = ticket.linkImage,
                            addressCinema = ticket.addressCinema,
                            ageRating = ticket.ageRating,
                            timeEnd = LocalTime.parse(ticket.timeEnd, formatterTime).toString(),
                            timeStart = LocalTime.parse(ticket.timeStart, formatterTime).toString(),
                            duration = ticket.duration,
                            price = ticket.price,
                            dateTime = LocalDateTime.parse(ticket.dateTime, formatterDateTime),
                            dateStartSeance = ticket.dateStartSeance
                        )
                    )
                }
                listStateTicket = listStateTicket.sortedByDescending { it.dateTime }.toMutableList()
            }else{
                resultChannel.send(TicketResults.UnknownError())
            }

            resultChannel.send(TicketResults.OK())

            state = state.copy(isLoading = false)

        }
    }
}