package com.example.atomic_cinema.server.ticket

sealed class TicketResults <T>(val data : T? = null){
    class OK <T>(data : T? = null) : TicketResults<T>(data)
    class UnknownError <T>(data : T? = null) : TicketResults<T>(data)
    class NotFoundTickets <T>(data : T? = null) : TicketResults<T>(data)
    class InsufficientFunds <T>(data : T? = null) : TicketResults<T>(data)
    class Unauthorized <T>(data : T? = null) : TicketResults<T>(data)
    class NotifyCustomer <T>(data : T? = null) : TicketResults<T>(data)
}