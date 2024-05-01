package com.syndicate.carsharing.database.models

import com.fasterxml.jackson.annotation.JsonProperty

data class User(
    @JsonProperty("Id") val id: Int = 0,
    @JsonProperty("Email") val email: String = "",
    @JsonProperty("Password") val password: String = "",
    @JsonProperty("UserRole") val userRole: String = "",
    @JsonProperty("PassportId") val passportId: Int? = null,
    @JsonProperty("DriverLicenseId") val driverLicenseId: Int? = null,
    @JsonProperty("Balance") val balance: Double = 0.0,
    @JsonProperty("IsVerified") val isVerified: Boolean = false,
    @JsonProperty("IsEmailVerified") val isEmailVerified: Boolean = false,
    @JsonProperty("SelfieId") val selfieId: Int? = null,
)