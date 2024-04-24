package com.syndicate.carsharing

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.syndicate.carsharing.ui.theme.CarsharingTheme
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.syndicate.carsharing.views.Camera
import com.syndicate.carsharing.views.Code
import com.syndicate.carsharing.views.Main
import com.syndicate.carsharing.views.Document
import com.syndicate.carsharing.views.DocumentIntro
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_KEY)
        setContent {
            CarsharingApp(userStore)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CarsharingApp(
    userStore: UserStore
) {
    CarsharingTheme {
        val navController = rememberNavController()

        val mainViewModel: MainViewModel = viewModel()
        val scope = rememberCoroutineScope()

        val sheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmValueChange = {
                if (it == ModalBottomSheetValue.Hidden) {
                    if (mainViewModel.circle.value != null)
                        mainViewModel.mapView.value!!.mapWindow.map.mapObjects.remove(mainViewModel.circle.value!!)
                    mainViewModel.updateCircle(null)
                }

                true
            },
            skipHalfExpanded = true
        )

        val listener: MapObjectTapListener = MapObjectTapListener { placemark, point: Point ->
            mainViewModel.updateLastSelectedPlacemark(placemark as PlacemarkMapObject)
            mainViewModel.updatePoints(1, RequestPoint(point, RequestPointType.WAYPOINT, null, null))

            if (mainViewModel.isReserving.value)
                mainViewModel.updatePage("reservationPage")
            else if (mainViewModel.isChecking.value)
                mainViewModel.updatePage("checkPage")
            else if (mainViewModel.isRenting.value)
                mainViewModel.updatePage("rentPage")
            else
                mainViewModel.updatePage("car")

            scope.launch {
                sheetState.show()
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
                        userStore = userStore
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
                        sheetState = sheetState,
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