package com.example.atomic_cinema.server.profile

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ProfileApi {

    @GET("profile")
    suspend fun getProfileInfo(
        @Header("Authorization") token : String,
    ) : ProfileResponse

    @GET("getPaymentsForMonth")
    suspend fun getProfileInfoPaymentsForMonth(
        @Header("Authorization") token : String,
    ) : ProfileResponsePaymentsInfo

    @POST("profile/edit")
    suspend fun editProfile(
        @Header("Authorization") token : String,
        @Body request: ProfileEditRequest
    )

    @POST("/updateBalance")
    suspend fun updateBalance(
        @Header("Authorization") token : String,
        @Body balance: ProfileBalanceRequest
    )

}