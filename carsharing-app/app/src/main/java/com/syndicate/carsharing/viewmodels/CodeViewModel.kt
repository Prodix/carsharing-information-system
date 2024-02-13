package com.syndicate.carsharing.viewmodels

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.models.CodeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CodeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CodeModel())
    val uiState = _uiState.asStateFlow()

    fun changeValidity(value: Boolean?) {
        _uiState.value = CodeModel(_uiState.value.code, value)
    }

    fun changeCode(code: TextFieldValue) {
        _uiState.value = CodeModel(code, _uiState.value.isValid)
    }
}