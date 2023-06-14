package com.example.atomic_cinema.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.atomic_cinema.stateClasses.CinemaState
import com.example.atomic_cinema.stateClasses.FilterMovieState
import com.example.atomic_cinema.stateClasses.MovieState
import com.example.atomic_cinema.stateClasses.SeanceState

object Support{
    var copyMovie by mutableStateOf(MovieState())
    var copyMovieUpdate by mutableStateOf(MovieState())
    var copyFilterUpdate by mutableStateOf(FilterMovieState())
    var copySeance by mutableStateOf(SeanceState())
    var copySeanceUpdate by mutableStateOf(SeanceState())
    var viewBalance by mutableStateOf(false)
    var copyCinemaState by mutableStateOf(CinemaState())
}