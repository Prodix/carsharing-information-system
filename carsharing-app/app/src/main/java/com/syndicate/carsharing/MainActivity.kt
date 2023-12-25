package com.syndicate.carsharing

import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.syndicate.carsharing.ui.theme.CarsharingTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    CarsharingTheme {
        val navController = rememberNavController()
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            NavHost(
                navController = navController,
                startDestination = "start"
            ) {
                composable("start") {
                    Start(
                        navigation = navController
                    )
                }
                composable("signInPassword") {
                    SignInPassword(
                        navigation = navController
                    )
                }
                composable("signInCode") {
                    SignInCode(
                        navigation = navController
                    )
                }
                composable("signUp") {
                    SignUp(
                        navigation = navController
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Start(
    modifier: Modifier = Modifier,
    navigation: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
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
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.road),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(),
                colorFilter = ColorFilter.tint(Color(0xFFF9F9FB))

            )
            Button(
                onClick = {navigation.navigate("signInPassword")},
                content = { Text(
                    text = "Войти",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6699CC)
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = "Зарегистрироваться",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(indication = null, interactionSource = MutableInteractionSource()){
                        navigation.navigate("signUp")
                    }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInPassword(
    modifier: Modifier = Modifier,
    navigation: NavHostController
) {
    var emailValue by remember {
        mutableStateOf("")
    }

    var passwordValue by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                value = emailValue,
                onValueChange = { value -> emailValue = value},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(30.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp
                ),
                placeholder = { Text(text = "Email") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color(0xFF6699CC),
                    placeholderColor = Color(0xFFB5B5B5),
                    unfocusedBorderColor = Color(0xFFB5B5B5),
                    focusedBorderColor = Color(0xFFB5B5B5),
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            OutlinedTextField(
                value = passwordValue,
                onValueChange = { value -> passwordValue = value},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(30.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp
                ),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text(text = "Пароль") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color(0xFF6699CC),
                    placeholderColor = Color(0xFFB5B5B5),
                    unfocusedBorderColor = Color(0xFFB5B5B5),
                    focusedBorderColor = Color(0xFFB5B5B5),
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Button(
                onClick = {},
                content = { Text(
                    text = "Войти",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6699CC)
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = "Войти по коду",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(indication = null, interactionSource = MutableInteractionSource()){
                        navigation.navigate("signInCode")
                    }
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable(indication = null, interactionSource = MutableInteractionSource()){
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInCode(
    modifier: Modifier = Modifier,
    navigation: NavHostController
) {
    var emailValue by remember {
        mutableStateOf("")
    }

    var passwordValue by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                value = emailValue,
                onValueChange = { value -> emailValue = value},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(30.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp
                ),
                placeholder = { Text(text = "Email") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color(0xFF6699CC),
                    placeholderColor = Color(0xFFB5B5B5),
                    unfocusedBorderColor = Color(0xFFB5B5B5),
                    focusedBorderColor = Color(0xFFB5B5B5),
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Button(
                onClick = {},
                content = { Text(
                    text = "Войти",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6699CC)
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = "Войти с паролем",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(indication = null, interactionSource = MutableInteractionSource()){
                        navigation.navigate("signInPassword")
                    }
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable(indication = null, interactionSource = MutableInteractionSource()){
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(
    modifier: Modifier = Modifier,
    navigation: NavHostController
) {
    var emailValue by remember {
        mutableStateOf("")
    }

    var passwordValue by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                value = emailValue,
                onValueChange = { value -> emailValue = value},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(30.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp
                ),
                placeholder = { Text(text = "Email") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color(0xFF6699CC),
                    placeholderColor = Color(0xFFB5B5B5),
                    unfocusedBorderColor = Color(0xFFB5B5B5),
                    focusedBorderColor = Color(0xFFB5B5B5),
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            OutlinedTextField(
                value = passwordValue,
                onValueChange = { value -> passwordValue = value},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(30.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp
                ),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text(text = "Пароль") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color(0xFF6699CC),
                    placeholderColor = Color(0xFFB5B5B5),
                    unfocusedBorderColor = Color(0xFFB5B5B5),
                    focusedBorderColor = Color(0xFFB5B5B5),
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Button(
                onClick = {  },
                content = { Text(
                    text = "Зарегистрироваться",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6699CC)
                )
            )
            Spacer(
                modifier = Modifier
                    .size(16.dp)
            )
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable(indication = null, interactionSource = MutableInteractionSource()){
                        navigation.navigate("signInPassword")
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

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun GreetingPreview() {
    App()
}