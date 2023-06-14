package com.example.atomic_cinema.server.profile

sealed class ProfileResult <T>(val data : T? = null){
    class Shown<T>(data : T? = null) : ProfileResult<T>(data)
    class Unauthorized<T>(data : T? = null) : ProfileResult<T>(data)
    class UnknownError<T>(data : T? = null) : ProfileResult<T>(data)
    class Edited<T>(data : T? = null) : ProfileResult<T>(data)
    class MoneyOperationIsSuccessful<T>(data : T? = null) : ProfileResult<T>(data)
    class FewReplenish<T>(data : T? = null) : ProfileResult<T>(data)
    class NotFoundTicketsForMonth<T>(data : T? = null) : ProfileResult<T>(data)
    class InsufficientFunds<T>(data : T? = null) : ProfileResult<T>(data)
    class MoneyReturnISSuccessful<T>(data : T? = null) : ProfileResult<T>(data)

}