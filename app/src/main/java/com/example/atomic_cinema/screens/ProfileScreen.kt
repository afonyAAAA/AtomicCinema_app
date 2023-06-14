package com.example.atomic_cinema.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.example.atomic_cinema.events.AuthUIEvent
import com.example.atomic_cinema.events.ProfileUIEvent
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.server.profile.ProfileResult
import com.example.atomic_cinema.stateClasses.ProfileState
import com.example.atomic_cinema.utils.*
import com.example.atomic_cinema.viewModel.AuthViewModel
import com.example.atomic_cinema.viewModel.ProfileViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel(),
    viewModelA: AuthViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val state = viewModel.state
    val copyState = viewModel.copyState

    val maxCharNumberPhone = 10
    val maxCharFIO = 50

    var buttonIsEnabled by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var textAboutPayments by remember { mutableStateOf("") }

    LaunchedEffect(viewModel, context) {
        viewModelA.authResults.collect { result ->
            when (result) {
                is AuthResult.Authorized -> {
                    viewModelA.onEvent(AuthUIEvent.Authorized(true))
                }
                is AuthResult.Unauthorized -> {
                    viewModelA.onEvent(AuthUIEvent.Authorized(false))
                }
                else -> {

                }
            }
        }
    }

    LaunchedEffect(viewModel, context){
        viewModel.profileResults.collect{ result ->
            when(result){
                is ProfileResult.Shown -> {

                }
                is ProfileResult.Unauthorized -> {
                    Toast.makeText(
                        context, "Авторизируйтесь в системе.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navHostController.navigate(NavRoutes.Authorization.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is ProfileResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()
                }
                is ProfileResult.Edited -> TODO()
                is ProfileResult.MoneyOperationIsSuccessful -> {
                    viewModel.onEvent(ProfileUIEvent.MoneyOperationIsSuccessful(true))
                }
                is ProfileResult.FewReplenish -> {
                    Toast.makeText(
                        context,
                        "Сумма пополнения должна быть больше 100 руб.",
                        Toast.LENGTH_LONG).show()
                }
                is ProfileResult.InsufficientFunds -> TODO()
                is ProfileResult.NotFoundTicketsForMonth -> {

                    Toast.makeText(
                        context,
                        "Вы не приобретали билеты за этот месяц",
                        Toast.LENGTH_LONG).show()

                    textAboutPayments = "Увы, за этот месяц не было трат"
                }
                is ProfileResult.MoneyReturnISSuccessful -> {}
            }
        }
    }

    LaunchedEffect(state){

        val lengthFIO = state.firstNameChanged.length +
                        state.nameChanged.length +
                        state.lastNameChanged.length

        buttonIsEnabled =
                state.firstNameChanged.isNotBlank() &&
                state.nameChanged.isNotBlank() &&
                state.lastNameChanged.isNotBlank() &&
                state.numberPhoneChanged.isNotBlank() &&
                maxCharNumberPhone == state.numberPhoneChanged.length &&
                state.dateOfBirthChanged.isNotBlank() &&
                        lengthFIO <= maxCharFIO
    }

    if(state.loadingMoneyOperation){
        LoadingMoneyOperationScreen(
            navHostController = navHostController,
            state.isLoading,
            sum = state.sumReplenish.toDouble(),
            state.moneyOperationIsSuccessful,
            isPaying = false,
            NavRoutes.UserProfile.route
        )
    }else{
        if(state.isLoading){
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.primary),
                contentAlignment = Alignment.Center){
                CircularProgressIndicator(color = Color.White)
            }
        }else{
            if(state.viewBalance || Support.viewBalance && !state.isLoading){
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(Modifier.height(100.dp))

                    Text("Ваш баланс")

                    Spacer(Modifier.height(50.dp))

                    Text(state.balance + " ₽", fontWeight = FontWeight.Bold, fontSize = 25.sp)

                    Spacer(Modifier.height(50.dp))

                    Button(onClick = {
                        viewModel.onEvent(ProfileUIEvent.ReplenishBalanceChanged(true))
                    }) {
                        Text(text = "Пополнить")
                    }

                    Spacer(Modifier.height(50.dp))

                    Button(onClick = {
                        viewModel.onEvent(ProfileUIEvent.GenerateSpendingReport)
                        textAboutPayments = ""
                    }) {
                        Text(text = "Сформировать отчёт по тратам")
                    }

                    Spacer(Modifier.height(50.dp))

                    if(state.sumSpending != 0.0 && state.countTicketSpending != 0){
                        Text(text = "За этот месяц:", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(text = "Количество приобретенных билетов: ${state.countTicketSpending} шт.")
                        Text(text = "Потраченная сумма на билеты: ${state.sumSpending} ₽")
                    }else{
                        Text(text = textAboutPayments)
                    }
                }

                if(state.replenishBalanceMode){
                    AlertDialogReplenishBalance(profile = state, viewModel = viewModel)
                }

            }else{


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.widthIn(min = 380.dp, max = 380.dp), contentAlignment = Alignment.Center){
                        Column{
                            RoundedTextField(label = "Логин",
                                placeholder = "Ваш логин",
                                notNull = true,
                                enabled = !state.editMode,
                                readOnly = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                value = state.login,
                                onValueChange = {})


                            RoundedTextField(
                                label = "Фамилия",
                                placeholder = if(!state.editMode) "Введите фамилию" else "Ваша фамилия",
                                notNull = true,
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                                readOnly = !state.editMode,
                                value = state.firstNameChanged,
                                onValueChange = {
                                    val text = it.filter { !it.isDigit() }
                                    viewModel.onEvent(ProfileUIEvent.FirstNameChanged(text))
                                }

                            )

                            RoundedTextField(
                                label = "Имя",
                                placeholder = if(!state.editMode) "Введите имя" else "Ваше имя",
                                notNull = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                readOnly = !state.editMode,
                                value = state.nameChanged,
                                onValueChange = {
                                    val text = it.filter { !it.isDigit() }
                                    viewModel.onEvent(ProfileUIEvent.NameChanged(text))
                                })

                            RoundedTextField(
                                label = "Отчество",
                                placeholder = if (!state.editMode) "Введите отчество" else "Ваше отчество",
                                notNull = true,
                                readOnly = !state.editMode,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                value = state.lastNameChanged,
                                onValueChange = {
                                    val text = it.filter { !it.isDigit() }
                                    viewModel.onEvent(ProfileUIEvent.LastNameChanged(text))
                                })

                            RoundedTextField(
                                label = "Номер телефона",
                                placeholder = if(!state.editMode) "Введите номер телефона" else "Ваш номер телефона",
                                notNull = true == (maxCharNumberPhone == state.numberPhoneChanged.length),
                                readOnly = !state.editMode,
                                value = state.numberPhoneChanged,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                visualTransformation = MaskNumberPhone(),
                                onValueChange = {
                                    val num = it.filter { it.isDigit() }
                                    if(it.length <= maxCharNumberPhone) viewModel.onEvent(ProfileUIEvent.NumberPhoneChanged(num))

                                })

                            RoundedTextField(
                                label = "Дата рождения",
                                placeholder = if(!state.editMode) "Введите дату рождения" else "Выберите дату рождения",
                                notNull = true,
                                readOnly = true,
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
                        if(!buttonIsEnabled){
                            Text(text = "Проверьте правильность введенных данных:" +
                                    "\n 1. ФИО не должно превышать 50 символов " +
                                    "\n 2. Все поля должны быть заполнены",
                                color = Color.Gray,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Left)
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
            }
        }
    }
}

    @Composable
    fun AlertDialogReplenishBalance(profile : ProfileState, viewModel: ProfileViewModel){
        var numberCard by remember { mutableStateOf("") }
        val maxCharNumberCard = 16
        var buttonIsEnabled by remember {
            mutableStateOf(
                maxCharNumberCard == numberCard.length && profile.sumReplenish.isNotBlank()
            )
        }
        LaunchedEffect(numberCard, profile){
            buttonIsEnabled = maxCharNumberCard == numberCard.length && profile.sumReplenish.isNotBlank()
        }

        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                viewModel.onEvent(ProfileUIEvent.ReplenishBalanceChanged(false))
                viewModel.onEvent(ProfileUIEvent.SumReplenish(""))
                               },
            title = {
                Text(text = "Пополнение баланса", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    if(!buttonIsEnabled){
                        Text("Все поля должны быть заполнены", fontSize = 15.sp, color = Color.Gray)
                    }

                    Spacer(Modifier.height(20.dp))

                    RoundedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Номер карты",
                        value = numberCard,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = "Введите номер карты",
                        visualTransformation = MaskNumberCard(),
                        onValueChange = {numberCard = it})

                    Spacer(Modifier.height(20.dp))

                    RoundedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Сумма пополнения", value = profile.sumReplenish.replace(".", ","),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = "Введите сумму пополнения",
                        onValueChange = {
                            if(it != "0" && it.isDigitsOnly() || it.indexOf(",") != -1 && it.split(",").size == 2){
                                val maxNum = if(it.indexOf(",") != -1){
                                    when(it.substringAfter(",").length){
                                        0 -> {
                                            it.length + 2
                                        }
                                        1 -> {
                                            it.length + 1
                                        }
                                        2 -> {
                                            it.length
                                        }else -> {
                                            it.length - 1
                                        }
                                    }
                                }else{
                                    6
                                }
                                if (it.length <= maxNum){
                                    val splitSum = it.split(",")
                                    if(splitSum.size != 1){
                                        if(splitSum[1].isNotBlank() && splitSum[1].isDigitsOnly() || splitSum[1] == ""){
                                            viewModel.onEvent(ProfileUIEvent.SumReplenish(it.replace(",", ".")))
                                        }
                                    }else{
                                        viewModel.onEvent(ProfileUIEvent.SumReplenish(it.replace(",", ".")))
                                    }
                                }
                            }
                        })

                }

            },
            buttons = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()){
                    Column{
                        Button(onClick = {
                            viewModel.onEvent(ProfileUIEvent.ReplenishBalanceChanged(false))
                            viewModel.onEvent(ProfileUIEvent.ConfirmReplenish(profile.sumReplenish.toDouble()))
                        }, enabled = buttonIsEnabled) {
                            Text(text = "Пополнить")
                        }

                    }

                }


            }
        )
    }
