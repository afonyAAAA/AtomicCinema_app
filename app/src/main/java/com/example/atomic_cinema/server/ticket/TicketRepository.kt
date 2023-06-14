package com.example.atomic_cinema.server.ticket

interface TicketRepository {

   suspend fun addTicket(ticketRequest: AddTicketRequest) : TicketResults<Unit>

   suspend fun getTicketsUser() : TicketResults<List<TicketsUsersResponse>>

   suspend fun getTicketsUser(ticketRequest: TicketsUserRequest) : TicketResults<List<TicketsUsersResponse>>

   suspend fun updateTicket(ticketRequest: UpdateTicketRequest) : TicketResults<Unit>

   suspend fun checkTickets() : TicketResults<Unit>

}