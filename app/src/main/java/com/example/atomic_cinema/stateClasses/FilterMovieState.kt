package com.example.atomic_cinema.stateClasses

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.ui.text.input.TextFieldValue

data class FilterMovieState(
    val isLoading : Boolean = false,
    val selectedGenreList : MutableList<GenresState> = mutableListOf(),
    val selectedAgeRating : Int = -1,
    val selectedYearIssue : Int = 0,
    val expandedListGenreTextField : Boolean = false,
    val expandedListYearTextField : Boolean = false,
    val expandedListAgeTextField : Boolean = false,
    val filterIsActivated : Boolean = false,
    val searchText : TextFieldValue = TextFieldValue(""),
    val animateVisibilityFilter : MutableTransitionState<Boolean> = MutableTransitionState(false).apply {
        targetState = false
    }
)

data class GenresState(
    val id : Int = 0,
    val nameGenre : String = "",
    val isChoice : Boolean = false
)