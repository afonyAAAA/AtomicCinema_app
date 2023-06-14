package com.example.atomic_cinema.server.movie

interface MovieRepository {

    suspend fun getAllMovie() : MovieResult<List<MovieResponse>>

    suspend fun getFilteredMovie(
        selectedListGenre : List<String>,
        selectedAgeRating : Int? = null,
        selectedYear : Int? = null
    ) : MovieResult<List<MovieResponse>>

    suspend fun getAllGenre() : MovieResult<List<GenreResponse>>

    suspend fun addMovie(request: MovieAddRequest) : MovieResult<Unit>

    suspend fun deleteMovie(request: MovieDeleteRequest) : MovieResult<Unit>

    suspend fun updateMovie(request: MovieUpdateRequest) : MovieResult<Unit>

}