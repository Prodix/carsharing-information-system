package com.syndicate.carsharing.viewmodels

import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.models.SignInModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignInViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SignInModel())
    val uiState = _uiState.asStateFlow()

    fun changeEmail(email: String) {
        _uiState.value = SignInModel(email, _uiState.value.password)
    }

    fun changePassword(password: String) {
        _uiState.value = SignInModel(_uiState.value.email, password)
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