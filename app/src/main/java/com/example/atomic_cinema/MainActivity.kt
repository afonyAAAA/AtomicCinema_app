@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.example.atomic_cinema

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.atomic_cinema.events.AuthUIEvent
import com.example.atomic_cinema.navigation.BottomNavItem
import com.example.atomic_cinema.navigation.CinemaNavHost
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.stateClasses.CinemaState
import com.example.atomic_cinema.stateClasses.FilterMovieState
import com.example.atomic_cinema.stateClasses.MovieState
import com.example.atomic_cinema.stateClasses.SeanceState
import com.example.atomic_cinema.ui.theme.AtomicCinemaTheme
import com.example.atomic_cinema.utils.AppFinisher
import com.example.atomic_cinema.utils.FinishListener
import com.example.atomic_cinema.utils.Support
import com.example.atomic_cinema.utils.Users.*
import com.example.atomic_cinema.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class MainActivity : ComponentActivity(), FinishListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppFinisher.setFinishListener(this)

        setContent {
            AtomicCinemaTheme {

                val navHostController: NavHostController = rememberNavController()
                val scaffoldState = rememberScaffoldState(DrawerState(DrawerValue.Closed))

                ScaffoldContainer(
                    navHostController = navHostController,
                    scaffoldState = scaffoldState
                )
            }
        }
    }

    override fun finishApp() {
        finish()
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo?.isConnectedOrConnecting == true
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScaffoldContainer(
    navHostController: NavHostController,
    scaffoldState: ScaffoldState,
    viewModelAuth: AuthViewModel = hiltViewModel()
) {

    val state = viewModelAuth.state
    val context = LocalContext.current
    var displayPattern by remember { mutableStateOf(ANONYMOUS)}
    val scope = rememberCoroutineScope()

    var isInternetConnected by remember {mutableStateOf(isInternetAvailable(context))}

    scope.launch {
        while (true){
            delay(5000L)
            isInternetConnected = isInternetAvailable(context)
        }
    }
    if(!isInternetConnected){
        AlertDialog(
            onDismissRequest = {
                AppFinisher.finishApp()
            },
            title = {
                Text(text = "Ошибка интернет соединения")
            },
            text = {
                Text(text = "Проверьте интернет соединение и попробуйте попытку снова")
            },
            confirmButton = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    Button(onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }) {
                        Text(text = "Повторить попытку")
                    }
                }
            },
            dismissButton = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    Button(onClick = {
                        AppFinisher.finishApp()
                    }) {
                        Text(text = "Закрыть приложение")
                    }
                }
            }


        )
    }else{
        @Composable
        fun TopBar() {
            TopAppBar {
                IconButton(onClick = {
                    scope.launch {
                        scaffoldState.drawerState.open()
                    }
                }) {
                    Icon(Icons.Rounded.Menu, null)
                }
                Text(text = "Cinema")
            }
        }

        @Composable
        fun BottomBar(navHostController: NavHostController) {

            val items = when(displayPattern){
                CUSTOMER -> {
                    listOf(
                        BottomNavItem.Main,
                        BottomNavItem.Tickets,
                        BottomNavItem.Cinemas,
                        BottomNavItem.Movies
                    )
                }
                ADMIN -> {
                    listOf(
                        BottomNavItem.Main,
                        BottomNavItem.Cinemas,
                        BottomNavItem.Movies
                    )
                }
                ANONYMOUS -> {
                    listOf(
                        BottomNavItem.Main,
                        BottomNavItem.Cinemas,
                        BottomNavItem.Movies
                    )
                }
                EMPLOYEE -> {
                    listOf(
                        BottomNavItem.Cinemas,
                    )
                }
            }


            BottomNavigation {
                val navBackStackEntry by navHostController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    BottomNavigationItem(
                        icon = { Icon(imageVector = item.icon, null)},
                        label = { Text(text = item.title, fontSize = 10.sp) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(0.6f),
                        alwaysShowLabel = true,
                        selected = currentRoute == item.route,
                        onClick = {
                            navHostController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
        Scaffold(
            scaffoldState = scaffoldState,
            drawerContent = {
                Column(Modifier
                    .fillMaxSize()
                    .padding(10.dp)){
                    LaunchedEffect(viewModelAuth, context) {
                        viewModelAuth.authResults.collect { result ->
                            when (result) {
                                is AuthResult.Authorized -> {
                                    viewModelAuth.onEvent(AuthUIEvent.Authorized(true))
                                }
                                is AuthResult.Unauthorized -> {
                                    viewModelAuth.onEvent(AuthUIEvent.Authorized(false))
                                }
                                else -> {

                                }
                            }
                        }
                    }

                    LaunchedEffect(state.authorized){
                        displayPattern = if (state.authorized) {
                            when (state.role) {
                                "customer" -> {
                                    CUSTOMER
                                }
                                "employee" -> {
                                    EMPLOYEE
                                }
                                "admin" -> {
                                    ADMIN
                                }
                                else -> {
                                    ANONYMOUS
                                }
                            }
                        }else{
                            ANONYMOUS
                        }
                    }

                    if(state.isLoading){
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                            CircularProgressIndicator()
                        }
                    }else{

                        when(displayPattern){
                            CUSTOMER -> {
                                Column(Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                ) {
                                    ListItemsCustomer(
                                        scaffoldState = scaffoldState,
                                        scope = scope,
                                        viewModelAuth = viewModelAuth,
                                        navHostController = navHostController)
                                }
                            }
                            ANONYMOUS -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart){
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clickable {
                                                scope.launch {
                                                    scaffoldState.drawerState.close()
                                                }
                                                navHostController.navigate(NavRoutes.Authorization.route)
                                            },
                                        elevation = 10.dp,
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 5.dp, bottom = 10.dp)) {
                                            Icon(Icons.Rounded.Login, "")
                                            Spacer(Modifier.width(10.dp))
                                            Text(text = "Войти")
                                        }
                                    }
                                }
                            }
                            else ->{
                                ListItemsEmployeeJob(
                                    scaffoldState = scaffoldState,
                                    scope = scope,
                                    viewModelAuth = viewModelAuth,
                                    navHostController = navHostController
                                )
                            }
                        }
                    }
                }
            },
            topBar = { TopBar() },
            bottomBar = { BottomBar(navHostController = navHostController)},
            content = {
                CinemaNavHost(navController = navHostController, state)
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListItemsCustomer(
    scaffoldState : ScaffoldState,
    navHostController : NavHostController,
    scope : CoroutineScope,
    viewModelAuth : AuthViewModel
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .height(100.dp)
            .clickable {
                scope.launch {
                    Support.viewBalance = false
                    scaffoldState.drawerState.close()
                }
                navHostController.navigate(NavRoutes.UserProfile.route)
            },
        elevation = 5.dp,
        shape = RoundedCornerShape(20.dp)

    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 5.dp)) {
            Icon(Icons.Outlined.AccountBox, "")
            Spacer(Modifier.width(10.dp))
            Text(text = "Профиль")
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .height(100.dp)
            .clickable {
                scope.launch {
                    Support.viewBalance = true
                    scaffoldState.drawerState.close()
                }

                navHostController.navigate(NavRoutes.UserProfile.route)
            },
        elevation = 5.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 5.dp)) {
            Icon(Icons.Rounded.AccountBalanceWallet, "")
            Spacer(Modifier.width(10.dp))
            Text(text = "Баланс")
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .height(100.dp)
            .clickable {
                scope.launch {
                    scaffoldState.drawerState.close()
                }
                viewModelAuth.onEvent(AuthUIEvent.GoOut)
                navHostController.navigate(NavRoutes.Authorization.route)
            },
        elevation = 5.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 5.dp)) {
            Icon(Icons.Filled.Logout, "")
            Spacer(Modifier.width(10.dp))
            Text(text = "Выход")
        }
    }
}

@Composable
fun ListItemsEmployeeJob(
    scaffoldState : ScaffoldState,
    navHostController : NavHostController,
    scope : CoroutineScope,
    viewModelAuth : AuthViewModel
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .height(100.dp)
            .clickable {
                scope.launch {
                    Support.copyMovieUpdate = MovieState()
                    Support.copyFilterUpdate = FilterMovieState()
                    Support.copySeanceUpdate = SeanceState()
                    Support.copyCinemaState = CinemaState()
                    scaffoldState.drawerState.close()
                }

                navHostController.navigate(NavRoutes.Job.route)
            },
        elevation = 5.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 5.dp)) {
            Icon(Icons.Filled.HolidayVillage, "")
            Spacer(Modifier.width(10.dp))
            Text(text = "Работа")
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .height(100.dp)
            .clickable {
                scope.launch {
                    scaffoldState.drawerState.close()
                }
                viewModelAuth.onEvent(AuthUIEvent.GoOut)
                navHostController.navigate(NavRoutes.Authorization.route)
            },
        elevation = 5.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 5.dp)) {
            Icon(Icons.Filled.Logout, "")
            Spacer(Modifier.width(10.dp))
            Text(text = "Выход")
        }
    }
}

