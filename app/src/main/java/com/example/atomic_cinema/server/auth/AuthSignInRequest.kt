package com.example.atomic_cinema.server.auth

data class AuthSignInRequest(
    val login : String,
    val password : String
)
