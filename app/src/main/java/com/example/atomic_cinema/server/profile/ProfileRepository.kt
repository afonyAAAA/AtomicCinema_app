package com.example.atomic_cinema.server.profile

import com.example.atomic_cinema.RequiredAuth
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.server.auth.SecretInfoResponse
import java.time.LocalDate

interface ProfileRepository{

    suspend fun getProfileInfo() : ProfileResult<ProfileResponse>

    suspend fun editProfile(
        firstName : String,
        name : String,
        lastName : String,
        numberPhone : String,
        dateOfBirth : LocalDate
    ) : ProfileResult<Unit>

    suspend fun authenticate() : ProfileResult<SecretInfoResponse>

}