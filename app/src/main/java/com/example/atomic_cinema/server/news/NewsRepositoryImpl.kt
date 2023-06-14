package com.example.atomic_cinema.server.news

import retrofit2.HttpException

class NewsRepositoryImpl(
    private val api: NewsApi
) : NewsRepository {
    override suspend fun getTopHeadlines(): NewsResults<NewsResponse> {
        return try{
            val response = api.getTopHeadlines()
            NewsResults.OK(response)
        }catch (e : HttpException){
            if(e.code() == 401){
                NewsResults.Unauthorized()
            }else{
                e.printStackTrace()
                NewsResults.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            NewsResults.UnknownError()
        }
    }

}