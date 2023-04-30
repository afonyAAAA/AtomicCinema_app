package com.example.atomic_cinema.server.auth

import android.content.SharedPreferences
import retrofit2.HttpException
import java.time.LocalDate

class AuthRepositoryImpl(
    private val api : AuthApi,
    private val prefs : SharedPreferences
    ) : AuthRepository{

    override suspend fun signUp(
        login: String,
        password: String,
        firstName: String,
        name: String,
        lastName: String,
        numberPhone: String,
        dateOfBirth: LocalDate,
    ): AuthResult<Unit> {
        return try{
            api.signUp(
                request = AuthSignUpRequest(
                    login = login,
                    password = password,
                    firstName = firstName,
                    name = name,
                    lastName = lastName,
                    numberPhone = numberPhone,
                    dateBirth = dateOfBirth,
                    idRole = 1
                )
            )
            AuthResult.Registered()
        }catch (e : HttpException){
            if(e.code() == 401){
                AuthResult.Unauthorized()
            }else{
                e.printStackTrace()
                AuthResult.UnknownError()
            }
        }catch (e : Exception){
            e.printStackTrace()
            AuthResult.UnknownError()
        }
    }

    override suspend fun signIn(login: String, password: String): AuthResult<Unit> {
            return try{
                val response = api.signIn(
                    request = AuthSignInRequest(login = login, password = password)
                )
                prefs.edit().putString("jwt", response.token)
                    .apply()
                AuthResult.Authorized()
            }catch (e : HttpException ){
                if(e.code() == 401){
                    AuthResult.Unauthorized()
                }else{
                    AuthResult.UnknownError()
                }
            }catch (e : Exception){
                AuthResult.UnknownError()
            }
    }

    override suspend fun authenticate(): AuthResult<SecretInfoResponse> {
        return try{
            val token = prefs.getString("jwt", null) ?: return AuthResult.Unauthorized()
            val userSecretInfo = api.authenticate("Bearer $token")
            AuthResult.Authorized(SecretInfoResponse(
                role = userSecretInfo.role
            ))
        }catch (e : HttpException ){
            if(e.code() == 401){
                AuthResult.Unauthorized()
            }else{
                AuthResult.UnknownError()
            }
        }catch (e : Exception){
            AuthResult.UnknownError()
        }
    }

    override suspend fun goOut(): AuthResult<Unit> {
        return try{
            val token = prefs.getString("jwt", null) ?: return AuthResult.Unauthorized()
            prefs.edit().clear().apply()
            api.goOut("Bearer $token")
            AuthResult.CameOut()
        }catch(e : HttpException){
            if(e.code() == 401){
                AuthResult.Unauthorized()
            }else{
                AuthResult.UnknownError()
            }
        }catch (e : Exception){
            AuthResult.UnknownError()
        }
    }
}