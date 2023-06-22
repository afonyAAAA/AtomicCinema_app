package com.example.atomic_cinema.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic_cinema.events.ProfileUIEvent
import com.example.atomic_cinema.server.profile.ProfileRepository
import com.example.atomic_cinema.server.profile.ProfileResult
import com.example.atomic_cinema.stateClasses.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
    var copyState : ProfileState = state

    private val resultChannel = Channel<ProfileResult<Unit>>()
    val profileResults = resultChannel.receiveAsFlow()

    init {
        showProfile()
    }

    fun onEvent(event : ProfileUIEvent){
        when(event){
            is ProfileUIEvent.LoginChanged -> {
                state = state.copy(login = event.value)
            }
            is ProfileUIEvent.FirstNameChanged -> {
                state = state.copy(firstNameChanged = event.value)
            }
            is ProfileUIEvent.NameChanged -> {
                state = state.copy(nameChanged = event.value)
            }
            is ProfileUIEvent.LastNameChanged -> {
                state = state.copy(lastNameChanged = event.value)
            }
            is ProfileUIEvent.NumberPhoneChanged -> {
                state = state.copy(numberPhoneChanged = event.value)
            }
            is ProfileUIEvent.DateOfBirthChanged -> {
                state = state.copy(dateOfBirthChanged = event.value)
            }
            is ProfileUIEvent.Role -> {
                state = state.copy(role = event.value)
            }
            is ProfileUIEvent.EditMode ->{
                state = state.copy(editMode = event.value)
                copyState = state
            }
            is ProfileUIEvent.DeclineEdit -> {
                state = copyState
                state = state.copy(editMode = event.value)
            }
            is ProfileUIEvent.LoadingMoneyOperation -> {
                state = state.copy(loadingMoneyOperation = event.value)
            }
            is ProfileUIEvent.ReplenishBalanceChanged -> {
                state = state.copy(replenishBalanceMode = event.value)
            }
            is ProfileUIEvent.SumReplenish -> {
                state = state.copy(sumReplenish = event.value)
            }
            is ProfileUIEvent.ViewBalance ->{
                state = state.copy(viewBalance = event.value)
            }
            is ProfileUIEvent.ConfirmReplenish -> {
                replenishBalance(event.value, state.balance.toDouble())
            }
            is ProfileUIEvent.MoneyOperationIsSuccessful ->{
                state = state.copy(moneyOperationIsSuccessful = event.value)
            }
            is ProfileUIEvent.BalanceChanged -> {
                state = state.copy(balance = event.value)
            }
            is ProfileUIEvent.ConfirmSubtract -> {
                subtractBalance(event.value, state.balance.toDouble())
            }
            ProfileUIEvent.GenerateSpendingReport -> {
                generateSpendingReport()
            }
            ProfileUIEvent.ShowProfile -> {
                showProfile()
            }
            ProfileUIEvent.EditProfile ->{
                editProfile()
            }
        }
    }

    private fun showProfile(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = repository.getProfileInfo()

            if(result.data != null){
                onEvent(ProfileUIEvent.LoginChanged(result.data.login))
                onEvent(ProfileUIEvent.FirstNameChanged(result.data.firstName))
                onEvent(ProfileUIEvent.NameChanged(result.data.name))
                onEvent(ProfileUIEvent.LastNameChanged(result.data.lastName))
                onEvent(ProfileUIEvent.NumberPhoneChanged(result.data.numberPhone))
                onEvent(ProfileUIEvent.DateOfBirthChanged(result.data.dateOfBirth.replace(".", "")))
                onEvent(ProfileUIEvent.BalanceChanged(result.data.balance))

                resultChannel.send(ProfileResult.Shown())
            }else{
                resultChannel.send(ProfileResult.UnknownError())
            }

            state = state.copy(isLoading = false)

        }
    }

    private fun replenishBalance(sumReplenish : Double, balance : Double){
        viewModelScope.launch {

            if(sumReplenish <= 100.0){
                resultChannel.send(ProfileResult.FewReplenish())
            }else{
                onEvent(ProfileUIEvent.LoadingMoneyOperation(true))

                state = state.copy(isLoading = true)

                val result = repository.updateBalance((balance + sumReplenish).toString())

                resultChannel.send(result)

                state = state.copy(isLoading = false)
            }
        }
    }

    private fun generateSpendingReport(){
        viewModelScope.launch {

            state = state.copy(isLoading = true)

            val result = repository.getProfileInfoPaymentsForMonth()

            if(result.data != null){
                with(result.data){
                    state = state.copy(
                        sumSpending = sumPay,
                        countTicketSpending = countTickets
                    )
                }
            }else{
                resultChannel.send(ProfileResult.NotFoundTicketsForMonth())
            }

            state = state.copy(isLoading = false)
        }
    }

    private fun subtractBalance(sumPay : Double, balance : Double){
        viewModelScope.launch {

            if(sumPay > balance){
                resultChannel.send(ProfileResult.InsufficientFunds())
            }else{
                onEvent(ProfileUIEvent.LoadingMoneyOperation(true))

                state = state.copy(isLoading =  true)

                val result = repository.updateBalance((balance - sumPay).toString())

                resultChannel.send(result)

                state = state.copy(isLoading =  false)
            }
        }
    }




    private fun editProfile(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val formattedDate = LocalDate.of(
                state.dateOfBirthChanged.substring(4..7).toInt(),
                if(state.dateOfBirthChanged[2].code != 0){
                    state.dateOfBirthChanged.substring(2..3).toInt()
                }else{
                    state.dateOfBirthChanged[3].code
                },
                if(state.dateOfBirthChanged[0].code != 0){
                    state.dateOfBirthChanged.substring(0..1).toInt()
                }else{
                    state.dateOfBirthChanged[1].code
                }
            )

            val result = repository.editProfile(
                firstName = state.firstNameChanged,
                name = state.nameChanged,
                lastName = state.lastNameChanged,
                numberPhone = state.numberPhoneChanged,
                dateOfBirth = formattedDate
            )

            resultChannel.send(result)

            showProfile()

            state = state.copy(isLoading = false)
        }
    }


}