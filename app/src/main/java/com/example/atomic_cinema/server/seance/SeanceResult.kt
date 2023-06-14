package com.example.atomic_cinema.server.seance


sealed class SeanceResult <T>(val data : T? = null){
    class NotCorrectDateOrTime<T>(data : T? = null) : SeanceResult<T>(data)
    class ConflictSeances<T>(data : T? = null) : SeanceResult<T>(data)
    class Unauthorized<T>(data : T? = null) : SeanceResult<T>(data)
    class NotFoundSeances<T>(data : T? = null) : SeanceResult<T>(data)
    class OK<T>(data : T? = null) : SeanceResult<T>(data)
    class UnknownError<T>(data : T? = null) : SeanceResult<T>(data)
    class SeanceIsAdded<T>(data : T? = null) : SeanceResult<T>(data)
    class SeanceIsUpdated<T>(data : T? = null) : SeanceResult<T>(data)
    class SeanceIsDeleted<T>(data : T? = null) : SeanceResult<T>(data)
}


