package com.example.atomic_cinema.server.profile

import java.time.LocalDate

data class ProfileEditRequest(
    val firstName: String,
    val name: String,
    val lastName: String,
    val numberPhone: String,
    val dateOfBirth: LocalDate
)
