package com.syndicate.carsharing.viewmodels

import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.models.SignInModel
import com.syndicate.carsharing.models.SignUpModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    val userStore: UserStore,
    val mainViewModel: MainViewModel
) : ViewModel()  {

    private val _uiState = MutableStateFlow(SignInModel())
    val uiState = _uiState.asStateFlow()

    fun changeEmail(email: String) {

        if (email.isEmpty()) {
            _uiState.update {
                it.copy(email = "")
            }
            return
        }

        if (listOf(' ', '"', '\'').contains(email.last()))
            return

        _uiState.value = SignInModel(email, _uiState.value.password, _uiState.value.isByPassword, _uiState.value.buttonText, _uiState.value.emailNote, _uiState.value.passwordNote)
    }

    fun changePassword(password: String) {

        if (password.isEmpty()) {
            _uiState.value = SignInModel(_uiState.value.email, "", _uiState.value.isByPassword, _uiState.value.buttonText, _uiState.value.emailNote, _uiState.value.passwordNote)
            return
        }

        if (listOf(' ', '"', '\'').contains(password.last()))
            return

        _uiState.value = SignInModel(_uiState.value.email, password, _uiState.value.isByPassword, _uiState.value.buttonText, _uiState.value.emailNote, _uiState.value.passwordNote)
    }

    fun changePasswordVisibility(visibility: Boolean) {
        _uiState.value = SignInModel(
            _uiState.value.email,
            _uiState.value.password,
            visibility,
            if (visibility) "Войти по коду" else "Войти с паролем",
            _uiState.value.emailNote,
            _uiState.value.passwordNote
        )
    }

    fun updateEmailNote(note: String) {
        _uiState.value = SignInModel(_uiState.value.email, _uiState.value.password, _uiState.value.isByPassword, _uiState.value.buttonText, note, _uiState.value.passwordNote)
    }

    fun updatePasswordNote(note: String) {
        _uiState.value = SignInModel(_uiState.value.email, _uiState.value.password, _uiState.value.isByPassword, _uiState.value.buttonText, _uiState.value.emailNote, note)
    }
}