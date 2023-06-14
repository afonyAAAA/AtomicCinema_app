package com.example.atomic_cinema.server.movie


sealed class MovieResult<T>(val data : T? = null){
    class OK<T>(data: T? = null) : MovieResult<T>(data)
    class UnknownError<T>(data: T? = null) : MovieResult<T>(data)
    class Unauthorized<T>(data: T? = null) : MovieResult<T>(data)
    class MovieIsAdded<T>(data: T? = null) : MovieResult<T>(data)
    class MovieIsUpdated<T>(data: T? = null) : MovieResult<T>(data)
    class MovieIsDeleted<T>(data: T? = null) : MovieResult<T>(data)

}
