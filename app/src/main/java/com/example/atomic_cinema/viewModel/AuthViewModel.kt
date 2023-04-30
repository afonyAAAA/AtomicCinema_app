package com.example.atomic_cinema.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic_cinema.events.AuthUIEvent
import com.example.atomic_cinema.server.auth.AuthRepository
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.stateClasses.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf(AuthState())

    private val resultChannel = Channel<AuthResult<Unit>>()
    val authResults = resultChannel.receiveAsFlow()

    init {
        authenticate()
    }

    fun onEvent(event : AuthUIEvent){
        when(event){
            is AuthUIEvent.SignInLoginChanged -> {
                state = state.copy(signInLoginChanged = event.value)
            }
            is AuthUIEvent.SignInPasswordChanged -> {
                state = state.copy(signInPasswordChanged = event.value)
            }
            is AuthUIEvent.SignUpLoginChanged -> {
                state = state.copy(signUpLoginChanged = event.value)
            }
            is AuthUIEvent.SignUpPasswordChanged -> {
                state = state.copy(signUpPasswordChanged = event.value)
            }
            is AuthUIEvent.SignUpDateOfBirthChanged ->{
                state = state.copy(signUpDateOfBirthChanged = event.value)
            }
            is AuthUIEvent.SignUpFirstNameChanged -> {
                state = state.copy(signUpFirstNameChanged = event.value)
            }
            is AuthUIEvent.SignUpNameChanged -> {
                state = state.copy(signUpNameChanged = event.value)
            }
            is AuthUIEvent.SignUpLastNameChanged ->{
                state = state.copy(signUpLastNameChanged = event.value)
            }
            is AuthUIEvent.SignUpNumberPhoneChanged ->{
                state = state.copy(signUpNumberPhoneChanged = event.value)
            }
            is AuthUIEvent.Role -> {
                state = state.copy(role = event.value)
            }
            is AuthUIEvent.Authorized ->{

                state = state.copy(authorized = event.value)

                viewModelScope.launch {
                    while(!state.authorized){
                        onEvent(AuthUIEvent.Authenticate)
                        delay(2000L)
                    }
                }

            }
            is AuthUIEvent.Authenticate -> {
                authenticate()
            }
            is AuthUIEvent.SignUp ->{
                signUp()
            }
            is AuthUIEvent.SignIn -> {
                signIn()
            }
            is AuthUIEvent.GoOut -> {
                goOut()
                onEvent(AuthUIEvent.Authorized(false))
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun signUp(){
        viewModelScope.launch {

            state = state.copy(isLoading = true)

            val formattedDate = LocalDate.of(
                state.signUpDateOfBirthChanged.substring(4..7).toInt(),
                if(state.signUpDateOfBirthChanged[2].code != 0){
                    state.signUpDateOfBirthChanged.substring(2..3).toInt()
                }else{
                    state.signUpDateOfBirthChanged[3].code
                },
                if(state.signUpDateOfBirthChanged[0].code != 0){
                    state.signUpDateOfBirthChanged.substring(0..1).toInt()
                }else{
                    state.signUpDateOfBirthChanged[1].code
                }
            )


            val result = repository.signUp(
                login = state.signUpLoginChanged,
                password = state.signUpPasswordChanged,
                firstName = state.signUpFirstNameChanged,
                name = state.signUpNameChanged,
                lastName = state.signUpLastNameChanged,
                numberPhone = state.signUpNumberPhoneChanged,
                dateOfBirth = formattedDate
            )

            resultChannel.send(result)

            state = state.copy(isLoading = false)

        }
    }

    private fun signIn(){
        viewModelScope.launch {

            state = state.copy(isLoading = true)

            val result = repository.signIn(
                login = state.signInLoginChanged,
                password = state.signInPasswordChanged
            )

            resultChannel.send(result)

            state = state.copy(isLoading = false)

        }
    }

    private fun goOut(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = repository.goOut()

            resultChannel.send(result)

            state = state.copy(isLoading = false)

        }
    }

    private fun authenticate(content : () -> Unit = {}){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = repository.authenticate()

            if(result.data != null){
                content()
                onEvent(AuthUIEvent.Role(result.data.role))
                resultChannel.send(AuthResult.Authorized())
            }

            state = state.copy(isLoading = false)
        }
    }
}