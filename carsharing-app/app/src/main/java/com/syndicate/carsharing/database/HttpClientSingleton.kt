package com.syndicate.carsharing.database

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson

class HttpClientSingleton {

    companion object Client {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
        }
    }

}