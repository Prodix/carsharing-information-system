package com.syndicate.carsharing.models

data class SignInModel(
    val email: String = "",
    val password: String = "",
    val isByPassword: Boolean = true,
    val buttonText: String = "Войти по коду",
    val emailNote: String = "",
    val passwordNote: String = ""
)