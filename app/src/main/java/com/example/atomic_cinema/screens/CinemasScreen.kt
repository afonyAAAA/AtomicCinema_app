package com.example.atomic_cinema.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.cinema.CinemaResults
import com.example.atomic_cinema.stateClasses.AuthState
import com.example.atomic_cinema.stateClasses.CinemaState
import com.example.atomic_cinema.utils.LoadingScreen
import com.example.atomic_cinema.utils.Support
import com.example.atomic_cinema.viewModel.AuthViewModel
import com.example.atomic_cinema.viewModel.CinemaViewModel

@Composable
fun CinemasScreen(
    navHostController: NavHostController,
    viewModelCinema: CinemaViewModel = hiltViewModel(),
    viewModelAuth: AuthViewModel = hiltViewModel(),
) {

    val context = LocalContext.current
    val stateCinema = viewModelCinema.state
    val stateAuth = viewModelAuth.state
    val listState = viewModelCinema.listStateCinema

    LaunchedEffect(viewModelCinema, context){
        viewModelCinema.cinemaResults.collect{ result ->
            when(result){
                is CinemaResults.OK -> {

                }
                is CinemaResults.UnknownError -> {
                    Toast.makeText(
                        context, "Неизвестная ошибка.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is CinemaResults.Unauthorized -> {
                    Toast.makeText(
                        context, "Авторизируйтесь в системе.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navHostController.navigate(NavRoutes.Authorization.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                else -> {

                }
            }
        }
    }

    LaunchedEffect(viewModelAuth, context){
        viewModelAuth.authResults.collect{ result ->
            when(result){
                else -> {}
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        if(stateCinema.isLoading){
            LoadingScreen()
        }else{
            ListCinema(listCinema = listState, stateAuth, navHostController)
        }
    }

}


@Composable
fun ListCinema(listCinema : List<CinemaState>, stateA: AuthState, navHostController: NavHostController){
    LazyColumn{
        items(items = listCinema){ cinema ->
            ElementListCinema(cinema = cinema, stateA = stateA, navHostController = navHostController)
        }
    }
}

@Composable
fun ElementListCinema(cinema : CinemaState, stateA : AuthState, navHostController: NavHostController){
    Card(
        modifier = if(stateA.role != "employee") Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colors.primary, Color.White)))
            .padding(start = 10.dp, end = 10.dp, top = 5.dp)
        else Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colors.primary, Color.White)))
            .padding(start = 10.dp, end = 10.dp, top = 5.dp)
            .clickable {
                navHostController.navigate(NavRoutes.Job.route)
                Support.copyCinemaState = cinema
            },
        shape = RoundedCornerShape(topStart = 20.dp, bottomEnd = 20.dp),
        elevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            Box(modifier = Modifier
                .width(200.dp)
                .background(
                    MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(topStart = 20.dp, bottomEnd = 20.dp)
                )
                .clip(RoundedCornerShape(topStart = 20.dp, bottomEnd = 20.dp)),
                contentAlignment = Alignment.TopStart
            ){
                Text(
                    modifier = Modifier.width(190.dp),
                    textAlign = TextAlign.Center,
                    text = cinema.addressCinema.substringBefore("."),
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(modifier = Modifier.padding(10.dp)){
                Column(verticalArrangement = Arrangement.SpaceEvenly){
                    Text(text = cinema.addressCinema.substringAfter(". "))
                    Text(text = "Рабочие номера:")
                    cinema.numbersPhone.forEach {
                        Text(text = it)
                    }

                }
            }
        }
    }
}