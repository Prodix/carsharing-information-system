package com.syndicate.carsharing.database

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson

class HttpClient {

    companion object Client {
        val url = "http://192.168.0.108:5052/api"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 10000
            }
        }
    }

}