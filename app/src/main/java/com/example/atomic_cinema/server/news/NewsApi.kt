package com.example.atomic_cinema.server.news

import retrofit2.http.GET

interface NewsApi {


    @GET("v2/everything?q=Фильм&apiKey=310f1b56ed3e4f60b68464c2b8b5275a")
    suspend fun getTopHeadlines() : NewsResponse

}