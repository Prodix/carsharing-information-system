package com.syndicate.carsharing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.ui.theme.CarsharingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarsharingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    SignIn()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignIn(modifier: Modifier = Modifier) {

    var emailText by remember {
        mutableStateOf("")
    }

    var passwordText by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDFE0E2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.56f)
                .clip(RoundedCornerShape(bottomEnd = 15.dp, bottomStart = 15.dp))
                .background(Color(0xFF8113B5))
        )
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.logo),
            null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight(0.51f)
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFFFFFFF))
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 22.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Добро пожаловать в Автопрокат",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Самый выгодный каршеринг",
                    fontSize = 14.sp
                )
                Spacer(
                    modifier =
                        Modifier
                            .size(20.dp)
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = emailText,
                    onValueChange = { newText -> emailText = newText },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.email), contentDescription = "Почта" ) },
                    placeholder = { Text("Почта", fontWeight = FontWeight.Bold, fontSize = 14.sp)  },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94979E),
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFDFE0E2),
                        unfocusedLeadingIconColor = Color(0xFF94979E),
                        placeholderColor = Color(0xFF94979E),
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(
                    modifier =
                    Modifier
                        .size(20.dp)
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = passwordText,
                    onValueChange = { newText -> passwordText = newText },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.password), contentDescription = "Почта" ) },
                    placeholder = { Text("Пароль", fontWeight = FontWeight.Bold, fontSize = 14.sp)  },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94979E),
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFDFE0E2),
                        unfocusedLeadingIconColor = Color(0xFF94979E),
                        placeholderColor = Color(0xFF94979E),
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(
                    modifier =
                    Modifier
                        .size(20.dp)
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8113B5)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    onClick = { /*TODO*/ }
                ) {
                    Text(
                        text = "Войти",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(
                    modifier =
                    Modifier
                        .size(30.dp)
                )
                ClickableText(
                    text = AnnotatedString(text = "Забыли пароль?"),
                    onClick = {

                    },
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF8113B5),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Text(
            text = "РЕГИСТРАЦИЯ",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF94979E)

        )
    }
}

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun GreetingPreview() {
    CarsharingTheme {
        SignIn()
    }
}