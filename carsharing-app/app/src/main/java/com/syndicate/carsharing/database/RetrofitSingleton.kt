package com.syndicate.carsharing.database

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class RetrofitSingleton {

    companion object Client {

        val client = RetrofitClient.getClient()

        private object RetrofitClient {
            private const val BASE_URL = "http://ip:port/"

            private val okHttpClient = OkHttpClient()
                .newBuilder()
                .addInterceptor(RequestInterceptor)
                .build()

            fun getClient(): Retrofit =
                Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build()
        }

        private object RequestInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                println("Outgoing request to ${request.url()}")
                return chain.proceed(request)
            }
        }
    }

}