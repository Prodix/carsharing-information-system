package com.syndicate.carsharing.viewmodels

import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.models.SignInModel
import com.syndicate.carsharing.models.SignUpModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignInViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SignInModel())
    val uiState = _uiState.asStateFlow()

    fun changeEmail(email: String) {
        if (listOf(' ', '"', '\'').contains(email.last()))
            return
        _uiState.value = SignInModel(email, _uiState.value.password, _uiState.value.isPasswordVisible)
    }

    fun changePassword(password: String) {
        if (listOf(' ', '"', '\'').contains(password.last()))
            return
        _uiState.value = SignInModel(_uiState.value.email, password, _uiState.value.isPasswordVisible)
    }

    fun changePasswordVisibility(visibility: Boolean) {
        _uiState.value = SignInModel(
            _uiState.value.email,
            _uiState.value.password,
            visibility,
            if (_uiState.value.buttonText == "Войти с паролем") "Войти по коду" else "Войти с паролем"
        )
    }
}