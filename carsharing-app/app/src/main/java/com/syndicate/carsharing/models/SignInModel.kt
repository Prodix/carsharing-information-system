package com.syndicate.carsharing.models

data class SignInModel(
    val email: String = "",
    val password: String = "",
    val isByPassword: Boolean = false,
    val buttonText: String = "Войти с паролем",
    val emailNote: String = "",
    val passwordNote: String = ""
)