package com.syndicate.carsharing

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.syndicate.carsharing.ui.theme.CarsharingTheme
import com.syndicate.carsharing.views.Camera
import com.syndicate.carsharing.views.Code
import com.syndicate.carsharing.views.Main
import com.syndicate.carsharing.views.Document
import com.syndicate.carsharing.views.DocumentIntro
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
                startDestination = "main"
            ) {
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
                composable(
                    "documentViewer/{fileName}",
                    arguments = listOf(
                        navArgument("fileName") { type = NavType.StringType }
                    )
                ) {
                    Document(
                        fileName = it.arguments?.getString("fileName") ?: "",
                        navigation = navController
                    )
                }
                composable(
                    "camera/{fileName}",
                    arguments = listOf(
                        navArgument("fileName") { type = NavType.StringType }
                    )
                ) {
                    Camera(
                        fileName = it.arguments?.getString("fileName") ?: "",
                        navigation = navController
                    )
                }
                composable(
                    "documentIntro/{isPassport}/{isSelfie}",
                    arguments = listOf(
                        navArgument("isPassport") { type = NavType.BoolType },
                        navArgument("isSelfie") { type = NavType.BoolType }
                    )
                ) {
                    DocumentIntro(
                        isPassport = it.arguments?.getBoolean("isPassport") ?: false,
                        isSelfie = it.arguments?.getBoolean("isSelfie") ?: false,
                        navigation = navController
                    )
                }
                composable(
                    "code/{isRegister}/{email}",
                    arguments = listOf(
                        navArgument("isRegister") { type = NavType.BoolType },
                        navArgument("email") { type = NavType.StringType }
                    )
                ) {
                    Code(
                        email = it.arguments?.getString("email") ?: "",
                        isRegister = it.arguments?.getBoolean("isRegister") ?: false,
                        navigation = navController
                    )
                }
            }
        }
    }
}