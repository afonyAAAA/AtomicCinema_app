package com.example.atomic_cinema.server.profile

import com.example.atomic_cinema.RequiredAuth
import com.example.atomic_cinema.server.auth.SecretInfoResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ProfileApi : RequiredAuth {

    @GET("profile")
    suspend fun getProfileInfo(
        @Header("Authorization") token : String,
    ) : ProfileResponse

    @POST("profile/edit")
    suspend fun editProfile(
        @Header("Authorization") token : String,
        @Body request: ProfileEditRequest
    )

    @GET("authenticate")
    override suspend fun authenticate(
        @Header("Authorization") token : String
    ) : SecretInfoResponse
}