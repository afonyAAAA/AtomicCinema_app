package com.example.atomic_cinema.server.movie

import android.content.SharedPreferences
import retrofit2.HttpException

class MovieRepositoryImpl(
    private val api : MovieApi,
    private val prefs : SharedPreferences
) : MovieRepository {
    override suspend fun getAllMovie(): MovieResult<List<MovieResponse>> {
        return try{
            val response = api.getAllMovie()
            MovieResult.OK(response)
        }catch (e : HttpException){
            if(e.code() == 401){
                MovieResult.Unauthorized()
            }else{
                MovieResult.UnknownError()
            }
        }catch (e : Exception){
            MovieResult.UnknownError()
        }
    }



    override suspend fun getFilteredMovie(
        selectedListGenre: List<String>,
        selectedAgeRating: Int?,
        selectedYear: Int?
    ): MovieResult<List<MovieResponse>> {
        return try{
            val response = api.getFilteredMovie(
                MovieFilterRequest(
                    listGenre = selectedListGenre,
                    yearOfIssue = selectedYear,
                    ageRating = selectedAgeRating
                )
            )
            MovieResult.OK(response)
        }catch (e : HttpException){
            if(e.code() == 401){
                MovieResult.Unauthorized()
            }else{
                MovieResult.UnknownError()
            }
        }catch (e : Exception){
            MovieResult.UnknownError()
        }
    }

    override suspend fun getAllGenre(): MovieResult<List<GenreResponse>> {
        return try{
            val response = api.getAllGenre()
            MovieResult.OK(response)
        }catch (e : HttpException){
            if(e.code() == 401){
                MovieResult.Unauthorized()
            }else{
                e.printStackTrace()
                MovieResult.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            MovieResult.UnknownError()
        }
    }

    override suspend fun addMovie(request: MovieAddRequest): MovieResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return MovieResult.Unauthorized()
            api.addMovie(
                "Bearer $token",
                request
            )
            MovieResult.MovieIsAdded()
        }catch (e : HttpException){
            if(e.code() == 401){
                MovieResult.Unauthorized()
            }else{
                MovieResult.UnknownError()
            }
        }catch (e : Exception){
            MovieResult.UnknownError()
        }
    }

    override suspend fun deleteMovie(request: MovieDeleteRequest): MovieResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return MovieResult.Unauthorized()
            api.deleteMovie(
                "Bearer $token",
                request
            )
            MovieResult.MovieIsDeleted()
        }catch (e : HttpException){
            if(e.code() == 401){
                MovieResult.Unauthorized()
            }else{
                MovieResult.UnknownError()
            }
        }catch (e : Exception){
            MovieResult.UnknownError()
        }
    }

    override suspend fun updateMovie(request: MovieUpdateRequest): MovieResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return MovieResult.Unauthorized()
            val result = api.updateMovie(
                "Bearer $token",
                request
            )
            MovieResult.MovieIsUpdated(result)
        }catch (e : HttpException){
            if(e.code() == 401){
                MovieResult.Unauthorized()
            }else{
                MovieResult.UnknownError()
            }
        }catch (e : Exception){
            MovieResult.UnknownError()
        }
    }

}