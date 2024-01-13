package com.syndicate.carsharing.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun Code(
    email: String,
    isRegister: Boolean,
    navigation: NavHostController,
    modifier: Modifier = Modifier
) {
    var value by remember {
        mutableStateOf(TextFieldValue())
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
        BasicTextField(
            value = value,
            onValueChange = {
                if (it.text.length <= 5)
                    value = it
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
                                text = value.text.getOrElse(it, {' '}).toString(),
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
        Spacer(modifier = Modifier
            .size(25.dp))
        Text(
            text = "Отправить код повторно через 5:00",
            fontSize = 12.sp,
            color = Color(0xFFB5B5B5)
        )
        Spacer(modifier = Modifier
            .size(25.dp))
        Button(
            onClick = {
                /* TODO: Проверка кода */
                if (isRegister)
                    navigation.navigate("documentIntro/true")
                else
                    navigation.navigate("main")
            },
            modifier = Modifier
                .width(265.dp)
                .border(2.dp, Color(0xFFB5B5B5), RoundedCornerShape(10.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Text(
                text = "Продолжить",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB5B5B5)
            )
        }
    }
}
