package com.example.atomic_cinema.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.atomic_cinema.events.ProfileUIEvent
import com.example.atomic_cinema.events.TicketUIEvent
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.server.profile.ProfileResult
import com.example.atomic_cinema.server.ticket.TicketResults
import com.example.atomic_cinema.stateClasses.MovieState
import com.example.atomic_cinema.stateClasses.ProfileState
import com.example.atomic_cinema.stateClasses.SeanceState
import com.example.atomic_cinema.stateClasses.TicketState
import com.example.atomic_cinema.utils.*
import com.example.atomic_cinema.viewModel.AuthViewModel
import com.example.atomic_cinema.viewModel.ProfileViewModel
import com.example.atomic_cinema.viewModel.TicketViewModel
import kotlinx.datetime.toKotlinLocalTime
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TicketsScreen(
    navHostController: NavHostController,
    movieState: MovieState = Support.copyMovie,
    seanceState: SeanceState = Support.copySeance,
    viewModelT: TicketViewModel = hiltViewModel(),
    viewModelA: AuthViewModel = hiltViewModel(),
    viewModelP: ProfileViewModel = hiltViewModel()
){

    val ticketState = viewModelT.state
    val profileState = viewModelP.state
    val authState = viewModelA.state

    val context = LocalContext.current

    LaunchedEffect(viewModelT, context){
        viewModelT.ticketResults.collect{ result ->
            when(result){
                is TicketResults.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()

                    if(ticketState.sumPay != ""){
                        viewModelT.onEvent(TicketUIEvent.ConfirmTicketWithoutPay(seanceState))
                    }
                }
                is TicketResults.OK -> {
                    Toast.makeText(
                        context,
                        "Билет приобретён",
                        Toast.LENGTH_LONG).show()
                    viewModelT.onEvent(TicketUIEvent.PayChanged(false))

                }
                is TicketResults.Unauthorized -> {
                    navHostController.navigate(NavRoutes.Authorization.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is TicketResults.InsufficientFunds -> {
                    Toast.makeText(
                        context,
                        "Недостаточно средств. Покупка не удалась.",
                        Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(viewModelT, context){
        viewModelP.profileResults.collect{ result ->
            when(result){
                is ProfileResult.UnknownError -> {

                }
                is ProfileResult.MoneyOperationIsSuccessful -> {
                    viewModelP.onEvent(ProfileUIEvent.MoneyOperationIsSuccessful(true))
                    viewModelT.onEvent(TicketUIEvent.ConfirmTicket(seanceState))
                }
                else -> {

                }
            }
        }
    }

    LaunchedEffect(viewModelT, context){
        viewModelA.authResults.collect{ result ->
            when(result){
                is AuthResult.Unauthorized -> {
                    Toast.makeText(
                        context,
                        "Авторизируйтесь в системе",
                        Toast.LENGTH_LONG).show()
                    navHostController.navigate(NavRoutes.Authorization.route,
                    NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is AuthResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()
                }
                is AuthResult.Authorized -> {
                    viewModelP.onEvent(ProfileUIEvent.ShowProfile)
                }
                else ->{

                }
            }
        }
    }

    if(profileState.loadingMoneyOperation){
        LoadingMoneyOperationScreen(
            navHostController,
            profileState.isLoading,
            ticketState.sumPay.toDouble(),
            profileState.moneyOperationIsSuccessful,
            true,
            NavRoutes.Movies.route
        )
    }else{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if(profileState.isLoading){
                Box(Modifier.fillMaxWidth()){
                    CircularProgressIndicator()
                }
            }else{
                viewModelT.onEvent(TicketUIEvent.IdUserChanged(authState.userID))
                viewModelT.onEvent(TicketUIEvent.IdSeanceChanged(seanceState.id))
                InfoUser(viewModelT, ticketState, profileState)
            }

            Spacer(modifier = Modifier.height(25.dp))

            InfoMovie(movieState)

            Spacer(modifier = Modifier.height(25.dp))

            InfoSeance(seanceState)

            Spacer(modifier = Modifier.height(25.dp))

            InfoSumPay(seanceState, ticketState)

            Spacer(modifier = Modifier.height(25.dp))

            Button(onClick = {
                viewModelT.onEvent(TicketUIEvent.PayChanged(true))
            }, enabled = if(seanceState.selectedDateSeance == LocalDate.now()) {
                seanceState.timeStart!!.toKotlinLocalTime() >= LocalTime.now().toKotlinLocalTime()
            } else{
                seanceState.selectedDateSeance > LocalDate.now()
            }){
                Text(text = "Потвердить")
            }
            if(seanceState.selectedDateSeance == LocalDate.now()) {
                if (seanceState.timeStart!!.toKotlinLocalTime() <= LocalTime.now().toKotlinLocalTime()){
                    Text(text = "Сеанс уже начался")
                }
            }else if(seanceState.selectedDateSeance < LocalDate.now()){
                Text(text = "Сеанс уже прошёл")
            }

            Spacer(modifier = Modifier.height(50.dp))

            if(ticketState.showPayAlertDialog){
                AlertDialogPayTicket(
                    seanceState = seanceState,
                    viewModelT = viewModelT,
                    profileState = profileState,
                    viewModelP = viewModelP,
                    ticketState = ticketState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InfoUser(
    viewModelT : TicketViewModel,
    ticketState: TicketState,
    profileState: ProfileState){

    val number = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

    RoundedTextField(
        label = "Ваше имя",
        value = profileState.firstNameChanged + " " + profileState.nameChanged + " " + profileState.lastNameChanged,
        placeholder = "Ваше имя",
        onValueChange = {},
        readOnly = true)
    RoundedTextField(
        label = "Ваш телефон",
        value = profileState.numberPhoneChanged,
        placeholder = "Ваш телефон",
        visualTransformation = MaskNumberPhone(),
        onValueChange = {},
        readOnly = true)



    ExposedDropdownMenuBox(
        expanded = ticketState.expandedNumberTextField,
        onExpandedChange = {
            viewModelT.onEvent(TicketUIEvent.ExpandedTextFieldChanged(!ticketState.expandedNumberTextField))
        }
    ) {
        RoundedTextField(
            label = "Выберите кол-во билетов",
            value = ticketState.count.toString(), placeholder = "Количество билетов",
            onValueChange ={},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = ticketState.expandedNumberTextField
                )
            }
        )
        ExposedDropdownMenu(
            expanded = ticketState.expandedNumberTextField,
            onDismissRequest = {
                viewModelT.onEvent(TicketUIEvent.ExpandedTextFieldChanged(false))
            }) {
            number.forEach { selectionNumber ->
                DropdownMenuItem(onClick = {
                    viewModelT.onEvent(TicketUIEvent.CountChanged(selectionNumber))
                    viewModelT.onEvent(TicketUIEvent.ExpandedTextFieldChanged(false))
                }) {
                    Text(text = selectionNumber.toString())
                }

            }
        }
    }
}

@Composable
fun InfoMovie(movieState : MovieState){
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(movieState.linkImage.replace("https", "http"))
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )
    Row(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(15.dp))
        .border(3.dp, MaterialTheme.colors.primary, RoundedCornerShape(15.dp))){
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier.height(250.dp),
            Alignment.Center,
            contentScale = ContentScale.Fit,
        )
        Column(modifier = Modifier
            .padding(start = 5.dp)
            .heightIn(250.dp), verticalArrangement = Arrangement.Center){

            Text(text = movieState.nameMovie,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp)

            Spacer(Modifier.height(25.dp))

            Text("Возрастное ограничение", color = Color.Gray, fontSize = 15.sp)

            Text(movieState.ageRating, modifier = Modifier.padding(start = 5.dp), fontSize = 15.sp)

            Spacer(Modifier.height(25.dp))

            Text("Продолжительность", color = Color.Gray, fontSize = 15.sp)

            Text(
                text = "${movieState.duration} мин. / ${"%.1f".format(movieState.duration.toDouble() / 60)} ч.",
                modifier = Modifier.padding(start = 5.dp),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun InfoSeance(seanceState: SeanceState){
    Card(
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp,
                MaterialTheme.colors.primary,
                RoundedCornerShape(15.dp))
    ) {
        Column(Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Text(seanceState.price.toString() + " ₽",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp)
                Text(seanceState.selectedDateSeance.toString().toMyDateFormat(seanceState.selectedDateSeance),
                    color = Color.Gray)
            }
            Text(seanceState.addressCinema)
            Spacer(Modifier.height(10.dp))
            Text("${seanceState.timeStart} - ${seanceState.timeEnd}",
                fontSize = 16.sp)
            Text("Тип зала: ${seanceState.typeHall}")
        }
    }
}

@Composable
fun InfoSumPay(seanceState: SeanceState, ticketState: TicketState){
    Text("${seanceState.price} * ${ticketState.count}")
    Text("Итого: ${ticketState.count * seanceState.price}")
}

@Composable
private fun AlertDialogPayTicket(
    profileState: ProfileState,
    seanceState: SeanceState,
    ticketState: TicketState,
    viewModelT: TicketViewModel,
    viewModelP: ProfileViewModel
){
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = {viewModelT.onEvent(TicketUIEvent.PayChanged(false))},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Text(text = "Оплата", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Text(text = "Ваш баланс : " + profileState.balance)

                Spacer(Modifier.height(20.dp))

            }

        },
        buttons = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()){
                Button(onClick = {
                    viewModelT.onEvent(TicketUIEvent.SumPay((seanceState.price * ticketState.count).toString()))
                    viewModelP.onEvent(ProfileUIEvent.ConfirmSubtract(seanceState.price * ticketState.count))
                }) {
                    Text(text = "Оплатить")
                }
            }
        }
    )
}