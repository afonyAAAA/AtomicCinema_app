package com.example.atomic_cinema.server.seance

interface SeanceRepository {

    suspend fun getSeanceMovie(idMovie : Int) : SeanceResult<List<SeanceResponse>>

    suspend fun getSeanceCinema(request: SeanceCinemaRequest) : SeanceResult<List<SeanceResponse>>

    suspend fun addSeance(request: SeanceAddRequest) : SeanceResult<Unit>

    suspend fun updateSeance(request: SeanceUpdateRequest) : SeanceResult<Unit>

    suspend fun deleteSeance(request: SeanceDeleteRequest) : SeanceResult<Unit>

}