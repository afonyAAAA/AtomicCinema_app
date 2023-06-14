package com.example.atomic_cinema.server.profile

import android.content.SharedPreferences
import retrofit2.HttpException
import java.time.LocalDate

class ProfileRepositoryImpl(
    private val api : ProfileApi,
    private val prefs : SharedPreferences
) : ProfileRepository {

    override suspend fun getProfileInfo(): ProfileResult<ProfileResponse> {
        return try{
            val token = prefs.getString("jwt", null) ?: return ProfileResult.Unauthorized()
            val response = api.getProfileInfo("Bearer $token")
            ProfileResult.Shown(response)
        }catch (e : HttpException){
            if(e.code() == 401){
                ProfileResult.Unauthorized()
            }else{
                ProfileResult.UnknownError()
            }
        }catch (e : Exception){
            ProfileResult.UnknownError()
        }
    }

    override suspend fun getProfileInfoPaymentsForMonth(): ProfileResult<ProfileResponsePaymentsInfo> {
        return try{
            val token = prefs.getString("jwt", null) ?: return ProfileResult.Unauthorized()
            val response = api.getProfileInfoPaymentsForMonth("Bearer $token")
            ProfileResult.Shown(response)
        }catch (e : HttpException){
            if(e.code() == 401){
                ProfileResult.Unauthorized()
            }else if(e.code() == 404){
                ProfileResult.NotFoundTicketsForMonth()
            }else{
                ProfileResult.UnknownError()
            }
        }catch (e : Exception){
            ProfileResult.UnknownError()
        }
    }

    override suspend fun updateBalance(balance : String): ProfileResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return ProfileResult.Unauthorized()
             api.updateBalance(
                 "Bearer $token",
                 ProfileBalanceRequest(
                     balance = balance
                 )
             )
            ProfileResult.MoneyOperationIsSuccessful()
        }catch (e : HttpException){
            if(e.code() == 401){
                ProfileResult.Unauthorized()
            }else{
                ProfileResult.UnknownError()
            }
        }catch (e : Exception){
            ProfileResult.UnknownError()
        }
    }

    override suspend fun editProfile(
        firstName: String,
        name: String,
        lastName: String,
        numberPhone: String,
        dateOfBirth: LocalDate,
    ): ProfileResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return ProfileResult.Unauthorized()
            api.editProfile(
                "Bearer $token",
                ProfileEditRequest(
                    firstName = firstName,
                    name = name,
                    lastName = lastName,
                    numberPhone = numberPhone,
                    dateOfBirth = dateOfBirth
                )
            )
            ProfileResult.Edited()
        }catch (e : HttpException ){
            if(e.code() == 401){
                ProfileResult.Unauthorized()
            }else{
                e.printStackTrace()
                ProfileResult.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            ProfileResult.UnknownError()
        }
    }
}
