package com.example.atomic_cinema.server.cinema

import android.content.SharedPreferences
import retrofit2.HttpException

class CinemaRepositoryImpl(
    private val api: CinemaApi,
    private val prefs : SharedPreferences
    ) : CinemaRepository {
    override suspend fun getAllCinema(): CinemaResults<List<CinemaResponse>> {
        return try{
            val response = api.getAllCinema()
            CinemaResults.OK(response)
        }catch (e : HttpException){
            CinemaResults.UnknownError()
        }catch (e : Exception){
            CinemaResults.UnknownError()
        }
    }

    override suspend fun getRevenueReport(): CinemaResults<RevenueResponse> {
        return try{
            val token = prefs.getString("jwt", null) ?: return CinemaResults.Unauthorized()
            val response = api.getFormedRevenueReport(
                "Bearer $token"
            )
            CinemaResults.OK(response)
        }catch (e : HttpException){
            if(e.code() == 401){
                CinemaResults.Unauthorized()
            }else if(e.code() == 404){
                CinemaResults.NotRevenue()
            }else{
                CinemaResults.UnknownError()
            }
        }catch (e : Exception){
            CinemaResults.UnknownError()
        }
    }

    override suspend fun getHallsCinema(request: HallCinemaRequest): CinemaResults<List<HallsResponse>> {
        return try{
            val token = prefs.getString("jwt", null) ?: return CinemaResults.Unauthorized()
            val result = api.getHallsCinema(
                "Bearer $token",
                request
            )
            CinemaResults.OK(result)
        }catch (e : HttpException){
            if(e.code() == 401){
                CinemaResults.Unauthorized()
            }else{
                CinemaResults.UnknownError()
            }
        }catch (e : Exception){
            CinemaResults.UnknownError()
        }
    }
}