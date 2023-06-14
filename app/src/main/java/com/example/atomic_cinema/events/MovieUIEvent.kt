package com.example.atomic_cinema.events

import com.example.atomic_cinema.stateClasses.MovieState

sealed class MovieUIEvent {
    data class IdMovieChanged(val value: Int) : MovieUIEvent()
    data class NameMovieChanged(val value: String) : MovieUIEvent()
    data class AgeRatingChanged(val value: String) : MovieUIEvent()
    data class DescriptionChanged(val value: String) : MovieUIEvent()
    data class DirectorChanged(val value: String) : MovieUIEvent()
    data class LinkImageChanged(val value: String) : MovieUIEvent()
    data class YearOfIssueChanged(val value: String) : MovieUIEvent()
    data class GenresChanged(val value: MutableList<String>) : MovieUIEvent()
    data class DurationChanged(val value: Int) : MovieUIEvent()
    data class ShowDetailsMovieChanged(val value: Boolean) : MovieUIEvent()
    data class MovieChanged(val value: MovieState) : MovieUIEvent()
    object AddMovie : MovieUIEvent()
    object DeleteMovie : MovieUIEvent()
    object UpdateMovie : MovieUIEvent()
}