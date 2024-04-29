package com.syndicate.carsharing.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.syndicate.carsharing.R
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.User
import com.syndicate.carsharing.viewmodels.SignUpViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.nefilim.kjwt.JWT
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.launch
import java.security.spec.AlgorithmParameterSpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SignUp(
    navigation: NavHostController,
    signUpViewModel: SignUpViewModel = hiltViewModel()
) {
    val signUpState by signUpViewModel.uiState.collectAsState()
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userStore = signUpViewModel.userStore

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
                text = "Регистрация аккаунта",
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
                value = signUpState.email,
                onValueChange = { value -> signUpViewModel.changeEmail(value) },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp
                ),
                placeholder = { Text(text = "Email") },
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
                isError = signUpState.emailNote != "",
                supportingText = {
                    if (signUpState.emailNote != "") {
                        Text(
                            text = signUpState.emailNote,
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
            OutlinedTextField(
                value = signUpState.password,
                onValueChange = { value -> signUpViewModel.changePassword(value) },
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
                isError = signUpState.passwordNote != "",
                supportingText = {
                    if (signUpState.passwordNote != "") {
                        Text(
                            text = signUpState.passwordNote,
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
            Button(
                onClick = {
                    val mapper = jacksonObjectMapper()
                    scope.launch {
                        val response = HttpClient.client.post(
                            "${HttpClient.url}/account/signup"
                        ) {
                            setBody(
                                MultiPartFormDataContent(
                                    formData {
                                        append("email", signUpState.email)
                                        append("password", signUpState.password)
                                    }
                                ))
                        }.body<DefaultResponse>()
                        if (response.status_code != 200) {
                            AlertDialog.Builder(context)
                                .setMessage(response.message)
                                .setPositiveButton("ok") { _, _ -> run { } }
                                .show()
                        } else {
                            userStore.saveToken(response.token as String)
                            navigation.navigate("code/true/${signUpState.email}")
                        }
                    }
                },
                content = { Text(
                    text = "Зарегистрироваться",
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
                border = if (signUpState.emailNote == "" && signUpState.passwordNote == "" && signUpState.password != "" && signUpState.email != "") null else BorderStroke(2.dp, Color(0xFFB5B5B5)),
                enabled = signUpState.emailNote == "" && signUpState.passwordNote == "" && signUpState.password != "" && signUpState.email != ""
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable(indication = null, interactionSource = MutableInteractionSource()) {
                        navigation.navigate("signIn")
                    }
            ) {
                Text(
                    text = "Есть аккаунт? ",
                    fontSize = 16.sp,
                    color = Color(0xFFB5B5B5)
                )
                Text(
                    text = "Войти",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

    }
}

@Composable
fun rememberImeState(): State<Boolean> {
    val imeState = remember {
        mutableStateOf(false)
    }

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            imeState.value = isKeyboardOpen
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return imeState
}