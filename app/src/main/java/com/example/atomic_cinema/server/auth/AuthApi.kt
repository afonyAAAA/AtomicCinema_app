package com.example.atomic_cinema.server.auth

import com.example.atomic_cinema.RequiredAuth
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi : RequiredAuth {

    @POST("register")
    suspend fun signUp(
        @Body request: AuthSignUpRequest
    )
    @POST("login")
    suspend fun signIn(
        @Body request: AuthSignInRequest
    ) : TokenResponse

    @POST("goOut")
    suspend fun goOut(
        @Header("Authorization") token : String
    )
    @GET("authenticate")
    override suspend fun authenticate(
        @Header("Authorization") token : String
    ) : SecretInfoResponse
}
