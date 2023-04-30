package com.example.atomic_cinema.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.atomic_cinema.events.ProfileUIEvent
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.profile.ProfileResult
import com.example.atomic_cinema.utils.DatePicker
import com.example.atomic_cinema.utils.MaskDate
import com.example.atomic_cinema.utils.MaskNumberPhone
import com.example.atomic_cinema.utils.RoundedTextField
import com.example.atomic_cinema.viewModel.ProfileViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(navHostController: NavHostController, viewModel: ProfileViewModel = hiltViewModel()){

    val state = viewModel.state
    val copyState = viewModel.copyState

    val context = LocalContext.current

    val maxCharNumberPhone = 10

    var showDatePicker by remember { mutableStateOf(false) }
    var buttonIsEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel, context){
        viewModel.profileResults.collect{ result ->
            when(result){
                is ProfileResult.Shown -> {

                }
                is ProfileResult.Unauthorized -> {
                    Toast.makeText(
                        context,
                        "Попытка авторизации не удалась",
                        Toast.LENGTH_LONG).show()

                    navHostController.navigate(NavRoutes.Authorization.route)

                }
                is ProfileResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    LaunchedEffect(state){
        buttonIsEnabled =
                state.firstNameChanged.isNotBlank() &&
                state.nameChanged.isNotBlank() &&
                state.lastNameChanged.isNotBlank() &&
                state.numberPhoneChanged.isNotBlank() && maxCharNumberPhone == state.numberPhoneChanged.length &&
                state.dateOfBirthChanged.isNotBlank()
    }

    if(state.isLoading){
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
            contentAlignment = Alignment.Center){
            CircularProgressIndicator(color = Color.White)
        }
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
                    placeholder = "Ваш логин",
                    notNull = true,
                    enabled = !state.editMode,
                    readOnly = true,
                    value = state.login,
                    onValueChange = {})


                RoundedTextField(
                    label = "Фамилия",
                    placeholder = if(!state.editMode) "Введите фамилию" else "Ваша фамилия",
                    notNull = true,
                    readOnly = !state.editMode,
                    value = state.firstNameChanged,
                    onValueChange = {viewModel.onEvent(ProfileUIEvent.FirstNameChanged(it))})

                RoundedTextField(
                    label = "Имя",
                    placeholder = if(!state.editMode) "Введите имя" else "Ваше имя",
                    notNull = true,
                    readOnly = !state.editMode,
                    value = state.nameChanged,
                    onValueChange = {viewModel.onEvent(ProfileUIEvent.NameChanged(it))})

                RoundedTextField(
                    label = "Отчество",
                    placeholder = if (!state.editMode) "Введите отчество" else "Ваше отчество",
                    notNull = true,
                    readOnly = !state.editMode,
                    value = state.lastNameChanged,
                    onValueChange = {viewModel.onEvent(ProfileUIEvent.LastNameChanged(it))})

                RoundedTextField(
                    label = "Номер телефона",
                    placeholder = if(!state.editMode) "Введите номер телефона" else "Ваш номер телефона",
                    notNull = true == (maxCharNumberPhone == state.numberPhoneChanged.length),
                    readOnly = !state.editMode,
                    value = state.numberPhoneChanged,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = MaskNumberPhone(),
                    keyboardActions = KeyboardActions {

                    },
                    onValueChange = {
                        if(it.length <= maxCharNumberPhone) viewModel.onEvent(ProfileUIEvent.NumberPhoneChanged(it))
                    })

                SelectionContainer{

                }
                RoundedTextField(
                    label = "Дата рождения",
                    placeholder = if(!state.editMode) "Введите дату рождения" else "Выберите дату рождения",
                    notNull = true,
                    readOnly = true,
                    modifier = Modifier.clickable{},
                    value = state.dateOfBirthChanged,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    trailingIcon = {
                        IconButton(onClick = {
                        showDatePicker = true
                    }, enabled = state.editMode) {
                        Icon(Icons.Filled.EditCalendar, "")
                    }
                    },
                    visualTransformation = MaskDate(),
                    onValueChange = {
                        viewModel.onEvent(ProfileUIEvent.DateOfBirthChanged(it))
                    })

                    if(state.editMode){
                        Button(
                            modifier = Modifier.padding(top = 16.dp),
                            onClick = {
                                viewModel.onEvent(ProfileUIEvent.EditProfile)
                            },
                            enabled = buttonIsEnabled && state != copyState
                        ) {
                            Text(text = "Потвердить")
                        }
                        IconButton(onClick = {
                            viewModel.onEvent(ProfileUIEvent.DeclineEdit(false))
                        }) {
                            Icon(Icons.Filled.Close, "")
                        }
                    }else{
                        Button(
                            modifier = Modifier.padding(top = 16.dp),
                            onClick = {
                                viewModel.onEvent(ProfileUIEvent.EditMode(true))
                            },
                            ) {
                            Text(text = "Редактировать")
                        }
                    }
            }

            if(showDatePicker) {
                DatePicker(onValueChange = {viewModel.onEvent(
                    ProfileUIEvent.DateOfBirthChanged(
                        if(it.dayOfMonth >= 10) it.dayOfMonth.toString() else {"0${it.dayOfMonth}"} +
                                if (it.monthValue >= 10) it.monthValue.toString() else {"0${it.monthValue}"} +
                                it.year.toString()))
                })
                showDatePicker = false
            }
        }
    }
}