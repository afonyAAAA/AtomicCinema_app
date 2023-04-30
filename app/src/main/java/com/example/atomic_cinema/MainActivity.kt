package com.example.atomic_cinema

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.atomic_cinema.events.AuthUIEvent
import com.example.atomic_cinema.navigation.BottomNavItem
import com.example.atomic_cinema.navigation.CinemaNavHost
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.ui.theme.AtomicCinemaTheme
import com.example.atomic_cinema.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AtomicCinemaTheme {
                val scaffoldState = rememberScaffoldState(DrawerState(DrawerValue.Closed))
                val navHostController = rememberNavController()
                ScaffoldContainer(navHostController = navHostController, scaffoldState = scaffoldState)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScaffoldContainer(navHostController: NavHostController, scaffoldState: ScaffoldState, viewModel : AuthViewModel = hiltViewModel()) {

    val state = viewModel.state
    val context = LocalContext.current

    var displayPattern by remember { mutableStateOf(3)}
    val scope = rememberCoroutineScope()

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
            Text(text = "AtomicCinema")
        }
    }

    @Composable
    fun BottomBar(navHostController: NavHostController) {

        val items = listOf(
            BottomNavItem.Main,
            BottomNavItem.Seances,
            BottomNavItem.Cinemas,
            BottomNavItem.Movies
        )

        BottomNavigation {
            val navBackStackEntry by navHostController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                BottomNavigationItem(
                    icon = { Icon(imageVector = item.icon, null) },
                    label = { Text(text = item.title) },
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
            Column{

                LaunchedEffect(viewModel, context) {
                    viewModel.authResults.collect { result ->
                        when (result) {
                            is AuthResult.Authorized -> {

                            }
                            is AuthResult.Unauthorized -> {
                                viewModel.onEvent(AuthUIEvent.Authorized(false))
                            }
                        }
                    }
                }

                displayPattern = if (state.authorized) {
                    when (state.role == "customer") {
                        true -> {
                            1
                        }
                        false -> {
                            2
                        }
                    }
                } else {
                    3
                }

                val navBackStackEntry by navHostController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(currentRoute){
                    scaffoldState.drawerState.close()
                }

                if(state.isLoading){
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        CircularProgressIndicator()
                    }
                }else{
                    if(displayPattern != 3){
                        IconButton(onClick = {
                            navHostController.navigate(NavRoutes.UserProfile.route)
                        }) {
                            Text(text = "Профиль")
                        }
                        IconButton(onClick = {
                            viewModel.onEvent(AuthUIEvent.GoOut)
                            navHostController.navigate(NavRoutes.Authorization.route)
                        }) {
                            Text(text = "Выйти")
                        }
                    }

                    when(displayPattern){
                        1 -> {

                        }
                        2 ->{

                        }
                        3 ->{
                            IconButton(onClick = {
                                navHostController.navigate(NavRoutes.Authorization.route)
                            }) {
                                Text(text = "Войти")
                            }
                        }
                    }
                }
            }
        },
        topBar = { TopBar() },
        bottomBar = { BottomBar(navHostController = navHostController) },
        content = {CinemaNavHost(navController = navHostController)})
}


