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
    val role : String = "",
    val balance : String = "",
    val viewBalance : Boolean = false,
    val loadingMoneyOperation : Boolean = false,
    val replenishBalanceMode : Boolean = false,
    val sumReplenish : String = "",
    val moneyOperationIsSuccessful : Boolean = false,
    val sumSpending : Double = 0.0,
    val countTicketSpending : Int = 0
)
