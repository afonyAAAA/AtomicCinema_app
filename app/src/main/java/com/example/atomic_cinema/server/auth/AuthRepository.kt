package com.example.atomic_cinema.server.auth

import java.time.LocalDate

interface AuthRepository{
    suspend fun signUp(
        login: String,
        password: String,
        firstName: String,
        name: String,
        lastName: String,
        numberPhone: String,
        dateOfBirth: LocalDate
    ): AuthResult<Unit>

    suspend fun signIn(login: String, password: String): AuthResult<Unit>

    suspend fun authenticate(): AuthResult<SecretInfoResponse>

    suspend fun goOut(): AuthResult<Unit>
}