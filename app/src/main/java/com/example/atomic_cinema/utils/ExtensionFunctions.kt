package com.example.atomic_cinema.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.toMyDateFormat(date : LocalDate) : String{
    return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date)
}

