package com.example.atomic_cinema.server.news

import com.example.atomic_cinema.server.movie.MovieResult

sealed class NewsResults<T>(val data : T? = null) {
    class OK<T>(data: T? = null) : NewsResults<T>(data)
    class UnknownError<T>(data: T? = null) : NewsResults<T>(data)
    class Unauthorized<T>(data: T? = null) : NewsResults<T>(data)
}