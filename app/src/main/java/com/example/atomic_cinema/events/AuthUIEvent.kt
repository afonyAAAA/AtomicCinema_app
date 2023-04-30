package com.example.atomic_cinema.events

sealed class AuthUIEvent{
    data class SignInLoginChanged(val value: String) : AuthUIEvent()
    data class SignInPasswordChanged(val value: String) : AuthUIEvent()
    data class SignUpLoginChanged(val value : String) : AuthUIEvent()
    data class SignUpPasswordChanged(val value: String) : AuthUIEvent()
    data class SignUpFirstNameChanged(val value: String) : AuthUIEvent()
    data class SignUpNameChanged(val value : String) : AuthUIEvent()
    data class SignUpLastNameChanged(val value : String) : AuthUIEvent()
    data class SignUpNumberPhoneChanged(val value: String) : AuthUIEvent()
    data class SignUpDateOfBirthChanged(val value: String) : AuthUIEvent()
    data class Role(val value : String) : AuthUIEvent()
    data class Authorized(val value: Boolean) : AuthUIEvent()
    object Authenticate : AuthUIEvent()
    object SignUp : AuthUIEvent()
    object GoOut : AuthUIEvent()
    object SignIn : AuthUIEvent()
}
