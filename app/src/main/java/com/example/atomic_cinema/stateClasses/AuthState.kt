package com.example.atomic_cinema.stateClasses

import android.annotation.SuppressLint
import java.time.LocalDate

@SuppressLint("NewApi")
data class AuthState(
    val isLoading: Boolean = false,
    val authorized : Boolean = false,
    val signInLoginChanged: String = "",
    val signInPasswordChanged : String = "",
    val signUpLoginChanged : String = "",
    val signUpPasswordChanged : String = "",
    val signUpFirstNameChanged : String = "",
    val signUpNameChanged : String = "",
    val signUpLastNameChanged : String = "",
    val signUpNumberPhoneChanged : String = "",
    val signUpDateOfBirthChanged : String = "",
    val userID : Int = 0,
    val role : String = ""
)
