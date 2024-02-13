package com.syndicate.carsharing.models

import androidx.compose.ui.text.input.TextFieldValue

data class CodeModel (
    val code: TextFieldValue = TextFieldValue(),
    val isValid: Boolean? = null
)