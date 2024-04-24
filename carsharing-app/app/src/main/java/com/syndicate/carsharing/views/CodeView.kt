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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.syndicate.carsharing.viewmodels.CodeViewModel
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@Composable
fun Code(
    email: String,
    isRegister: Boolean,
    navigation: NavHostController,
    codeViewModel: CodeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val codeState = codeViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userStore = codeViewModel.userStore
    val token by userStore.getToken().collectAsState(initial = "")
    val user: User by userStore.getUser().collectAsState(initial = User())

    val timer = remember {
        mutableStateOf(Timer(0,5, 0))
    }

    DisposableEffect(context) {

        scope.launch {
            while(token == "")
                delay(100)
            HttpClient.client.post(
                "${HttpClient.url}/account/generate_code?token=${token}"
            )
        }

        scope.launch {
            timer.value.start()
        }

        onDispose { timer.value.stop() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Подтверждение почты",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        if (user.email != "") {
            Text(
                text = "На почту ${if (user.email.substring(0, user.email.indexOf('@')).length >= 5) "${user.email.substring(0..1)}***${user.email.substring(email.indexOf('@') - 2)}" else "${user.email[0]}*${user.email.substring(user.email.indexOf('@'))}"} был отправлен код",
                fontSize = 12.sp,
                color = Color(0xFFB5B5B5)
            )
        }
        Spacer(modifier = Modifier
            .size(25.dp))
        CodeComposable(
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
    timer: State<Timer>,
    scope: CoroutineScope,
    codeViewModel: CodeViewModel
) {
    val context = LocalContext.current
    val userStore = codeViewModel.userStore
    val token by userStore.getToken().collectAsState(initial = "")

    val isError by codeViewModel.isError.collectAsState()

    Text(
        text = if (!timer.value.isStarted) if (isError) "Неверный код, отправить код повторно" else "Отправить код повторно" else if (isError) "Неверный код, отправить код повторно через ${timer.value}" else "Отправить код повторно через ${timer.value}",
        fontSize = 12.sp,
        color = if (!timer.value.isStarted) Color(if (isError) 0xFFBB3E3E else 0xFF6699CC) else Color(if (isError) 0xFFBB3E3E else 0xFFB5B5B5),
        modifier = Modifier
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                enabled = !timer.value.isStarted
            ) {
                scope.launch {
                    HttpClient.client.post(
                        "${HttpClient.url}/account/generate_code?token=${token}"
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
    codeState: State<CodeModel>,
    codeViewModel: CodeViewModel,
    timer: State<Timer>,
    isRegister: Boolean,
    navigation: NavHostController,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val userStore = codeViewModel.userStore
    val user: User by userStore.getUser().collectAsState(initial = User())
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
                            fontSize = 20.sp
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
    SendCodeComposable(timer, scope, codeViewModel)
    Spacer(modifier = Modifier
        .size(25.dp))
    Button(
        onClick = {
            scope.launch {
                val response = HttpClient.client.post(
                    "${HttpClient.url}/account/signin?code=${codeState.value.code.text}"
                ) {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append("email", user.email)
                            }
                        ))
                }.body<DefaultResponse>()
                if (response.status_code == 200) {
                    if (isRegister) {
                        userStore.saveToken(response.token as String)
                        navigation.navigate("documentIntro/true/false")
                    }
                    else {
                        navigation.navigate("main")
                    }
                } else {
                    codeViewModel.changeErrorStatus(true)
                }
            }
        },
        modifier = Modifier
            .width(265.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6699CC),
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color(0xFFB5B5B5)
        ),
        border = if (codeState.value.code.text.length == 5 || codeState.value.isValid == true) null else BorderStroke(2.dp, Color(0xFFB5B5B5)),
        enabled = codeState.value.code.text.length == 5
    ) {
        Text(
            text = "Продолжить",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 10.dp)
        )
    }
}


