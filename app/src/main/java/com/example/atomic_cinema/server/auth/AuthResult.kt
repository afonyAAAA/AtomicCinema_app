package com.example.atomic_cinema.server.auth

sealed class AuthResult<T>(val data : T? = null){
    class Authorized<T>(data: T? = null) : AuthResult<T>(data)
    class Unauthorized<T>(data: T? = null) : AuthResult<T>(data)
    class UnknownError<T>(data: T? = null) : AuthResult<T>(data)
    class UserIsAlreadyExist<T>(data: T? = null) : AuthResult<T>(data)
    class NotValidPassword<T>(data: T? = null) : AuthResult<T>(data)
    class UserNotFound<T>(data: T? = null) : AuthResult<T>(data)
    class CameOut<T>(data: T? = null) : AuthResult<T>(data)
    class Registered<T>(data: T? = null) : AuthResult<T>(data)
    class OK<T>(data: T? = null) : AuthResult<T>(data)
}
