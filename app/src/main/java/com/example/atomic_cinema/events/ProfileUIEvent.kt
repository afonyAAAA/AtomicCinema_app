package com.example.atomic_cinema.events

sealed class ProfileUIEvent {
    data class LoginChanged(val value: String) : ProfileUIEvent()
    data class FirstNameChanged(val value: String) : ProfileUIEvent()
    data class NameChanged(val value: String) : ProfileUIEvent()
    data class LastNameChanged(val value: String) : ProfileUIEvent()
    data class NumberPhoneChanged(val value: String) : ProfileUIEvent()
    data class DateOfBirthChanged(val value: String) : ProfileUIEvent()
    data class BalanceChanged(val value: String) : ProfileUIEvent()
    data class Role(val value : String) : ProfileUIEvent()
    data class EditMode(val value : Boolean) : ProfileUIEvent()
    data class DeclineEdit (val value : Boolean) : ProfileUIEvent()
    data class ViewBalance(val value: Boolean) : ProfileUIEvent()
    data class LoadingMoneyOperation(val value: Boolean) : ProfileUIEvent()
    data class ReplenishBalanceChanged(val value: Boolean) : ProfileUIEvent()
    data class SumReplenish(val value: String) : ProfileUIEvent()
    data class MoneyOperationIsSuccessful(val value: Boolean) : ProfileUIEvent()
    data class ConfirmReplenish(val value: Double) : ProfileUIEvent()
    data class ConfirmSubtract(val value: Double) : ProfileUIEvent()
    object ShowProfile : ProfileUIEvent()
    object EditProfile : ProfileUIEvent()
    object GenerateSpendingReport: ProfileUIEvent()

}
