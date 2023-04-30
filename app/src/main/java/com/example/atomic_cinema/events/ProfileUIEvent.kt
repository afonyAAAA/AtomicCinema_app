package com.example.atomic_cinema.events

sealed class ProfileUIEvent {
    data class LoginChanged(val value: String) : ProfileUIEvent()
    data class FirstNameChanged(val value: String) : ProfileUIEvent()
    data class NameChanged(val value: String) : ProfileUIEvent()
    data class LastNameChanged(val value: String) : ProfileUIEvent()
    data class NumberPhoneChanged(val value: String) : ProfileUIEvent()
    data class DateOfBirthChanged(val value: String) : ProfileUIEvent()
    data class Role(val value : String) : ProfileUIEvent()
    data class EditMode(val value : Boolean) : ProfileUIEvent()
    data class DeclineEdit (val value : Boolean) : ProfileUIEvent()
    object EditProfile : ProfileUIEvent()

}
