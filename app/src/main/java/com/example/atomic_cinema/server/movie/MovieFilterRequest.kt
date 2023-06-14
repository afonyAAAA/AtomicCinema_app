package com.example.atomic_cinema.server.movie

data class MovieFilterRequest(
    val listGenre: List<String> = listOf(),
    val ageRating: Int?,
    val yearOfIssue : Int?
)
