package com.syndicate.carsharing.database.managers

import android.util.Log
import android.widget.Toast
import com.syndicate.carsharing.database.RetrofitSingleton
import com.syndicate.carsharing.database.interfaces.AccountApi
import com.syndicate.carsharing.database.interfaces.CarApi
import com.syndicate.carsharing.database.models.Car
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.syndicate.carsharing.viewmodels.SignUpViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CarManager {

    companion object Manager {
        private val retrofit = RetrofitSingleton.client
        private val carApi = retrofit.create(CarApi::class.java)

        fun getCars(
            viewModel: MainViewModel
        ) {
            val response = carApi.getCars().enqueue(object : Callback<List<Car>> {
                override fun onResponse(call: Call<List<Car>>, response: Response<List<Car>>) {
                    Log.d("test", response.body().toString())
                    if (response.isSuccessful) {
                        response.body()?.let { viewModel.changeStartCar(it) }
                    }
                }

                override fun onFailure(call: Call<List<Car>>, t: Throwable) {

                    Log.d("fail", t.toString())

                    Log.d("fail","getCar failure")
                }


            })
        }
    }

}