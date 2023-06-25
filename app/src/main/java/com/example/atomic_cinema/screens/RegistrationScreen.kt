package com.example.atomic_cinema.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.example.atomic_cinema.MainActivity
import com.example.atomic_cinema.events.AuthUIEvent
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.utils.*
import com.example.atomic_cinema.viewModel.AuthViewModel

@SuppressLint("NewApi", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RegistrationScreen(
    navHostController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
){

    val state = viewModel.state
    val context = LocalContext.current

    val maxCharLogin = 30
    val minCharLogin = 5
    val minCharPassword = 8
    val maxCharPassword = 35
    val maxCharDate = 8
    val maxCharNumberPhone = 10
    val maxCharFIO = 50

    var showDatePicker by remember { mutableStateOf(false) }
    var buttonIsEnabled by remember {mutableStateOf(false)}
    var passwordVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel, context){
        viewModel.authResults.collect{ result ->
            when(result){
                is AuthResult.Unauthorized -> {
//                    Toast.makeText(
//                        context,
//                        "Попытка авторизации не удалась",
//                        Toast.LENGTH_LONG).show()
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

                    viewModel.onEvent(AuthUIEvent.SignInLoginChanged(viewModel.state.signUpLoginChanged))
                    viewModel.onEvent(AuthUIEvent.SignInPasswordChanged(viewModel.state.signUpPasswordChanged))
                    viewModel.onEvent(AuthUIEvent.SignIn)
                }
                is AuthResult.Authorized -> {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)

                    navHostController.navigate(NavRoutes.Main.route, NavOptions.Builder()
                        .setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is AuthResult.UserIsAlreadyExist -> {

                }
                else -> {
                    Toast.makeText(
                        context,
                        "Данный логин уже занят",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    LaunchedEffect(state){
        val lengthFIO = state.signUpFirstNameChanged.length +
                state.signUpNameChanged.length +
                state.signUpLastNameChanged.length

        buttonIsEnabled = state.signUpLoginChanged.isNotBlank() &&
                state.signUpLoginChanged.length <= maxCharLogin &&
                state.signUpLoginChanged.length >= minCharLogin &&
            state.signUpPasswordChanged.isNotBlank() &&
                state.signUpPasswordChanged.length <= maxCharPassword &&
                state.signUpPasswordChanged.length >= minCharPassword &&
            state.signUpFirstNameChanged.isNotBlank() &&
            state.signUpNameChanged.isNotBlank() &&
            state.signUpLastNameChanged.isNotBlank() &&
            state.signUpNumberPhoneChanged.isNotBlank() &&
            state.signUpDateOfBirthChanged.isNotBlank() &&
                lengthFIO <= maxCharFIO

    }

    if(state.isLoading){
        LoadingScreen()
    }else{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.widthIn(min = 350.dp, max = 350.dp), contentAlignment = Alignment.Center) {
                Column {
                    RoundedTextField(label = "Логин",
                        placeholder = "Введите логин",
                        notNull = true,
                        value = state.signUpLoginChanged,
                        onValueChange = {
                            if(it.length <= maxCharLogin){
                                viewModel.onEvent(AuthUIEvent.SignUpLoginChanged(it))
                            }
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
                        onValueChange = {
                            if(it.length <= maxCharPassword){
                                viewModel.onEvent(AuthUIEvent.SignUpPasswordChanged(it))
                            }
                        }
                    )

                    RoundedTextField(
                        label = "Фамилия",
                        placeholder = "Введите фамилию",
                        notNull = true,
                        value = state.signUpFirstNameChanged,
                        onValueChange = {
                            val text = it.filter { !it.isDigit() }
                            viewModel.onEvent(AuthUIEvent.SignUpFirstNameChanged(text))
                        })

                    RoundedTextField(
                        label = "Имя",
                        placeholder = "Введите имя",
                        notNull = true,
                        value = state.signUpNameChanged,
                        onValueChange = {
                            val text = it.filter { !it.isDigit() }
                            viewModel.onEvent(AuthUIEvent.SignUpNameChanged(text))
                        })

                    RoundedTextField(
                        label = "Отчество",
                        placeholder = "Введите отчество",
                        notNull = true,
                        value = state.signUpLastNameChanged,
                        onValueChange = {
                            val text = it.filter { !it.isDigit() }
                            viewModel.onEvent(AuthUIEvent.SignUpLastNameChanged(text))
                        })

                    RoundedTextField(
                        label = "Номер телефона",
                        placeholder = "Введите номер телефона",
                        notNull = true,
                        value = state.signUpNumberPhoneChanged,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        visualTransformation = MaskNumberPhone(),
                        onValueChange = {
                            if(it.length <= maxCharNumberPhone){
                                val num = it.filter { it.isDigit() }
                                viewModel.onEvent(AuthUIEvent.SignUpNumberPhoneChanged(num))
                            }
                        })


                    RoundedTextField(
                        label = "Дата рождения",
                        placeholder = "Введите дату рождения",
                        notNull = true,
                        readOnly = true,
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
                }
            }
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = {
                        viewModel.onEvent(AuthUIEvent.SignUp)
                    },
                    enabled = buttonIsEnabled

                ) {
                    Text(text = "Подтвердить")
                }
                if(!buttonIsEnabled){
                    Text(text = "Проверьте правильность введенных данных:" +
                            "\n 1. ФИО не должно превышать $maxCharFIO " +
                            "\n 2. Минимальная длина пароля: $minCharPassword" +
                            "\n 3. Минимальная длина логина: $minCharLogin"+
                            "\n 4. Все поля должны быть заполнены",
                        color = Color.Gray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Left)
                }

                Spacer(Modifier.height(70.dp))
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


