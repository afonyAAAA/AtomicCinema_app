package com.example.atomic_cinema.server.ticket


import android.annotation.SuppressLint
import com.google.gson.TypeAdapter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AddTicketRequest(
    val count : Int,
    val dateTime : LocalDateTime,
    val idUser : Int,
    val idStatusPayment : Int,
    val idSeance : Int,
    val returned : Boolean,
    val dateStartSeance : LocalDate
)


data class TicketsUserRequest(
    val login : String
)


data class UpdateTicketRequest(
    val idTicket : Int,
    val idStatusPayment : Int = 0,
    val returned: Boolean? = null
)

@SuppressLint("NewApi")
class LocalDateTimeTypeAdapter : TypeAdapter<LocalDateTime>() {

    override fun write(out: com.google.gson.stream.JsonWriter?, value: LocalDateTime?) {
        out?.value(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value))
    }

    override fun read(input: com.google.gson.stream.JsonReader?): LocalDateTime =  LocalDateTime.parse(input!!.nextString())

}


