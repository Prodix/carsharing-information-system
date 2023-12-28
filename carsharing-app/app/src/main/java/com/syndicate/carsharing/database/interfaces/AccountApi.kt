package com.syndicate.carsharing.database.interfaces

import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AccountApi {

    @POST("api/account/signUp")
    fun signUp(@Body body: @JvmSuppressWildcards Map<String, Any>): Call<DefaultResponse>

    @POST("api/account/signIn")
    fun signIn(@Query("password") password: String, @Query("email") email: String): Call<DefaultResponse>

    @POST("api/account/changePassword")
    fun changePassword(@Query("password") password: String, @Query("token") token: String): Call<DefaultResponse>

    @POST("api/account/sendEmailCode")
    fun sendEmailCode(@Query("email") email: String): Call<DefaultResponse>

    @POST("api/account/validateEmailCode")
    fun validateEmailCode(@Query("code") code: Int, @Query("email") email: String): Call<DefaultResponse>

}