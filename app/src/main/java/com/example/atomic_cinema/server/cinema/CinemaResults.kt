package com.example.atomic_cinema.server.cinema



sealed class CinemaResults<T>(val data : T? = null) {
    class OK<T>(data: T? = null) : CinemaResults<T>(data)
    class Unauthorized<T>(data: T? = null) : CinemaResults<T>(data)
    class UnknownError<T>(data: T? = null) : CinemaResults<T>(data)
    class NotRevenue<T>(data: T? = null) : CinemaResults<T>(data)
}