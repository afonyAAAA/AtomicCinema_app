package com.example.atomic_cinema.server.movie

data class MovieUpdateRequest(
    val id : Int,
    val ageRating : String,
    val description : String,
    val director : String,
    val duration : Int,
    val linkImage : String,
    val nameMovie : String,
    val yearOfIssue : String,
    val listGenre : List<Int>
)
