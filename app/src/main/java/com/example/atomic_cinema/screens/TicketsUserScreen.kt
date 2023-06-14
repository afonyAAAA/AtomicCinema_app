package com.example.atomic_cinema.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.atomic_cinema.utils.LoadingScreen
import com.example.atomic_cinema.utils.RoundedTextField
import com.example.atomic_cinema.utils.toMyDateFormat
import com.example.atomic_cinema.viewModel.*
import kotlinx.datetime.toKotlinLocalTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TicketsUserScreen(
    navHostController: NavHostController,
    viewModelT: TicketViewModel = hiltViewModel(),
    viewModelP: ProfileViewModel = hiltViewModel(),
    viewModelA: AuthViewModel = hiltViewModel()
){

    val context = LocalContext.current
    val stateTicket = viewModelT.state
    val listTickets = viewModelT.listStateTicket
    val stateProfile = viewModelP.state

    LaunchedEffect(viewModelT, context){
        viewModelT.ticketResults.collect{ result ->
            when(result){
                is TicketResults.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()
                }
                is TicketResults.OK -> {}
                is TicketResults.Unauthorized -> {}
                is TicketResults.InsufficientFunds -> {
                    Toast.makeText(
                        context,
                        "Недостаточно средств. Оплата не удалась.",
                        Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(viewModelT, context){
        viewModelP.profileResults.collect{ result ->
            when(result){
                is ProfileResult.MoneyOperationIsSuccessful -> {
                    if(!stateTicket.returned){
                        viewModelT.onEvent(TicketUIEvent.ReturnTicket)
                    }else{
                        viewModelP.onEvent(ProfileUIEvent.MoneyOperationIsSuccessful(true))
                        viewModelT.onEvent(TicketUIEvent.PayingTicketAwaitingPayment)
                    }
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(viewModelT, context){
        viewModelA.authResults.collect{ result ->
            when(result){
                is AuthResult.Unauthorized -> {
                    Toast.makeText(
                        context, "Авторизируйтесь в системе.",
                        Toast.LENGTH_SHORT
                    ).show()

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
                    viewModelT.onEvent(TicketUIEvent.GetTickets)
                }
                else ->{

                }
            }
        }
    }

//    if(stateProfile.loadingMoneyOperation){
//        LoadingMoneyOperationScreen(
//            navHostController,
//            stateProfile.isLoading,
//            stateTicket.price * stateTicket.count,
//            stateProfile.moneyOperationIsSuccessful,
//            true,
//            NavRoutes.UserTickets.route
//        )
//    }else{
        if(stateTicket.isLoading){
            LoadingScreen()
        }else{
            if(stateTicket.detailsTicketChanged){
                val movieState = MovieState(
                    nameMovie = stateTicket.nameMovie,
                    linkImage = stateTicket.linkImage,
                    duration = stateTicket.duration,
                    ageRating = stateTicket.ageRating
                )

                val infoSeance = SeanceState(
                    addressCinema = stateTicket.addressCinema,
                    timeStart = LocalTime.parse(stateTicket.timeStart, DateTimeFormatter.ofPattern("HH:mm")),
                    timeEnd = LocalTime.parse(stateTicket.timeEnd,DateTimeFormatter.ofPattern("HH:mm")),
                    price = stateTicket.price,
                    typeHall = stateTicket.nameTypeHall
                )

                val isTicketCanReturn by remember {
                    mutableStateOf(
                        !stateTicket.returned &&
                                stateTicket.nameStatus == "Оплачено" &&
                                LocalDate.parse(stateTicket.dateStartSeance, DateTimeFormatter.ofPattern("dd.MM.yyyy")) >= LocalDate.now() &&
                                infoSeance.timeStart!!.toKotlinLocalTime() > LocalTime.now().toKotlinLocalTime())
                }

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ){

                    Spacer(Modifier.height(10.dp))

                    Box(contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .height(50.dp)
                            .background(MaterialTheme.colors.primary,
                                RoundedCornerShape(bottomEnd = 10.dp, topEnd = 10.dp))
                            .padding(end = 40.dp)

                    ){
                        Text(text = "  Информация о билете", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(10.dp))

                    Column(modifier =
                    Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){

                        DetailsTicket(
                            profile = stateProfile,
                            ticket = stateTicket,
                            seance = infoSeance,
                            viewModelT = viewModelT,
                            viewModelP = viewModelP,
                        )

                        Spacer(Modifier.height(25.dp))

                        InfoMovie(movieState = movieState)

                        Spacer(Modifier.height(25.dp))

                        InfoSeance(seanceState = infoSeance)

                        Spacer(Modifier.height(25.dp))

                        if(stateTicket.nameStatus != "Не оплачено"){
                            Button(
                                onClick = {
                                    viewModelP.onEvent(ProfileUIEvent.ConfirmReplenish((stateTicket.price * stateTicket.count)))
                                          },
                                enabled = isTicketCanReturn
                            ) {
                                Text("Вернуть билет")
                            }

                            if(!isTicketCanReturn){
                                Text(text = "Билет уже возвращен или ожидает платежа, или сеанс уже прошёл", color = Color.Gray, textAlign = TextAlign.Center)
                            }
                            Spacer(Modifier.height(25.dp))
                        }

                    }
                }
            }else{
                if(listTickets.isNotEmpty()){
                    ListTickets(listTickets = listTickets, viewModelT)
                }else{
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(text = "Тут пока пусто...")
                    }
                }

            }
        }
    }
//    }

@Composable
fun ListTicketElement(ticket : TicketState, viewModelT: TicketViewModel){

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(ticket.linkImage.replace("https", "http"))
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )

    val formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                viewModelT.onEvent(TicketUIEvent.DetailsScreenChanged(true, ticket.idTicket))
            }
    ) {
        Column(Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painter,
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(100.dp)
                        .clip(RoundedCornerShape(15.dp)))

                Column(Modifier.padding(start = 10.dp)){
                    Text(ticket.nameMovie,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp)
                    Text(text = ticket.dateStartSeance.toMyDateFormat(LocalDate.parse(ticket.dateStartSeance, formatterDate)))
                    Text(text = ticket.addressCinema)
                    Text(text = "Начало сеанса : ${ticket.timeStart}")
                }
            }
        }
    }
}

@Composable
fun ListTickets(listTickets : List<TicketState>, viewModelT: TicketViewModel){
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(5.dp)){
        items(items = listTickets){ ticket ->
            ListTicketElement(ticket = ticket, viewModelT = viewModelT)
        }
    }
}

@Composable
fun DetailsTicket(
    profile : ProfileState,
    ticket : TicketState,
    seance: SeanceState,
    viewModelT: TicketViewModel,
    viewModelP: ProfileViewModel
){
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        RoundedTextField(
            value = profile.firstNameChanged + " " + profile.nameChanged + " " + profile.lastNameChanged,
            readOnly = true,
            onValueChange = {},
            placeholder = "ФИО покупателя",
            label = "ФИО покупателя"
        )

        RoundedTextField(
            value = ticket.count.toString(),
            readOnly = true,
            onValueChange = {},
            placeholder = "Количество билетов",
            label = "Количество билетов"
        )


        RoundedTextField(
            value = ticket.dateTime.toString().toMyDateFormat(ticket.dateTime.toLocalDate()),
            readOnly = true,
            onValueChange = {},
            placeholder = "Дата покупки",
            label = "Дата покупки"
        )

        RoundedTextField(
            value = (ticket.price * ticket.count).toString(),
            readOnly = true,
            onValueChange = {},
            placeholder = "Сумма покупки",
            label = "Сумма покупки"
        )


        RoundedTextField(
            value = ticket.nameStatus,
            readOnly = true,
            onValueChange = {},
            placeholder = "Статус платежа",
            label = "Статус платежа"
        )

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
            if(ticket.nameStatus != "Оплачено"){
                Button(onClick = {
                    //viewModelT.onEvent(TicketUIEvent.PayChanged(true))
                }) {
                    Text(text = "Оплатить")
                }
            }
        }

        if(ticket.showPayAlertDialog){
            AlertDialogPayTicket(profileState = profile,
                seanceState = seance,
                ticketState = ticket,
                viewModelT = viewModelT,
                viewModelP = viewModelP)
        }
    }
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
