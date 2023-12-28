package com.syndicate.carsharing.database.interfaces

import com.syndicate.carsharing.database.models.Car
import com.syndicate.carsharing.database.models.DefaultResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CarApi {
    @GET("api/car/get")
    fun getCars(): Call<List<Car>>
}