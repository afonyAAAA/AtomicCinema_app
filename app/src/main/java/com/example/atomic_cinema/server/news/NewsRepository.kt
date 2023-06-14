package com.example.atomic_cinema.server.news

interface NewsRepository {

    suspend fun getTopHeadlines() : NewsResults<NewsResponse>

}