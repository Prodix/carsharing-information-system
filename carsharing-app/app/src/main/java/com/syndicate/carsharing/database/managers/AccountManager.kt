package com.syndicate.carsharing.database.managers

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import com.syndicate.carsharing.database.RetrofitSingleton
import com.syndicate.carsharing.database.interfaces.AccountApi
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.models.SignUpModel
import com.syndicate.carsharing.viewmodels.SignInViewModel
import com.syndicate.carsharing.viewmodels.SignUpViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountManager {

    companion object Manager {
        private val retrofit = RetrofitSingleton.client
        private val accountApi = retrofit.create(AccountApi::class.java)

        fun signUp(
            passportSerie: Int,
            passportNumber: Int,
            name: String,
            surname: String,
            patronymic: String,
            birthDate: String,
            licenseSerie: Int,
            licenseNumber: Int,
            startDate: String,
            endDate: String,
            city: String,
            categories: List<String>,
            email: String,
            password: String,
            toast: Toast,
            viewModel: SignUpViewModel
        ) {
            val response = accountApi.signUp(
                mapOf(
                    "passport" to mapOf(
                        "serie" to passportSerie,
                        "number" to passportNumber,
                        "name" to name,
                        "surname" to surname,
                        "patronymic" to patronymic,
                        "birth_date" to birthDate
                    ),
                    "license" to mapOf(
                        "serie" to licenseSerie,
                        "number" to licenseNumber,
                        "start_date" to startDate,
                        "end_date" to endDate,
                        "city" to city,
                        "categories" to categories
                    ),
                    "user" to mapOf(
                        "email" to email,
                        "password" to password
                    )
                )
            ).enqueue(object : Callback<DefaultResponse> {
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    if (!response.isSuccessful){
                        toast.setText("Проблемы с отправкой запроса")
                        toast.show()
                        return;
                    }

                    val body = response.body()

                    if (body!!.status_code == 401) {
                        toast.setText("Пользователь уже зарегистрирован")
                        toast.show()
                        return;
                    }

                    if (body.status_code == 402) {
                        toast.setText("Паспорт уже используется")
                        toast.show()
                        return;
                    }

                    if (body.status_code == 403) {
                        toast.setText("Удостоверение уже используется")
                        toast.show()
                        return;
                    }

                    viewModel.changeRequestState("Пользователь зарегистрирован")
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    toast.setText("Неполадки с сетью")
                    toast.show()
                }

            })
        }

        fun signIn(
            login: String,
            password: String,
            viewModel: SignInViewModel
        ) {
            val response = accountApi.signIn(password, login)
                .enqueue(object : Callback<DefaultResponse> {
                    override fun onResponse(
                        call: Call<DefaultResponse>,
                        response: Response<DefaultResponse>
                    ) {
                        Log.d("test", response?.body().toString())
                        if (response.isSuccessful && response.body()?.status_code == 200)
                            viewModel.changeRequestState("Пользователь авторизован")
                    }

                    override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                        Log.d("fail", "auth failure")
                    }

                })
        }
    }
}