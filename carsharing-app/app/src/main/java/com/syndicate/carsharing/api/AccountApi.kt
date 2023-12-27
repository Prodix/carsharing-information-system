package com.syndicate.carsharing.api

import com.syndicate.carsharing.database.models.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface AccountApi {
    @POST("/api/account/signIn")
    fun signIn(): Call<DefaultResponse>
}

data class DefaultResponse (
    val message: String = "",
    val status_code: Int = 0
)
