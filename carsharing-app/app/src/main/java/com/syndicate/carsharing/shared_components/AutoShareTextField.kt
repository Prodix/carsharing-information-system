package com.syndicate.carsharing.shared_components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AutoShareTextField(
    value: String,
    placeholder: String = "",
    isError: Boolean = false,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit,
    supportingText: @Composable (() -> Unit)?,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        textStyle = MaterialTheme.typography.displayMedium,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        placeholder = { Text(text = placeholder, style = MaterialTheme.typography.displayMedium) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedPlaceholderColor = Color(0xFFB5B5B5),
            unfocusedPlaceholderColor = Color(0xFFB5B5B5),
            unfocusedBorderColor = Color(0xFFB5B5B5),
            focusedBorderColor = Color(0xFFB5B5B5),
            errorBorderColor = Color(0xFFBB3E3E),
            errorCursorColor = Color(0xFFBB3E3E),
            errorSupportingTextColor = Color(0xFFBB3E3E)
        ),
        isError = isError,
        supportingText = supportingText
    )
}

