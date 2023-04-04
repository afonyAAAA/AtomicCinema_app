package com.example.atomic_cinema.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.atomic_cinema.screens.*


sealed class NavRoutes(val route: String){
    object Main : NavRoutes("main_screen")
    object Authorization : NavRoutes("authorization_screen")
    object Registration : NavRoutes("registration_screen")
    object UserProfile : NavRoutes("profile_screen")
    object Tickets : NavRoutes("tickets_screen")
    object Seances : NavRoutes("seances_screen")
    object Movies : NavRoutes("movie_screen")
    object Cinemas: NavRoutes("cinema_screen")
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector){
    object Seances: BottomNavItem(
       route = NavRoutes.Seances.route,
       title = "Сеансы",
       icon = Icons.Rounded.LocationOn)
    object Cinemas: BottomNavItem(
        route = NavRoutes.Cinemas.route,
        title = "Кинотеатры",
        icon = Icons.Rounded.ShoppingCart)
    object Movies: BottomNavItem(
        route = NavRoutes.Movies.route,
        title = "Фильмы",
        icon = Icons.Rounded.Phone
    )
    object Main: BottomNavItem(
        route = NavRoutes.Main.route,
        title = "Главная",
        icon = Icons.Rounded.Place
    )
}

@Composable
fun CinemaNavHost(navController: NavHostController){
    NavHost(navController = navController, startDestination = NavRoutes.Main.route){
        composable(NavRoutes.Main.route){
           MainScreen(navHostController = navController)
        }
        composable(NavRoutes.Seances.route){
            SeancesScreen(navHostController = navController)
        }
        composable(NavRoutes.Cinemas.route){
            CinemasScreen(navHostController = navController)
        }
        composable(NavRoutes.Movies.route){
            MoviesScreen(navHostController = navController)
        }
        composable(NavRoutes.Tickets.route){
            TicketsScreen(navHostController = navController)
        }
        composable(NavRoutes.Authorization.route){
            AuthorizationScreen(navHostController = navController)
        }
        composable(NavRoutes.Registration.route){
            RegistrationScreen(navHostController = navController)
        }
        composable(NavRoutes.UserProfile.route){
            ProfileScreen(navHostController = navController)
        }
    }
}