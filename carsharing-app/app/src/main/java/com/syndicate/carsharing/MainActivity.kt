package com.syndicate.carsharing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.util.SntpClient
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.syndicate.carsharing.ui.theme.CarsharingTheme
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.syndicate.carsharing.views.Camera
import com.syndicate.carsharing.views.Code
import com.syndicate.carsharing.views.Main
import com.syndicate.carsharing.views.Document
import com.syndicate.carsharing.views.DocumentIntro
import com.syndicate.carsharing.views.PermissionView
import com.syndicate.carsharing.views.SignIn
import com.syndicate.carsharing.views.SignUp
import com.syndicate.carsharing.views.SplashScreen
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userStore: UserStore

    @Inject
    lateinit var mainViewModel: MainViewModel

    companion object {
        lateinit var fusedLocationClient: FusedLocationProviderClient
    }


    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.Black.toArgb(),
                Color.Black.toArgb()
            )
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        SntpClient.initialize(null, null)

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_KEY)
        setContent {
            CarsharingApp(userStore, mainViewModel)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CarsharingApp(
    userStore: UserStore,
    mainViewModel: MainViewModel
) {

    CarsharingTheme {
        val navController = rememberNavController()

        val scope = rememberCoroutineScope()

        mainViewModel.updateSheetState(
            rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                skipHalfExpanded = true
            )
        )

        val listener: MapObjectTapListener = MapObjectTapListener { placemark, point: Point ->
            mainViewModel.updateLastSelectedPlacemark(placemark as PlacemarkMapObject)

            if (mainViewModel.uiState.value.isReserving)
                mainViewModel.updatePage("reservationPage")
            else if (mainViewModel.uiState.value.isChecking)
                mainViewModel.updatePage("checkPage")
            else if (mainViewModel.uiState.value.isRenting)
                mainViewModel.updatePage("rentPage")
            else
                mainViewModel.updatePage("car")

            scope.launch {
                mainViewModel.uiState.value.sheetState!!.show()
            }
            true
        }



        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    SplashScreen(
                        navigateToDestination = {
                            navController.navigate(it) {
                                popUpTo(0)
                            }
                        },
                        userStore = userStore,
                        mainViewModel = mainViewModel
                    )
                }
                composable("permission") {
                    PermissionView(
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
                        navigation = navController,
                        mainViewModel = mainViewModel,
                        listener = listener
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