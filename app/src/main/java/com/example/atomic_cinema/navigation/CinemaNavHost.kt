package com.example.atomic_cinema.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.atomic_cinema.screens.*
import com.example.atomic_cinema.stateClasses.AuthState
import com.example.atomic_cinema.stateClasses.MovieState


sealed class NavRoutes(val route: String){
    object Main : NavRoutes("main_screen")
    object Authorization : NavRoutes("authorization_screen")
    object Registration : NavRoutes("registration_screen")
    object UserProfile : NavRoutes("profile_screen")
    object Tickets : NavRoutes("tickets_screen")
    object UserTickets : NavRoutes("seances_screen")
    object Movies : NavRoutes("movie_screen")
    object Cinemas: NavRoutes("cinema_screen")
    object Job: NavRoutes("job_screen")
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector){
    object Tickets: BottomNavItem(
       route = NavRoutes.UserTickets.route,
       title = "Билеты",
       icon = Icons.Rounded.ConfirmationNumber)
    object Cinemas: BottomNavItem(
        route = NavRoutes.Cinemas.route,
        title = "Кинотеатры",
        icon = Icons.Rounded.SmartDisplay)
    object Movies: BottomNavItem(
        route = NavRoutes.Movies.route,
        title = "Фильмы",
        icon = Icons.Outlined.Movie
    )
    object Main: BottomNavItem(
        route = NavRoutes.Main.route,
        title = "Главная",
        icon = Icons.Outlined.Home
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CinemaNavHost(navController: NavHostController, authState: AuthState){
    NavHost(
        navController = navController,
        startDestination = if(authState.role != "employee") NavRoutes.Main.route else NavRoutes.Cinemas.route,
        modifier = Modifier.padding(bottom = 45.dp)) {

        composable(NavRoutes.Main.route) {
            MainScreen(navHostController = navController)
        }
        composable(NavRoutes.UserTickets.route) {
            TicketsUserScreen(navHostController = navController)
        }
        composable(NavRoutes.Cinemas.route) {
            CinemasScreen(navHostController = navController)
        }
        composable(NavRoutes.Movies.route) {
            MoviesScreen(navHostController = navController)
        }
        composable(NavRoutes.Tickets.route) {
            TicketsScreen(navHostController = navController)
        }
        composable(NavRoutes.Authorization.route) {
            AuthorizationScreen(navHostController = navController)
        }
        composable(NavRoutes.Registration.route) {
            RegistrationScreen(navHostController = navController)
        }
        composable(NavRoutes.UserProfile.route) {
            ProfileScreen(navHostController = navController)
        }
        composable(NavRoutes.Job.route) {
            JobScreen(navHostController = navController)
        }
    }
}