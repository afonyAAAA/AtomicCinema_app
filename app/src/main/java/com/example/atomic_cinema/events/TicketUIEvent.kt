package com.example.atomic_cinema.events

import com.example.atomic_cinema.stateClasses.SeanceState

sealed class TicketUIEvent {
    data class CountChanged(val value: Int) : TicketUIEvent()
    data class StatusPaymentChanged(val value: Int) : TicketUIEvent()
    data class ExpandedTextFieldChanged(val value: Boolean) : TicketUIEvent()
    data class SumPay(val value: String) : TicketUIEvent()
    data class PayChanged(val value: Boolean) : TicketUIEvent()
    data class IdSeanceChanged(val value: Int) : TicketUIEvent()
    data class IdUserChanged(val value: Int) : TicketUIEvent()
    data class ConfirmTicket(val value: SeanceState) : TicketUIEvent()
    data class ConfirmTicketWithoutPay(val value: SeanceState) : TicketUIEvent()
    data class DetailsScreenChanged(val state: Boolean, val idTicket : Int) : TicketUIEvent()
    data class LoadingPay(val value: Boolean) : TicketUIEvent()
    data class GetTicketsByLogin(val value: String) : TicketUIEvent()
    object ClearTicketsUser : TicketUIEvent()
    object CheckTicketsOnSeances : TicketUIEvent()
    object PayingTicketAwaitingPayment : TicketUIEvent()
    object GetTickets : TicketUIEvent()
    object ReturnTicket : TicketUIEvent()
}