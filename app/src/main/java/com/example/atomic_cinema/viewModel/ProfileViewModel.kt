package com.example.atomic_cinema.viewModel

import android.os.Build
import android.util.Log
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
        authenticate{
            showProfile()
        }
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
            is ProfileUIEvent.EditProfile ->{
                editProfile()
            }
            is ProfileUIEvent.EditMode ->{
                state = state.copy(editMode = event.value)
                copyState = state
            }
            is ProfileUIEvent.DeclineEdit -> {
                state = copyState
                state = state.copy(editMode = event.value)
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

                resultChannel.send(ProfileResult.Shown())
            }else{
                resultChannel.send(ProfileResult.UnknownError())
            }

            state = state.copy(isLoading = false)

        }
    }
    private fun authenticate(content : () -> Unit){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = repository.authenticate()


            if(result.data != null){
                Log.d("МИША ВСЕ ХУЙНЯ", "ХУЙ")
                onEvent(ProfileUIEvent.Role(result.data.role))
                resultChannel.send(ProfileResult.Authorized())
                content()
            }

            state = state.copy(isLoading = false)
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