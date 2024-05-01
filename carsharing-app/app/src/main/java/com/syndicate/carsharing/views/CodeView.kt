package com.syndicate.carsharing.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.data.Timer
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.User
import com.syndicate.carsharing.models.CodeModel
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.viewmodels.CodeViewModel
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@Composable
fun Code(
    email: String,
    isRegister: Boolean,
    navigation: NavHostController,
    codeViewModel: CodeViewModel = hiltViewModel()
) {
    val codeState = codeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val timer = remember {
        mutableStateOf(Timer(0,5, 0))
    }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            timer.value.start()
        }
    }

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Подтверждение почты",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier
            .size(10.dp))
        if (email != "") {
            Text(
                text = "На почту ${if (email.substring(0, email.indexOf('@')).length >= 5) "${email.substring(0..1)}***${email.substring(email.indexOf('@') - 2)}" else "${email[0]}*${email.substring(email.indexOf('@'))}"} был отправлен код",
                style = MaterialTheme.typography.displaySmall,
                color = Color(0xFFB5B5B5)
            )
        }
        Spacer(modifier = Modifier
            .size(25.dp))
        CodeComposable(
            email,
            codeState,
            codeViewModel,
            timer,
            isRegister,
            navigation,
            scope
        )
    }
}

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun SendCodeComposable(
    email: String,
    timer: State<Timer>,
    scope: CoroutineScope,
    codeViewModel: CodeViewModel
) {
    val isError by codeViewModel.isError.collectAsState()

    Text(
        text = if (!timer.value.isStarted) if (isError) "Неверный код, отправить код повторно" else "Отправить код повторно" else if (isError) "Неверный код, отправить код повторно через ${timer.value}" else "Отправить код повторно через ${timer.value}",
        style = MaterialTheme.typography.displaySmall.copy(
            fontSize = 12.sp
        ),
        color = if (!timer.value.isStarted) Color(if (isError) 0xFFBB3E3E else 0xFF6699CC) else Color(if (isError) 0xFFBB3E3E else 0xFFB5B5B5),
        modifier = Modifier
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                enabled = !timer.value.isStarted
            ) {
                scope.launch {
                    HttpClient.client.post(
                        "${HttpClient.url}/account/generate_code?email=$email"
                    )
                }
                timer.value.restart()
                scope.launch {
                    timer.value.start()
                }
            }
    )
    Spacer(modifier = Modifier
        .size(25.dp))
}

@Composable
fun CodeComposable(
    email: String,
    codeState: State<CodeModel>,
    codeViewModel: CodeViewModel,
    timer: State<Timer>,
    isRegister: Boolean,
    navigation: NavHostController,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val userStore = codeViewModel.userStore
    val isError by codeViewModel.isError.collectAsState()

    BasicTextField(
        value = codeState.value.code,
        onValueChange = {
            codeViewModel.changeErrorStatus(false)
            if (it.text.length <= 5) {
                codeViewModel.changeCode(it)
            }
        },
        decorationBox = {
            Row(
                modifier = Modifier
                    .padding(15.dp)
            ){
                repeat(5) {
                    Spacer(
                        modifier = Modifier
                            .width(5.dp)
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (isError) Color(0xFFFAF0F0) else Color(0xFFF0F5FA),
                                RoundedCornerShape(10.dp)
                            )
                            .size(50.dp)
                            .then(
                                if (isError) {
                                    Modifier
                                        .border(2.dp, Color(0xFFBB3E3E), RoundedCornerShape(10.dp))
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = codeState.value.code.text.getOrElse(it, {' '}).toString(),
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .width(5.dp)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    SendCodeComposable(email, timer, scope, codeViewModel)
    Spacer(modifier = Modifier
        .size(25.dp))
    AutoShareButton(
        text = "Продолжить",
        border = if (codeState.value.code.text.length == 5 || codeState.value.isValid == true) null else BorderStroke(2.dp, Color(0xFFB5B5B5)),
        enabled = codeState.value.code.text.length == 5,
        modifier = Modifier
            .width(265.dp)
    ) {
        scope.launch {
            val response = HttpClient.client.post(
                "${HttpClient.url}/account/signin?code=${codeState.value.code.text}"
            ) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("email", email)
                        }
                    ))
            }.body<DefaultResponse>()
            if (response.status_code == 200) {
                userStore.saveToken(response.token as String)
                if (userStore.getUser().first().driverLicenseId == 0) {
                    navigation.navigate("documentIntro/false/false") {
                        popUpTo(0)
                    }
                } else if (userStore.getUser().first().passportId == 0) {
                    navigation.navigate("documentIntro/true/false") {
                        popUpTo(0)
                    }
                } else if (userStore.getUser().first().selfieId == 0) {
                    navigation.navigate("documentIntro/false/true") {
                        popUpTo(0)
                    }
                } else {
                    initialize(
                        mainViewModel = codeViewModel.mainViewModel,
                        userStore = userStore,
                        scope = scope
                    )
                    navigation.navigate("main") {
                        popUpTo(0)
                    }
                }
            } else {
                codeViewModel.changeErrorStatus(true)
            }
        }
    }
}


