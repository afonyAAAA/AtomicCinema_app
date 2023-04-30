package com.example.atomic_cinema.stateClasses

data class ProfileState(
    val isLoading: Boolean = false,
    val login: String = "",
    val firstNameChanged : String = "",
    val nameChanged : String = "",
    val lastNameChanged : String = "",
    val numberPhoneChanged : String = "",
    val dateOfBirthChanged : String = "",
    val editMode : Boolean = false,
    val role : String = ""
)
