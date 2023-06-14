package com.example.atomic_cinema.server.profile

import java.time.LocalDate

interface ProfileRepository{

    suspend fun getProfileInfo() : ProfileResult<ProfileResponse>

    suspend fun getProfileInfoPaymentsForMonth() : ProfileResult<ProfileResponsePaymentsInfo>

    suspend fun updateBalance(balance : String) : ProfileResult<Unit>

    suspend fun editProfile(
        firstName : String,
        name : String,
        lastName : String,
        numberPhone : String,
        dateOfBirth : LocalDate
    ) : ProfileResult<Unit>



}