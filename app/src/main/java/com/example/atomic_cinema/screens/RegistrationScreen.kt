package com.example.atomic_cinema.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.atomic_cinema.events.AuthUIEvent
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.utils.*
import com.example.atomic_cinema.viewModel.AuthViewModel

@SuppressLint("NewApi", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RegistrationScreen(navHostController: NavHostController, viewModel: AuthViewModel = hiltViewModel()){

    val state = viewModel.state
    val context = LocalContext.current

    val maxCharDate = 8
    val maxCharNumberPhone = 11

    var showDatePicker by remember { mutableStateOf(false) }
    var buttonIsEnabled by remember {mutableStateOf(false)}
    var passwordVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel, context){
        viewModel.authResults.collect{ result ->
            when(result){
                is AuthResult.Unauthorized -> {
                    Toast.makeText(
                        context,
                        "Попытка авторизации не удалась",
                        Toast.LENGTH_LONG).show()
                }
                is AuthResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()
                }
                is AuthResult.Registered -> {
                    Toast.makeText(
                        context,
                        "Вы успешно зарегистрировались",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    LaunchedEffect(state){
        buttonIsEnabled = state.signUpLoginChanged.isNotBlank() &&
            state.signUpPasswordChanged.isNotBlank() &&
            state.signUpFirstNameChanged.isNotBlank() &&
            state.signUpNameChanged.isNotBlank() &&
            state.signUpLastNameChanged.isNotBlank() &&
            state.signUpNumberPhoneChanged.isNotBlank() &&
            state.signUpDateOfBirthChanged.isNotBlank()
    }

    if(state.isLoading){
        LoadingScreen()
    }else{
        Scaffold{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RoundedTextField(label = "Логин",
                    placeholder = "Введите логин",
                    notNull = true,
                    value = state.signUpLoginChanged,
                    onValueChange = {
                        viewModel.onEvent(AuthUIEvent.SignUpLoginChanged(it))
                    })

                RoundedTextField(
                    label = "Пароль",
                    placeholder = "Введите пароль",
                    notNull = true,
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisibility) "Скрыть пароль" else "Показать пароль"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    value = state.signUpPasswordChanged,
                    onValueChange = {viewModel.onEvent(AuthUIEvent.SignUpPasswordChanged(it))}
                )

                RoundedTextField(
                    label = "Фамилия",
                    placeholder = "Введите фамилию",
                    notNull = true,
                    value = state.signUpFirstNameChanged,
                    onValueChange = {viewModel.onEvent(AuthUIEvent.SignUpFirstNameChanged(it))})

                RoundedTextField(
                    label = "Имя",
                    placeholder = "Введите имя",
                    notNull = true,
                    value = state.signUpNameChanged,
                    onValueChange = {viewModel.onEvent(AuthUIEvent.SignUpNameChanged(it))})

                RoundedTextField(
                    label = "Отчество",
                    placeholder = "Введите отчество",
                    notNull = true,
                    value = state.signUpLastNameChanged,
                    onValueChange = {viewModel.onEvent(AuthUIEvent.SignUpLastNameChanged(it))})

                RoundedTextField(
                    label = "Номер телефона",
                    placeholder = "Введите номер телефона",
                    notNull = true,
                    value = state.signUpNumberPhoneChanged,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = MaskNumberPhone(),
                    keyboardActions = KeyboardActions {

                    },
                    onValueChange = {
                        if(it.length <= maxCharNumberPhone) viewModel.onEvent(AuthUIEvent.SignUpNumberPhoneChanged(it))
                    })


                RoundedTextField(
                    label = "Дата рождения",
                    placeholder = "Введите дату рождения",
                    notNull = true,
                    enabled = true,
                    value = state.signUpDateOfBirthChanged,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    trailingIcon = { IconButton(onClick = {
                        showDatePicker = true
                    }) {
                        Icon(Icons.Filled.EditCalendar, "")
                    }},
                    visualTransformation = MaskDate(),
                    onValueChange = {
                        if(it.length <= maxCharDate) viewModel.onEvent(AuthUIEvent.SignUpDateOfBirthChanged(it))
                    })

                    Button(
                        modifier = Modifier.padding(top = 16.dp),
                        onClick = {
                            viewModel.onEvent(AuthUIEvent.SignUp)
                        },
                        enabled = buttonIsEnabled

                    ) {
                        Text(text = "Подтвердить")
                    }

               }

                if(showDatePicker) {
                    DatePicker(onValueChange = {viewModel.onEvent(
                        AuthUIEvent.SignUpDateOfBirthChanged(
                            if(it.dayOfMonth >= 10) it.dayOfMonth.toString() else {"0${it.dayOfMonth}"} +
                                    if (it.monthValue >= 10) it.monthValue.toString() else {"0${it.monthValue}"} +
                                    it.year.toString()))
                    })
                    showDatePicker = false
                }
            }
        }
    }


