package com.syndicate.carsharing.viewmodels

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.database.managers.AccountManager
import com.syndicate.carsharing.models.SignUpModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpModel())
    val uiState = _uiState.asStateFlow()

    fun changeEmail(email: String) {
        if (listOf(' ', '"', '\'').contains(email.last()))
            return
        _uiState.value = SignUpModel(email, _uiState.value.password)
    }

    fun changeRequestState(request: String) {
        _uiState.value = SignUpModel(_uiState.value.email, _uiState.value.password, request)
    }

    fun changePassword(password: String) {
        if (listOf(' ', '"', '\'').contains(password.last()))
            return
        _uiState.value = SignUpModel(_uiState.value.email, password)
    }

}