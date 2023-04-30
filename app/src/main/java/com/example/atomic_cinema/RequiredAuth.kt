package com.example.atomic_cinema

import com.example.atomic_cinema.server.auth.SecretInfoResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface RequiredAuth {
    @GET("authenticate")
    suspend fun authenticate(
        @Header("Authorization") token : String
    ) : SecretInfoResponse
}