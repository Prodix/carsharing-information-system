package com.syndicate.carsharing.models

data class SignUpModel(
    val email: String = "",
    val password: String = "",
    val emailNote: String = "",
    val passwordNote: String = ""
)