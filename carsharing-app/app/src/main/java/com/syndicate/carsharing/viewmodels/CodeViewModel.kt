package com.syndicate.carsharing.viewmodels

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.models.CodeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CodeViewModel @Inject constructor(val userStore: UserStore) : ViewModel() {
    private val _uiState = MutableStateFlow(CodeModel())
    val uiState = _uiState.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    fun changeErrorStatus(value: Boolean) {
        _isError.update {
            value
        }
    }

    fun changeValidity(value: Boolean?) {
        _uiState.value = CodeModel(_uiState.value.code, value)
    }

    fun changeCode(code: TextFieldValue) {
        _uiState.value = CodeModel(code, _uiState.value.isValid)
    }
}