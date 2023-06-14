package com.example.atomic_cinema.server.auth

import android.annotation.SuppressLint
import com.fasterxml.jackson.annotation.JsonFormat
import com.google.gson.TypeAdapter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


data class AuthSignUpRequest(
    val login: String,
    val password: String,
    val firstName: String,
    val name: String,
    val lastName: String,
    val numberPhone: String,
    val dateBirth: LocalDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val idRole: Int = 1,
)

@SuppressLint("NewApi")
class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {

    override fun write(out: com.google.gson.stream.JsonWriter?, value: LocalDate?) {
        out?.value(DateTimeFormatter.ISO_LOCAL_DATE.format(value))
    }

    override fun read(input: com.google.gson.stream.JsonReader?): LocalDate =  LocalDate.parse(input!!.nextString())

}

@SuppressLint("NewApi")
class LocalTimeTypeAdapter : TypeAdapter<LocalTime>() {

    override fun write(out: com.google.gson.stream.JsonWriter?, value: LocalTime?) {
        val time = value?.format(DateTimeFormatter.ofPattern("HH:mm"))
        out?.value(time)
    }

    override fun read(input: com.google.gson.stream.JsonReader?): LocalTime =  LocalTime.parse(input!!.nextString())

}

