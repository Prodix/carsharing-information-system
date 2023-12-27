package com.syndicate.carsharing.viewmodels

import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.models.SignUpModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpModel())
    val uiState = _uiState.asStateFlow()

    fun changeEmail(email: String) {
        _uiState.value = SignUpModel(email, _uiState.value.password)
    }

    fun changePassword(password: String) {
        _uiState.value = SignUpModel(_uiState.value.email, password)
    }
}