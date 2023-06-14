package com.example.atomic_cinema.stateClasses

data class MovieState(
    val isLoading : Boolean = false,
    val idMovie : Int = 0,
    val nameMovie : String = "",
    val ageRating : String = "",
    val description : String = "",
    val director : String = "",
    val duration : Int = 0,
    val linkImage : String = "",
    val yearOfIssue : String = "",
    val genreList : MutableList<String> = mutableListOf(),
    val showDetailsMovie : Boolean = false
)
