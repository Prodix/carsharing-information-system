package com.syndicate.carsharing.views

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.data.Timer
import com.syndicate.carsharing.models.CodeModel
import com.syndicate.carsharing.viewmodels.CodeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Code(
    email: String,
    isRegister: Boolean,
    navigation: NavHostController,
    codeViewModel: CodeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val codeState = codeViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val timer = remember {
        mutableStateOf(Timer())
    }

    DisposableEffect(context) {

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
        Text(
            text = "На почту ${if (email.substring(0, email.indexOf('@')).length >= 5) "${email.substring(0..1)}***${email.substring(email.indexOf('@') - 2)}" else "${email[0]}*${email.substring(email.indexOf('@'))}"} был отправлен код",
            fontSize = 12.sp,
            color = Color(0xFFB5B5B5)
        )
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
fun  SendCodeComposable(
    timer: State<Timer>,
    scope: CoroutineScope
) {

    Text(
        text = if (timer.value.toString() == "0:00") "Отправить код повторно" else "Отправить код повторно через ${timer.value}",
        fontSize = 12.sp,
        color = if (timer.value.toString() == "0:00") Color(0xFF6699CC) else Color(0xFFB5B5B5),
        modifier = Modifier
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                enabled = timer.value.toString() == "0:00"
            ) {
                /* TODO: Добавить отправку кода*/
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
    BasicTextField(
        value = codeState.value.code,
        onValueChange = {
            if (it.text.length <= 5)
                codeViewModel.changeCode(it)
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
                            .background(Color(0xFFF0F5FA), RoundedCornerShape(10.dp))
                            .size(50.dp),
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
    SendCodeComposable(timer, scope)
    Spacer(modifier = Modifier
        .size(25.dp))
    Button(
        onClick = {
            /* TODO: Проверка кода */
            if (isRegister)
                navigation.navigate("documentIntro/true/false")
            else
                navigation.navigate("main")
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


