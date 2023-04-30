package com.example.atomic_cinema.server.profile

sealed class ProfileResult <T>(val data : T? = null){
    class Authorized<T>(data : T? = null) : ProfileResult<T>(data)
    class Shown<T>(data : T? = null) : ProfileResult<T>(data)
    class Unauthorized<T>(data : T? = null) : ProfileResult<T>(data)
    class UnknownError<T>(data : T? = null) : ProfileResult<T>(data)
    class Edited<T>(data : T? = null) : ProfileResult<T>(data)
}