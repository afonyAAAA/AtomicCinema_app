package com.example.atomic_cinema.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.example.atomic_cinema.MainActivity
import com.example.atomic_cinema.events.AuthUIEvent
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.utils.AppFinisher
import com.example.atomic_cinema.utils.FinishListener
import com.example.atomic_cinema.utils.LoadingScreen
import com.example.atomic_cinema.utils.RoundedTextField
import com.example.atomic_cinema.viewModel.AuthViewModel

@SuppressLint("NewApi", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AuthorizationScreen(
    navHostController: NavHostController,
    viewModelAuth: AuthViewModel = hiltViewModel(),
){

    val state = viewModelAuth.state
    val context = LocalContext.current

    var passwordVisibility by remember { mutableStateOf(false) }
    var buttonIsEnabled by remember{ mutableStateOf(false) }

    LaunchedEffect(viewModelAuth, context){
        viewModelAuth.authResults.collect{ result ->
            when(result){
                is AuthResult.Unauthorized -> {
//                    Toast.makeText(
//                        context,
//                        "Попытка авторизации не удалась.",
//                        Toast.LENGTH_LONG).show()
                }
                is AuthResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже.",
                        Toast.LENGTH_LONG).show()
                }
                is AuthResult.Authorized -> {
                    Toast.makeText(
                        context,
                        "Вы успешно авторизовались.",
                        Toast.LENGTH_LONG).show()

                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)

                    navHostController.navigate(NavRoutes.Main.route, NavOptions.Builder()
                        .setPopUpTo(NavRoutes.Main.route, true).build())

                }
                is AuthResult.Registered -> {
                    Toast.makeText(
                        context,
                        "Аккаунт создан.",
                        Toast.LENGTH_LONG).show()

                }
                is AuthResult.UserIsAlreadyExist -> {
                    Toast.makeText(
                        context,
                        "Ошибка. Такой пользователь уже существует.",
                        Toast.LENGTH_LONG).show()

                }
                is AuthResult.CameOut -> TODO()
                is AuthResult.NotValidPassword -> {
                    Toast.makeText(
                        context,
                        "Неверный пароль.",
                        Toast.LENGTH_LONG).show()
                }
                is AuthResult.OK -> TODO()
                is AuthResult.UserNotFound -> {
                    Toast.makeText(
                        context,
                        "Пользователя с таким логином не существует.",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(state){
        buttonIsEnabled = state.signInLoginChanged.isNotBlank() && state.signInPasswordChanged.isNotBlank()
    }

    if(state.isLoading){
        LoadingScreen()
    }else{
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(Modifier.widthIn(min = 350.dp, max = 350.dp), contentAlignment = Alignment.Center){
                Column{
                    RoundedTextField(
                        label = "Логин",
                        value = state.signInLoginChanged,
                        placeholder = "Логин",
                        notNull = true,
                        onValueChange = {viewModelAuth.onEvent(AuthUIEvent.SignInLoginChanged(it))})

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
                        value = state.signInPasswordChanged,
                        onValueChange = {viewModelAuth.onEvent(AuthUIEvent.SignInPasswordChanged(it))}
                    )
                }
            }
            Button(
                onClick = {viewModelAuth.onEvent(AuthUIEvent.SignIn)},
                Modifier.padding(16.dp),
                enabled = buttonIsEnabled) {
                Text(text = "Подтвердить")
            }

            Text(text = "Ещё не зарегистрированы?", fontSize = 12.sp)

            OutlinedButton(onClick = {navHostController.navigate(NavRoutes.Registration.route)}, Modifier.padding(16.dp)) {
                Text(text = "Регистрация")
            }
        }
    }
}