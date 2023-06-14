package com.example.atomic_cinema.server.ticket

import com.example.atomic_cinema.RequiredAuth
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TicketApi : RequiredAuth{
    @POST("createTicket")
    suspend fun addTicket(
        @Header("Authorization") token : String,
        @Body ticketRequest: AddTicketRequest
    )
    @GET("ticketUser")
    suspend fun getTicketsUser(
        @Header("Authorization") token: String
    ) : List<TicketsUsersResponse>

    @POST("checkPresentsTickets")
    suspend fun getTicketsUser(
        @Header("Authorization") token: String,
        @Body request: TicketsUserRequest
    ) : List<TicketsUsersResponse>

    @GET("checkTicketsOnSeances")
    suspend fun checkTickets(
        @Header("Authorization") token: String,
    )

    @POST("updateTicket")
    suspend fun updateTicket(
        @Header("Authorization") token : String,
        @Body ticketRequest: UpdateTicketRequest
    )

}