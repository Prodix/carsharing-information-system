package com.syndicate.carsharing.viewmodels

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.models.SignUpModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(val userStore: UserStore): ViewModel() {

    private val _uiState = MutableStateFlow(SignUpModel())
    val uiState = _uiState.asStateFlow()

    private val regexSymbol = Regex("""\W""")
    private val regexUpperChars = Regex("""[A-Z]""")
    private val regexNumbers = Regex("""\d""")
    private val regexCyrillic = Regex("""[А-Яа-я]""")
    private val regexEmail = Regex("""^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""")

   fun changeEmail(email: String) {

        if (email.isEmpty()) {
            _uiState.value = SignUpModel("", _uiState.value.password, "", _uiState.value.passwordNote)
            return
        }

        if (listOf(' ', '"', '\'').contains(email.last()))
            return

        changeEmailNote(email)
    }

    fun changePassword(password: String) {

        if (password.isEmpty()) {
            _uiState.value = SignUpModel(_uiState.value.email, "", _uiState.value.emailNote, "")
            return
        }

        if (listOf(' ', '"', '\'').contains(password.last()))
            return

        changePasswordNote(password)
    }

    private fun changeEmailNote(email: String) {
        if (!regexEmail.containsMatchIn(email)) {
            _uiState.value = SignUpModel(
                email,
                _uiState.value.password,
                "Неверный формат e-mail",
                _uiState.value.passwordNote
            )
            return
        }

        _uiState.value = SignUpModel(
            email,
            _uiState.value.password,
            "",
            _uiState.value.passwordNote
        )
    }

    private fun changePasswordNote(password: String) {

        if (!regexNumbers.containsMatchIn(password)) {
            _uiState.value = SignUpModel(
                _uiState.value.email,
                password,
                _uiState.value.emailNote,
                "Пароль должен содержать цифры"
            )
            return
        }

        if (regexCyrillic.containsMatchIn(password)) {
            _uiState.value = SignUpModel(
                _uiState.value.email,
                password,
                _uiState.value.emailNote,
                "Пароль не должен содержать кириллицу"
            )
            return
        }

        if (!regexSymbol.containsMatchIn(password)) {
            _uiState.value = SignUpModel(
                _uiState.value.email,
                password,
                _uiState.value.emailNote,
                "Пароль должен содержать специальные символы"
            )
            return
        }

        if (!regexUpperChars.containsMatchIn(password)) {
            _uiState.value = SignUpModel(
                _uiState.value.email,
                password,
                _uiState.value.emailNote,
                "Пароль должен содержать заглавные буквы"
            )
            return
        }

        if (password.length < 8) {
            _uiState.value = SignUpModel(
                _uiState.value.email,
                password,
                _uiState.value.emailNote,
                "Пароль должен быть длиннее 8 символов"
            )
            return
        }

        _uiState.value = SignUpModel(
            _uiState.value.email,
            password,
            _uiState.value.emailNote,
            ""
        )

    }
}