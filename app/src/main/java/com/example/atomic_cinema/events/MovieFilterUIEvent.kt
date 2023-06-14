package com.example.atomic_cinema.events

import androidx.compose.ui.text.input.TextFieldValue

sealed class MovieFilterUIEvent {
    data class ExpandedTextFieldGenresChanged(val value: Boolean) : MovieFilterUIEvent()
    data class ExpandedTextFieldYearChanged(val value: Boolean) : MovieFilterUIEvent()
    data class ExpandedTextFieldAgeChanged(val value: Boolean) : MovieFilterUIEvent()
    data class AgeRatingChanged(val value: Int) : MovieFilterUIEvent()
    data class YearOfIssueChanged(val value: Int) : MovieFilterUIEvent()
    data class GenreChanged(val index : Int,  val state : Boolean) : MovieFilterUIEvent()
    data class FilterIsChanged(val value : Boolean) : MovieFilterUIEvent()
    data class SearchTextChanged(val value : TextFieldValue) : MovieFilterUIEvent()
    data class ApplyFilter(val value: Boolean) : MovieFilterUIEvent()
    object ClearSelectedGenres : MovieFilterUIEvent()
}