package com.syndicate.carsharing.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.support.v4.os.IResultReceiver.Default
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.shared_components.AutoShareTextField
import com.syndicate.carsharing.viewmodels.SignInViewModel
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun SignIn(
    navigation: NavHostController,
    signInViewModel: SignInViewModel = hiltViewModel()
) {
    val signInState by signInViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(
                top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()
            )
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
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF6699CC)
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = "Добро пожаловать",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = "Заполните поля ниже",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .height(43.dp)
            )
            AutoShareTextField(
                value = signInState.email,
                onValueChange = { value -> signInViewModel.changeEmail(value) },
                placeholder = "Email",
                isError = signInState.emailNote != ""
            ) {
                if (signInState.emailNote != "") {
                    Text(
                        text = signInState.emailNote,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            if (signInState.isByPassword) {
                AutoShareTextField(
                    value = signInState.password,
                    onValueChange = { value -> signInViewModel.changePassword(value) },
                    isError = signInState.passwordNote != "",
                    isPassword = true,
                    placeholder = "Пароль"
                ) {
                    if (signInState.passwordNote != "") {
                        Text(
                            text = signInState.passwordNote,
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
                Spacer(
                    modifier = Modifier
                        .size(16.dp)
                )
            }
            AutoShareButton(
                text = "Войти",
                border = if ((signInState.isByPassword && signInState.email.isNotEmpty() && signInState.password.isNotEmpty()) || (!signInState.isByPassword && signInState.email.isNotEmpty())) null else BorderStroke(2.dp, Color(0xFFB5B5B5)),
                enabled = (signInState.isByPassword && signInState.email.isNotEmpty() && signInState.password.isNotEmpty()) || (!signInState.isByPassword && signInState.email.isNotEmpty())
            ) {
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
                            signInViewModel.userStore.saveToken(response.token as String)
                            initialize(
                                mainViewModel = signInViewModel.mainViewModel,
                                userStore = signInViewModel.userStore,
                                scope = scope
                            )
                            navigation.navigate("main") {
                                popUpTo(0)
                            }
                        }
                    }
                } else {
                    scope.launch {
                        val response = HttpClient.client.post(
                            "${HttpClient.url}/account/generate_code?email=${signInState.email}"
                        ).body<DefaultResponse>()
                        if (response.status_code != 200 && response.message != "Прошло менее 5 минут") {
                            AlertDialog.Builder(context)
                                .setMessage(response.message)
                                .setPositiveButton("ok") { _, _ -> run { } }
                                .show()
                        } else {
                            navigation.navigate("code/false/${signInState.email}")
                        }
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = signInState.buttonText,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
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
                    style = MaterialTheme.typography.displaySmall,
                    color = Color(0xFFB5B5B5)
                )
                Text(
                    text = "Зарегистрируйтесь",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }

    }
}