package com.syndicate.carsharing.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.viewmodels.SignInViewModel
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignIn(
    navigation: NavHostController,
    signInViewModel: SignInViewModel = viewModel()
) {
    val signInState by signInViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val imeState = rememberImeState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value){
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.roadhorizontal),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 50.dp,
                    vertical = 80.dp
                )
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.logo),
                contentDescription = null)
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = "AutoShare",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6699CC)
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = "Добро пожаловать",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = "Заполните поля ниже",
                fontSize = 14.sp,
                modifier = Modifier
                    .height(43.dp)
            )
            OutlinedTextField(
                value = signInState.email,
                onValueChange = { value -> signInViewModel.changeEmail(value) },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp
                ),
                placeholder = { Text(text = "Email") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color(0xFFB5B5B5),
                    unfocusedPlaceholderColor = Color(0xFFB5B5B5),
                    unfocusedBorderColor = Color(0xFFB5B5B5),
                    focusedBorderColor = Color(0xFFB5B5B5),
                    errorBorderColor = Color(0xFFBB3E3E),
                    errorCursorColor = Color(0xFFBB3E3E),
                    errorSupportingTextColor = Color(0xFFBB3E3E)
                ),
                isError = signInState.emailNote != "",
                supportingText = {
                    if (signInState.emailNote != "") {
                        Text(
                            text = signInState.emailNote,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            if (signInState.isByPassword) {
                OutlinedTextField(
                    value = signInState.password,
                    onValueChange = { value -> signInViewModel.changePassword(value) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text(text = "Пароль") },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
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
                    isError = signInState.passwordNote != "",
                    supportingText = {
                        if (signInState.passwordNote != "") {
                            Text(
                                text = signInState.passwordNote,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                )
                Spacer(
                    modifier = Modifier
                        .size(16.dp)
                )
            }
            Button(
                onClick = {
                    if (signInState.isByPassword) {
                        scope.launch {
                            val response = HttpClient.client.post(
                                "${HttpClient.url}/account/signin"
                            ) {
                                setBody(
                                    MultiPartFormDataContent(
                                        formData {
                                            append("email", signInState.email)
                                            append("password", signInState.password)
                                        }
                                    ))
                            }.body<DefaultResponse>()
                            if (response.status_code != 200) {
                                AlertDialog.Builder(context)
                                    .setMessage(response.message)
                                    .setPositiveButton("ok") { _, _ -> run { } }
                                    .show()
                            } else {
                                navigation.navigate("main")
                            }
                        }
                    } else {
                        navigation.navigate("code/false/${signInState.email}")
                    }
                },
                content = { Text(
                    text = "Войти",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                ) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6699CC),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color(0xFFB5B5B5)
                ),
                border = if ((signInState.isByPassword && signInState.email.isNotEmpty() && signInState.password.isNotEmpty()) || (!signInState.isByPassword && signInState.email.isNotEmpty())) null else BorderStroke(2.dp, Color(0xFFB5B5B5)),
                enabled = (signInState.isByPassword && signInState.email.isNotEmpty() && signInState.password.isNotEmpty()) || (!signInState.isByPassword && signInState.email.isNotEmpty())
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = signInState.buttonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(indication = null, interactionSource = MutableInteractionSource()){
                        signInViewModel.changePasswordVisibility(!signInState.isByPassword)
                    }
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable(indication = null, interactionSource = MutableInteractionSource()) {
                        navigation.navigate("signUp")
                    }
            ) {
                Text(
                    text = "Нет аккаунта? ",
                    fontSize = 16.sp,
                    color = Color(0xFFB5B5B5)
                )
                Text(
                    text = "Зарегистрируйтесь",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

    }
}