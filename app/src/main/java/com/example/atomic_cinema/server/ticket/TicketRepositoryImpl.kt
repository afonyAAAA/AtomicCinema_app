package com.example.atomic_cinema.server.ticket

import android.content.SharedPreferences
import retrofit2.HttpException

class TicketRepositoryImpl(
    private val api : TicketApi,
    private val prefs : SharedPreferences) : TicketRepository {

    override suspend fun addTicket(ticket : AddTicketRequest): TicketResults<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return TicketResults.Unauthorized()
            api.addTicket(
                "Bearer $token",
                ticket
            )
            TicketResults.OK()
        }catch (e : HttpException){
            if(e.code() == 401){
                TicketResults.Unauthorized()
            }else{
                e.printStackTrace()
                TicketResults.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            TicketResults.UnknownError()
        }
    }

    override suspend fun getTicketsUser(): TicketResults<List<TicketsUsersResponse>> {
        return try{
            val token = prefs.getString("jwt", null) ?: return TicketResults.Unauthorized()
            val result = api.getTicketsUser("Bearer $token")
            TicketResults.OK(result)
        }catch (e : HttpException){
            if(e.code() == 401){
                TicketResults.Unauthorized()
            }else if (e.code() == 404){
                TicketResults.NotFoundTickets()
            }
            else {
                TicketResults.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            TicketResults.UnknownError()
        }
    }

    override suspend fun getTicketsUser(ticketRequest: TicketsUserRequest): TicketResults<List<TicketsUsersResponse>> {
        return try{
            val token = prefs.getString("jwt", null) ?: return TicketResults.Unauthorized()
            val result = api.getTicketsUser(
                "Bearer $token",
                ticketRequest
            )
            TicketResults.OK(result)
        }catch (e : HttpException){
            if(e.code() == 401){
                TicketResults.Unauthorized()
            }else if (e.code() == 404){
                TicketResults.NotFoundTickets()
            }
            else {
                TicketResults.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            TicketResults.UnknownError()
        }
    }

    override suspend fun updateTicket(ticketRequest: UpdateTicketRequest): TicketResults<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return TicketResults.Unauthorized()
            api.updateTicket(
                "Bearer $token",
                ticketRequest
            )
            TicketResults.OK()
        }catch (e : HttpException){
            if(e.code() == 401){
                TicketResults.Unauthorized()
            }else{
                e.printStackTrace()
                TicketResults.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            TicketResults.UnknownError()
        }
    }

    override suspend fun checkTickets(): TicketResults<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return TicketResults.Unauthorized()
            api.checkTickets("Bearer $token", )
            TicketResults.OK()
        }catch (e : HttpException){
            if(e.code() == 401){
                TicketResults.Unauthorized()
            }else if (e.code() == 404){
                TicketResults.NotFoundTickets()
            }
            else {
                e.printStackTrace()
                TicketResults.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            TicketResults.UnknownError()
        }
    }

}