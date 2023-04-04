package com.example.atomic_cinema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.atomic_cinema.navigation.BottomNavItem
import com.example.atomic_cinema.navigation.CinemaNavHost
import com.example.atomic_cinema.ui.theme.AtomicCinemaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AtomicCinemaTheme() {
                val scaffoldState = rememberScaffoldState(DrawerState(DrawerValue.Closed))
                var navHostController = rememberNavController()
                ScaffoldContainer(navHostController = navHostController, scaffoldState = scaffoldState)
            }
        }
    }
}


@Composable
fun ScaffoldContainer(navHostController: NavHostController, scaffoldState: ScaffoldState) {

    val scope = rememberCoroutineScope()

    @Composable
    fun TopBar(navHostController: NavHostController) {
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
            Column() {
                IconButton(onClick = { /*TODO*/ }) {
                    Text(text = "Профиль")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Text(text = "Настройки")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Text(text = "Выйти")
                }
            }
        },
        topBar = { TopBar(navHostController = navHostController) },
        bottomBar = { BottomBar(navHostController = navHostController) },
        content = {CinemaNavHost(navController = navHostController)})
}


