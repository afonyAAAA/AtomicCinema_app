package com.example.atomic_cinema.server.seance

import android.content.SharedPreferences
import retrofit2.HttpException

class SeanceRepositoryImpl(
    private val api : SeanceApi,
    private val prefs : SharedPreferences
) : SeanceRepository {

    override suspend fun getSeanceMovie(idMovie: Int): SeanceResult<List<SeanceResponse>> {
        return try{
            val result = api.getSeanceMovie(SeanceMovieRequest(
                idMovie = idMovie
            ))
            SeanceResult.OK(result)
        }catch (e : HttpException){
            if(e.code() == 404){
                e.printStackTrace()
                SeanceResult.NotFoundSeances()
            }else{
                e.printStackTrace()
                SeanceResult.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            SeanceResult.UnknownError()
        }
    }

    override suspend fun getSeanceCinema(request: SeanceCinemaRequest): SeanceResult<List<SeanceResponse>> {
        return try{
            val result = api.getSeanceCinema(
                request
            )
            SeanceResult.OK(result)
        }catch (e : HttpException){
            SeanceResult.UnknownError()
        }catch (e : Exception){
            SeanceResult.UnknownError()
        }
    }

    override suspend fun addSeance(request: SeanceAddRequest): SeanceResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return SeanceResult.Unauthorized()
            api.addSeance(
                "Bearer $token",
                request
            )
            SeanceResult.SeanceIsAdded()
        }catch (e : HttpException){
            if(e.code() == 401){
                SeanceResult.Unauthorized()
            }else if(e.code() == 400){
                SeanceResult.NotCorrectDateOrTime()
            }else if(e.code() == 409){
                SeanceResult.ConflictSeances()
            }else{
                SeanceResult.UnknownError()
            }
        }catch (e : Exception){
            SeanceResult.UnknownError()
        }
    }

    override suspend fun updateSeance(request: SeanceUpdateRequest): SeanceResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return SeanceResult.Unauthorized()
            api.updateSeance(
                "Bearer $token",
                request
            )
            SeanceResult.SeanceIsUpdated()
        }catch (e : HttpException){
            if(e.code() == 401){
                SeanceResult.Unauthorized()
            }else if(e.code() == 400){
                SeanceResult.NotCorrectDateOrTime()
            }else if(e.code() == 409){
                SeanceResult.ConflictSeances()
            }else{
                SeanceResult.UnknownError()
            }
        }catch (e : Exception){
            SeanceResult.UnknownError()
        }
    }

    override suspend fun deleteSeance(request: SeanceDeleteRequest): SeanceResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return SeanceResult.Unauthorized()
            api.deleteSeance(
                "Bearer $token",
                request
            )
            SeanceResult.SeanceIsDeleted()
        }catch (e : HttpException){
            if(e.code() == 401){
                SeanceResult.Unauthorized()
            }else{
                e.printStackTrace()
                SeanceResult.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            SeanceResult.UnknownError()
        }
    }
}