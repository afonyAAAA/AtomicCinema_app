package com.example.atomic_cinema.server.profile

data class ProfileResponse(
    val login: String,
    val dateOfBirth: String,
    val firstName: String,
    val name: String,
    val lastName: String,
    val numberPhone: String,
)
