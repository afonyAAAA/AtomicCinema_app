package com.example.atomic_cinema.server.profile

import android.content.SharedPreferences
import android.util.Log
import com.example.atomic_cinema.server.auth.SecretInfoResponse
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

    override suspend fun authenticate(): ProfileResult<SecretInfoResponse> {
        return try{
            val token = prefs.getString("jwt", null) ?: return ProfileResult.Unauthorized()
            val userSecretInfo = api.authenticate("Bearer $token")
            ProfileResult.Authorized(SecretInfoResponse(
                role = userSecretInfo.role
            ))
        }catch (e : HttpException ){
            if(e.code() == 401){
                ProfileResult.Unauthorized()
            }else{
                ProfileResult.UnknownError()
            }
        }catch (e : Exception){
            ProfileResult.UnknownError()
        }
    }
}
