package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonProperty

data class User(
    @JsonProperty("id") val id: Int,
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("passport") val passport: Int,
    @JsonProperty("driver_license") val driverLicense: Int,
    @JsonProperty("billing") val billing: Int,
    @JsonProperty("token") val token: String,
)