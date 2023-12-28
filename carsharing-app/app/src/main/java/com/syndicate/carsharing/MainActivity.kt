package com.syndicate.carsharing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.syndicate.carsharing.ui.theme.CarsharingTheme
import com.syndicate.carsharing.views.Main
import com.syndicate.carsharing.views.SignIn
import com.syndicate.carsharing.views.SignUp
import com.syndicate.carsharing.views.Start

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
                composable("signIn") {
                    SignIn(
                        navigation = navController
                    )
                }
                composable("signUp") {
                    SignUp(
                        navigation = navController
                    )
                }
                composable("main") {
                    Main(
                        navigation = navController
                    )
                }
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