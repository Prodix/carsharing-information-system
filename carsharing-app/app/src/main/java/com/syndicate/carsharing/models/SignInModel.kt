package com.syndicate.carsharing.models

data class SignInModel(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val buttonText: String = "Войти с паролем"
)